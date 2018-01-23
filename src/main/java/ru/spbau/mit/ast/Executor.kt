package ru.spbau.mit.ast

class Executor(private var context: ExecutionContext) : ASTreeVisitor<Int?> {

    override fun visitFile(file: File): Int? = file.block.visit(this)

    override fun visitBlock(block: Block): Int? {
        context = ExecutionContext(context)
        for (statement in block.statements) {
            statement.visit(this)
            if (context.isInterrupted()) {
                val result = context.getResult()
                context = context.parent as ExecutionContext
                context.interrupt(result)
                return null
            }
        }
        context = context.parent as ExecutionContext
        return null
    }

    override fun visitLiteral(literal: Literal) = literal.value

    override fun visitIdentifier(identifier: Identifier): Int {
        return context.getVariable(identifier.name) ?: throw UnknownIdentifierException(identifier.line)
    }

    override fun visitIfStatement(ifStatement: IfStatement): Int? {
        if (ifStatement.condition.visit(this) != 0) {
            ifStatement.body.visit(this)
        } else {
            ifStatement.elseBody?.visit(this)
        }
        return null
    }

    override fun visitFunctionCall(functionCall: FunctionCall): Int {
        val function = context.getFunction(functionCall.function) ?: throw UnknownIdentifierException(functionCall.line)
        val arguments = functionCall.arguments.map { it -> it.visit(this) as Int }
        try {
            return function.apply(arguments)
        } catch (e: WrongArgumentsNumberException) {
            throw FunctionCallException(functionCall.line)
        }
    }

    override fun visitWhileStatement(whileStatement: WhileStatement): Int? {
        while (whileStatement.condition.visit(this) != 0 && !context.isInterrupted()) {
            whileStatement.body.visit(this)
        }
        return null
    }

    override fun visitReturnStatement(returnStatement: ReturnStatement): Int? {
        context.interrupt(returnStatement.expression.visit(this) as Int)
        return null
    }

    override fun visitBinaryExpression(binaryExpression: BinaryExpression): Int {
        val leftValue = binaryExpression.left.visit(this) as Int
        val rightValue = binaryExpression.right.visit(this) as Int
        try {
            return binaryExpression.operator.calculator(leftValue, rightValue)
        } catch (e: Exception) {
            throw ArithmeticException(binaryExpression.line)
        }
    }

    override fun visitFunctionDefinition(functionDefinition: FunctionDefinition): Int? {
        val function = RuntimeFunction(context, functionDefinition.body, functionDefinition.arguments)
        if (!context.defineFunction(functionDefinition.name, function)) {
            throw RedefinitionException(functionDefinition.line)
        }
        return null
    }

    override fun visitAssignmentStatement(assignmentStatement: AssignmentStatement): Int? {
        context.setVariable(assignmentStatement.name, assignmentStatement.expression.visit(this) as Int)
        return null
    }

    override fun visitVariableDeclaration(variableDeclaration: VariableDeclaration): Int? {
        val value = variableDeclaration.expression?.visit(this) ?: 0
        if (!context.defineVariable(variableDeclaration.name, value)) {
            throw RedefinitionException(variableDeclaration.line)
        }
        return null
    }
}