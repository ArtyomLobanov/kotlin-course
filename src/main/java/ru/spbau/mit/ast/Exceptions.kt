package ru.spbau.mit.ast

sealed class InterpreterException(message: String, val line: Int) : RuntimeException(message)

object WrongArgumentsNumberException
    : Error("Wrong number of arguments in function call statement")

class UnknownIdentifierException(line: Int)
    : InterpreterException("Unknown identifier", line)

class UnknownASTNodeException(line: Int)
    : InterpreterException("Internal exception!", line)

class ArithmeticException(line: Int)
    : InterpreterException("Forbidden mathematical operation!", line)

class RedefinitionException(line: Int)
    : InterpreterException("Attempt to redefine member in current scope", line)