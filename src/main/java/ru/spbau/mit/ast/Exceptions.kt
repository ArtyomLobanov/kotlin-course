package ru.spbau.mit.ast

sealed class InterpreterException(message: String, val line: Int) : RuntimeException(message)

class WrongArgumentsNumberException : Exception("Wrong number of arguments in function call statement")

class FunctionCallException(line: Int)
    : InterpreterException("Wrong number of arguments in function call statement", line)

class UnknownIdentifierException(line: Int)
    : InterpreterException("Unknown identifier", line)

class SyntaxException(line: Int)
    : InterpreterException("Syntax exception! Failed to parse code!", line)

class ArithmeticException(line: Int)
    : InterpreterException("Forbidden mathematical operation!", line)

class RedefinitionException(line: Int)
    : InterpreterException("Attempt to redefine member in current scope", line)
