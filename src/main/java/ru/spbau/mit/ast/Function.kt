package ru.spbau.mit.ast

import java.io.PrintStream

interface Function {
    suspend fun apply(arguments: List<Int>, listener: ExecutionListener?): Int
}

class RuntimeFunction(
        private val globalContext: ExecutionContext,
        private val functionBody: Block,
        private val argumentNames: List<String>) : Function {

    override suspend fun apply(arguments: List<Int>, listener: ExecutionListener?): Int {
        if (arguments.size != argumentNames.size) {
            throw WrongArgumentsNumberException()
        }
        val localContext = ExecutionContext(globalContext)
        argumentNames.zip(arguments, { name, value ->
            localContext.defineVariable(name, value)
        })
        functionBody.visit(Executor(localContext, listener))
        return localContext.getResult()
    }
}

data class PrintFunction(private val out: PrintStream) : Function {
    override suspend fun apply(arguments: List<Int>, listener: ExecutionListener?): Int {
        arguments.forEach { it: Int -> out.print("$it ") }
        out.println()
        return 0
    }
}
