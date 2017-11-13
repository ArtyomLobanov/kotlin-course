@file:Suppress("MemberVisibilityCanPrivate")

package ru.spbau.mit.ast

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.tree.TerminalNode
import ru.spbau.mit.parser.FunLanguageLexer
import ru.spbau.mit.parser.FunLanguageParser
import ru.spbau.mit.parser.FunLanguageParser.*

object ASTBuilder {

    fun buildAST(stream: CharStream) : ASTree {
        val lexer = FunLanguageLexer(stream)
        val parser = FunLanguageParser(BufferedTokenStream(lexer))
        return ASTree(visitFile(parser.file()))
    }

    fun visitExpression(expression: ExpressionContext): Expression {
        return when {
            expression.primitiveExpression() !== null ->
                visitPrimitiveExpression(expression.primitiveExpression())
            expression.binaryExpression() !== null ->
                visitBinaryExpression(expression.binaryExpression())
            else -> throw UnknownASTNodeException(expression.start.line)
        }
    }

    fun visitPrimitiveExpression(expression: PrimitiveExpressionContext): Expression {
        return when {
            expression.functionCall() !== null -> visitFunctionCall(expression.functionCall())
            expression.expression() !== null -> visitExpression(expression.expression())
            expression.INTEGER() !== null -> visitLiteral(expression.INTEGER())
            expression.IDENTIFIER() !== null -> visitIdentifier(expression.IDENTIFIER())
            else -> throw UnknownASTNodeException(expression.start.line)
        }
    }

    fun visitBinaryExpression(expression: BinaryExpressionContext): BinaryExpression {
        val leftValue = visitPrimitiveExpression(expression.primitiveExpression())
        val rightValue = visitExpression(expression.expression())
        val operator = when (expression.op.type) {
            FunLanguageParser.MULTIPLY -> Operator.Multiply
            FunLanguageParser.DIVIDE -> Operator.Divide
            FunLanguageParser.REMAINDER -> Operator.Remainder
            FunLanguageParser.PLUS -> Operator.Plus
            FunLanguageParser.MINUS -> Operator.Minus
            FunLanguageParser.GREATER -> Operator.Greater
            FunLanguageParser.LESS -> Operator.Less
            FunLanguageParser.GREATER_OR_EQUAL -> Operator.GreaterOrEqual
            FunLanguageParser.LESS_OR_EQUAL -> Operator.LessOrEqual
            FunLanguageParser.EQUAL -> Operator.Equal
            FunLanguageParser.NOT_EQUAL -> Operator.NotEqual
            FunLanguageParser.LOGICAL_OR -> Operator.Or
            FunLanguageParser.LOGICAL_AND -> Operator.And
            else -> throw UnknownASTNodeException(expression.start.line)
        }
        return BinaryExpression(leftValue, rightValue, operator, expression.start.line)
    }

    fun visitLiteral(literal: TerminalNode): Literal {
        return Literal(literal.symbol.text.toInt(), literal.symbol.line)
    }

    fun visitIdentifier(identifier: TerminalNode): Identifier =
            Identifier(identifier.symbol.text, identifier.symbol.line)

    fun visitFunctionCall(callStatement: FunctionCallContext): FunctionCall {
        val name = callStatement.IDENTIFIER().symbol.text
        val arguments = callStatement.arguments().expression()
                .map(this::visitExpression)
        return FunctionCall(name, arguments, callStatement.start.line)
    }

    fun visitFile(file: FileContext): File {
        return File(visitBlock(file.block()), file.start.line)
    }

    fun visitBlock(block: BlockContext): Block {
        val statements = block.statement().map(this::visitStatement)
        return Block(statements, block.start.line)
    }

    fun visitStatement(statement: StatementContext): Statement {
        return when {
            statement.assignmentStatement() !== null ->
                visitAssignmentStatement(statement.assignmentStatement())
            statement.expression() !== null ->
                visitExpression(statement.expression())
            statement.functionDefinition() !== null ->
                visitFunctionDefinition(statement.functionDefinition())
            statement.ifStatement() !== null ->
                visitIfStatement(statement.ifStatement())
            statement.variableDeclaration() !== null ->
                visitVariableDeclaration(statement.variableDeclaration())
            statement.whileStatement() !== null ->
                visitWhileStatement(statement.whileStatement())
            statement.returnStatement() !== null ->
                visitReturnStatement(statement.returnStatement())
            else -> throw UnknownASTNodeException(statement.start.line)
        }
    }

    fun visitReturnStatement(statement: ReturnStatementContext): ReturnStatement {
        val expression = visitExpression(statement.expression())
        return ReturnStatement(expression, statement.start.line)
    }

    fun visitAssignmentStatement(assignment: AssignmentStatementContext): AssignmentStatement {
        val name = assignment.IDENTIFIER().symbol.text
        val expression = visitExpression(assignment.expression())
        return AssignmentStatement(name, expression, assignment.start.line)
    }

    fun visitIfStatement(statement: IfStatementContext): IfStatement {
        val condition = visitExpression(statement.expression())
        val body = visitBlock(statement.blockWithBraces(0).block())
        val elseBody = when (statement.blockWithBraces(1)?.block()) {
            null -> null
            else -> visitBlock(statement.blockWithBraces(1).block())
        }
        return IfStatement(condition, body, elseBody, statement.start.line)
    }

    fun visitWhileStatement(loop: WhileStatementContext): WhileStatement {
        val condition = visitExpression(loop.expression())
        val body = visitBlock(loop.blockWithBraces().block())
        return WhileStatement(condition, body, loop.start.line)
    }


    fun visitVariableDeclaration(declaration: VariableDeclarationContext): VariableDeclaration {
        val name = declaration.IDENTIFIER().symbol.text
        val expression = visitExpression(declaration.expression())
        return VariableDeclaration(name, expression, declaration.start.line)
    }

    fun visitFunctionDefinition(definition: FunctionDefinitionContext): FunctionDefinition {
        val name = definition.IDENTIFIER().symbol.text
        val argumentsNames = definition.parameterNames().IDENTIFIER()
                .map { it -> it.symbol.text }
                .toList()
        val body = visitBlock(definition.blockWithBraces().block())
        return FunctionDefinition(name, body, argumentsNames, definition.start.line)
    }
}