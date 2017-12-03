package ru.spbau.mit.ast

import java.io.PrintStream

interface Function {
    fun apply(arguments: List<Int>): Int
}

class RuntimeFunction(
        private val globalContext: ExecutionContext,
        private val functionBody: Block,
        private val argumentNames: List<String>) : Function {

    override fun apply(arguments: List<Int>): Int {
        if (arguments.size != argumentNames.size) {
            throw WrongArgumentsNumberException
        }
        val localContext = ExecutionContext(globalContext)
        argumentNames.zip(arguments, { name, value -> 
            localContext.defineVariable(name, value)
        })
        functionBody.evaluate(localContext)
        return localContext.getResult()
    }
}

data class PrintFunction(private val out: PrintStream) : Function {
    override fun apply(arguments: List<Int>): Int {
        arguments.forEach{ it: Int -> out.print("$it ") }
        out.println()
        return 0
    }
}