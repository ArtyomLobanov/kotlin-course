package ru.spbau.mit.interpreter

import ru.spbau.mit.parser.FunLanguageParser.BlockWithBracesContext

interface Function {
    fun apply(arguments: List<Int>): Int
}

class RuntimeFunction (
        private val globalContext: ExecutionContext,
        private val functionBody: BlockWithBracesContext,
        private val argumentNames: List<String>) : Function{

    override fun apply(arguments: List<Int>): Int {
        if (arguments.size != argumentNames.size) {
            throw WrongArgumentsNumberException
        }
        val localContext = ExecutionContext(globalContext)
        argumentNames.zip(arguments).forEach {
            (name, value) -> localContext.defineVariable(name, value)
        }
        Interpreter.interpretBlock(functionBody.block(), localContext)
        return localContext.getResult()
    }
}

object PrintFunction : Function {
    override fun apply(arguments: List<Int>): Int {
        arguments.forEach({it -> print("$it ")})
        print('\n')
        return 0
    }
}