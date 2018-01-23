package ru.spbau.mit.ast

class Executor(
        private var context: ExecutionContext,
        private val listener: ExecutionListener?) : ASTreeVisitor<Int?> {

    override suspend fun visitFile(file: File): Int? = file.block.visit(this)

    override suspend fun visitBlock(block: Block): Int? {
        listener?.visitNode(block, context)
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

    override suspend fun visitLiteral(literal: Literal) = literal.value

    override suspend fun visitIdentifier(identifier: Identifier): Int {
        listener?.visitNode(identifier, context)
        return context.getVariable(identifier.name) ?: throw UnknownIdentifierException(identifier.line)
    }

    override suspend fun visitIfStatement(ifStatement: IfStatement): Int? {
        listener?.visitNode(ifStatement, context)
        if (ifStatement.condition.visit(this) != 0) {
            ifStatement.body.visit(this)
        } else {
            ifStatement.elseBody?.visit(this)
        }
        return null
    }

    override suspend fun visitFunctionCall(functionCall: FunctionCall): Int {
        listener?.visitNode(functionCall, context)
        val function = context.getFunction(functionCall.function) ?: throw UnknownIdentifierException(functionCall.line)
        val arguments = functionCall.arguments.map { it -> it.visit(this) as Int }
        try {
            return function.apply(arguments, listener)
        } catch (e: WrongArgumentsNumberException) {
            throw FunctionCallException(functionCall.line)
        }
    }

    override suspend fun visitWhileStatement(whileStatement: WhileStatement): Int? {
        listener?.visitNode(whileStatement, context)
        while (whileStatement.condition.visit(this) != 0 && !context.isInterrupted()) {
            whileStatement.body.visit(this)
        }
        return null
    }

    override suspend fun visitReturnStatement(returnStatement: ReturnStatement): Int? {
        listener?.visitNode(returnStatement, context)
        context.interrupt(returnStatement.expression.visit(this) as Int)
        return null
    }

    override suspend fun visitBinaryExpression(binaryExpression: BinaryExpression): Int {
        listener?.visitNode(binaryExpression, context)
        val leftValue = binaryExpression.left.visit(this) as Int
        val rightValue = binaryExpression.right.visit(this) as Int
        try {
            return binaryExpression.operator.calculator(leftValue, rightValue)
        } catch (e: Exception) {
            throw ArithmeticException(binaryExpression.line)
        }
    }

    override suspend fun visitFunctionDefinition(functionDefinition: FunctionDefinition): Int? {
        listener?.visitNode(functionDefinition, context)
        val function = RuntimeFunction(context, functionDefinition.body, functionDefinition.arguments)
        if (!context.defineFunction(functionDefinition.name, function)) {
            throw RedefinitionException(functionDefinition.line)
        }
        return null
    }

    override suspend fun visitAssignmentStatement(assignmentStatement: AssignmentStatement): Int? {
        listener?.visitNode(assignmentStatement, context)
        context.setVariable(assignmentStatement.name, assignmentStatement.expression.visit(this) as Int)
        return null
    }

    override suspend fun visitVariableDeclaration(variableDeclaration: VariableDeclaration): Int? {
        listener?.visitNode(variableDeclaration, context)
        val value = variableDeclaration.expression?.visit(this) ?: 0
        if (!context.defineVariable(variableDeclaration.name, value)) {
            throw RedefinitionException(variableDeclaration.line)
        }
        return null
    }
}

interface ExecutionListener {
    suspend fun visitNode(node: ASTNode, context: ExecutionContext)
    suspend fun executionFinished()
}