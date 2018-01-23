package ru.spbau.mit.ast

interface ASTreeVisitor<out T> {
    suspend fun visitFile(file: File): T
    suspend fun visitBlock(block: Block): T
    suspend fun visitLiteral(literal: Literal): T
    suspend fun visitIdentifier(identifier: Identifier): T
    suspend fun visitIfStatement(ifStatement: IfStatement): T
    suspend fun visitFunctionCall(functionCall: FunctionCall): T
    suspend fun visitWhileStatement(whileStatement: WhileStatement): T
    suspend fun visitReturnStatement(returnStatement: ReturnStatement): T
    suspend fun visitBinaryExpression(binaryExpression: BinaryExpression): T
    suspend fun visitFunctionDefinition(functionDefinition: FunctionDefinition): T
    suspend fun visitAssignmentStatement(assignmentStatement: AssignmentStatement): T
    suspend fun visitVariableDeclaration(variableDeclaration: VariableDeclaration): T
}

data class ASTree(val root: File) {
    suspend fun evaluate(context: ExecutionContext, listener: ExecutionListener? = null) {
        try {
            root.visit(Executor(context, listener))
        } finally {
            listener?.executionFinished()
        }
    }
}

sealed class ASTNode {
    abstract val line: Int
    abstract suspend fun <T> visit(visitor: ASTreeVisitor<T>): T
}

sealed class Statement : ASTNode()

data class Block(
        val statements: List<Statement>,
        override val line: Int) : ASTNode() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitBlock(this)
}

data class File(val block: Block, override val line: Int) : ASTNode() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitFile(this)
}

sealed class Expression : Statement()


sealed class PrimitiveExpression : Expression()

data class Literal(val value: Int, override val line: Int) : PrimitiveExpression() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitLiteral(this)
}

data class Identifier(val name: String, override val line: Int) : PrimitiveExpression() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitIdentifier(this)
}

data class FunctionCall(
        val function: String,
        val arguments: List<Expression>,
        override val line: Int) : PrimitiveExpression() {

    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitFunctionCall(this)
}

data class BinaryExpression(
        val left: Expression,
        val right: Expression,
        val operator: Operator,
        override val line: Int) : Expression() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitBinaryExpression(this)
}

data class FunctionDefinition(
        val name: String,
        val body: Block,
        val arguments: List<String>,
        override val line: Int) : Statement() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitFunctionDefinition(this)
}

data class VariableDeclaration(
        val name: String,
        val expression: Expression?,
        override val line: Int) : Statement() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitVariableDeclaration(this)
}

data class WhileStatement(
        val condition: Expression,
        val body: Block,
        override val line: Int) : Statement() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitWhileStatement(this)
}

data class IfStatement(
        val condition: Expression,
        val body: Block,
        val elseBody: Block?,
        override val line: Int) : Statement() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitIfStatement(this)
}

data class AssignmentStatement(
        val name: String,
        val expression: Expression,
        override val line: Int) : Statement() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitAssignmentStatement(this)
}

data class ReturnStatement(
        val expression: Expression,
        override val line: Int) : Statement() {
    override suspend fun <T> visit(visitor: ASTreeVisitor<T>) = visitor.visitReturnStatement(this)
}
