package ru.spbau.mit.interpreter

open class InterpreterException(message: String, val line: Int) : RuntimeException(message)

class FunctionCallFailedException(reason: Exception, line: Int)
    : InterpreterException(reason.message ?: "Unknown exception during function call", line)

object WrongArgumentsNumberException
    : Error("Wrong number of arguments in function call statement")

class UnknownIdentifierException(line: Int) : InterpreterException("Unknown identifier", line)

class UnknownASTNodeException(line: Int) : InterpreterException("Internal exception!", line)

class ArithmeticException(line: Int) : InterpreterException("Forbidden mathematical operation!", line)