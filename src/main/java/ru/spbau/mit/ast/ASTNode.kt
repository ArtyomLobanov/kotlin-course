@file:Suppress("MemberVisibilityCanPrivate")

package ru.spbau.mit.ast

data class ASTree(val root: File) {
    fun evaluate(context: ExecutionContext) {
        root.evaluate(context)
    }
}

sealed class ASTNode() {
    abstract val line: Int
    abstract fun evaluate(context: ExecutionContext): Int?
}

sealed class Statement : ASTNode()

data class Block(
        val statements: List<Statement>,
        override val line: Int) : ASTNode() {
    override fun evaluate(context: ExecutionContext): Int? {
        val localContext = ExecutionContext(context)
        for (statement in statements) {
            statement.evaluate(localContext)
            if (localContext.isInterrupted()) {
                context.interrupt(localContext.getResult())
                break
            }
        }
        return null
    }
}

data class File(val block: Block, override val line: Int) : ASTNode() {
    override fun evaluate(context: ExecutionContext) = block.evaluate(context)
}

sealed class Expression : Statement() {
    override abstract fun evaluate(context: ExecutionContext): Int
}


sealed class PrimitiveExpression : Expression()

data class Literal(val value: Int, override val line: Int) : PrimitiveExpression() {
    override fun evaluate(context: ExecutionContext) = value
}

data class Identifier(val name: String, override val line: Int) : PrimitiveExpression() {
    override fun evaluate(context: ExecutionContext) : Int {
        return context.getVariable(name) ?: throw UnknownIdentifierException(line)
    }

}

data class FunctionCall(
        val function: String,
        val arguments: List<Expression>,
        override val line: Int) : PrimitiveExpression() {

    override fun evaluate(context: ExecutionContext): Int {
        val function = context.getFunction(function) ?: throw UnknownIdentifierException(line)
        val arguments = arguments.map { it -> it.evaluate(context) }
        try {
            return function.apply(arguments)
        } catch (e: WrongArgumentsNumberException) {
            throw FunctionCallException(line)
        }
    }
}

data class BinaryExpression(
        val left: Expression,
        val right: Expression,
        val operator: Operator,
        override val line: Int) : Expression() {
    override fun evaluate(context: ExecutionContext): Int {
        val leftValue = left.evaluate(context)
        val rightValue = right.evaluate(context)
        try {
            return operator.calculator(leftValue, rightValue)
        } catch (e: Exception) {
            throw ArithmeticException(line)
        }
    }
}

data class FunctionDefinition(
        val name: String,
        val body: Block,
        val arguments: List<String>,
        override val line: Int) : Statement() {
    override fun evaluate(context: ExecutionContext): Int? {
        val function = RuntimeFunction(context, body, arguments)
        if (!context.defineFunction(name, function)) {
            throw RedefinitionException(line)
        }
        return null
    }
}

data class VariableDeclaration(
        val name: String,
        val expression: Expression?,
        override val line: Int) : Statement() {
    override fun evaluate(context: ExecutionContext): Int? {
        val value = expression?.evaluate(context) ?: 0
        if (!context.defineVariable(name, value)) {
            throw RedefinitionException(line)
        }
        return null
    }
}

data class WhileStatement(
        val condition: Expression,
        val body: Block,
        override val line: Int) : Statement() {
    override fun evaluate(context: ExecutionContext): Int? {
        while (condition.evaluate(context) != 0 && !context.isInterrupted()) {
            body.evaluate(context)
        }
        return null
    }
}

data class IfStatement(
        val condition: Expression,
        val body: Block,
        val elseBody: Block?,
        override val line: Int) : Statement() {
    override fun evaluate(context: ExecutionContext): Int? {
        if (condition.evaluate(context) != 0) {
            body.evaluate(context)
        } else {
            elseBody?.evaluate(context)
        }
        return null
    }
}

data class AssignmentStatement(
        val name: String,
        val expression: Expression,
        override val line: Int) : Statement() {
    override fun evaluate(context: ExecutionContext): Int? {
        context.setVariable(name, expression.evaluate(context))
        return null
    }
}

data class ReturnStatement(
        val expression: Expression,
        override val line: Int) : Statement() {
    override fun evaluate(context: ExecutionContext): Int? {
        context.interrupt(expression.evaluate(context))
        return null
    }
}
