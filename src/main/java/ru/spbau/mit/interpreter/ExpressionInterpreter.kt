package ru.spbau.mit.interpreter

import org.antlr.v4.runtime.tree.TerminalNode
import ru.spbau.mit.parser.FunLanguageParser.*

object ExpressionInterpreter {
    fun interpretExpression(expression: ExpressionContext, context: ExecutionContext): Int {
        return when {
            expression.primitiveExpression() !== null ->
                interpretPrimitiveExpression(expression.primitiveExpression(), context)
            expression.binaryExpression() !== null ->
                interpretBinaryExpression(expression.binaryExpression(), context)
            else -> throw UnknownASTNodeException(expression.start.line)
        }
    }

    fun interpretPrimitiveExpression(expression: PrimitiveExpressionContext, context: ExecutionContext): Int {
        return when {
            expression.functionCall() !== null -> interpretFunctionCall(expression.functionCall(), context)
            expression.expression() !== null -> interpretExpression(expression.expression(), context)
            expression.INTEGER() !== null -> interpretLiteral(expression.INTEGER(), context)
            expression.IDENTIFIER() !== null -> interpretIdentifier(expression.IDENTIFIER(), context)
            else -> throw UnknownASTNodeException(expression.start.line)
        }
    }

    fun interpretBinaryExpression(expression: BinaryExpressionContext, context: ExecutionContext): Int {
        val leftValue = interpretPrimitiveExpression(expression.primitiveExpression(), context)
        val rightValue = interpretExpression(expression.expression(), context)
        return when (expression.op.type) {
            MULTIPLY -> leftValue * rightValue
            DIVIDE -> if (rightValue != 0) leftValue / rightValue else
                throw ArithmeticException(expression.start.line)
            REMAINDER -> if (rightValue != 0) leftValue % rightValue else
                throw ArithmeticException(expression.start.line)
            PLUS -> leftValue + rightValue
            MINUS -> leftValue - rightValue
            GREATER -> booleanToInt(leftValue > rightValue)
            LESS -> booleanToInt(leftValue < rightValue)
            GREATER_OR_EQUAL -> booleanToInt(leftValue >= rightValue)
            LESS_OR_EQUAL -> booleanToInt(leftValue <= rightValue)
            EQUAL -> booleanToInt(leftValue == rightValue)
            NOT_EQUAL -> booleanToInt(rightValue != leftValue)
            LOGICAL_OR -> booleanToInt(leftValue != 0 || rightValue != 0)
            LOGICAL_AND -> booleanToInt(leftValue != 0 && rightValue != 0)
            else -> throw UnknownASTNodeException(expression.start.line)
        }
    }

    fun interpretLiteral(literal: TerminalNode, context: ExecutionContext): Int {
        return literal.symbol.text.toInt()
    }

    fun interpretIdentifier(identifier: TerminalNode, context: ExecutionContext): Int {
        return context.getVariable(identifier.symbol.text) ?:
                throw UnknownIdentifierException(identifier.symbol.line)
    }

    fun interpretFunctionCall(callStatement: FunctionCallContext, context: ExecutionContext): Int {
        val arguments = callStatement.arguments().expression().map { it ->
            interpretExpression(it, context)
        }
        val function = context.getFunction(callStatement.IDENTIFIER().symbol.text)
                ?: throw UnknownIdentifierException(callStatement.start.line);
        try {
            return function.apply(arguments)
        } catch (e: InterpreterException) {
            throw e
        } catch (e: Exception) {
            throw FunctionCallFailedException(e, callStatement.start.line)
        }
    }

    private fun booleanToInt(boolean: Boolean): Int = if (boolean) 1 else 0
}