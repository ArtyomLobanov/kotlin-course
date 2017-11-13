package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.interpreter.Interpreter
import ru.spbau.mit.interpreter.InterpreterException


fun main(args: Array<String>) {
    if (args.size != 1) {
        print("Exactly one argument expected (path to file)!")
    }
    try {
        Interpreter.runInterpreter(CharStreams.fromFileName(args[0]))
    } catch (e: Exception) {
        print("Something went wrong:\n")
        print("Message: ${e.message}\n")
        if (e is InterpreterException) {
            print("At line: ${e.line}\n")
        }
    }
}
