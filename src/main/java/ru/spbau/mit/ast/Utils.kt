package ru.spbau.mit.ast

import org.antlr.v4.runtime.CharStreams
import java.io.PrintStream

fun parseExpression(code: String): Expression {
    val statements = ASTBuilder
            .buildAST(CharStreams.fromString(code))
            .root.block.statements
    if (statements.size != 1) {
        throw RuntimeException() //todo
    }
    return statements.first() as Expression
}

fun parseFile(code: String): ASTree =
        ASTBuilder.buildAST(CharStreams.fromFileName(code))

suspend fun evaluate(expression: Expression, context: ExecutionContext): Int {
    val executor = Executor(context, null)
    return expression.visit(executor) as Int
}

suspend fun evaluate(
        tree: ASTree,
        output: PrintStream,
        listener: ExecutionListener? = null) {
    val context = ExecutionContext(null)
    context.defineFunction("print", PrintFunction(output))
    tree.evaluate(context, listener)
}
