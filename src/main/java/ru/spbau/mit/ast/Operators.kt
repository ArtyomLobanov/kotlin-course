package ru.spbau.mit.ast

enum class Operator(val calculator: (Int, Int) -> Int) {
    Plus({x, y -> x + y}),
    Minus({x, y -> x - y}),
    Multiply({x, y -> x * y}),
    Divide({x, y -> x / y}),
    Remainder({x, y -> x % y}),
    Greater({x, y -> booleanToInt(x > y)}),
    Less({x, y -> booleanToInt(x < y)}),
    GreaterOrEqual({x, y -> booleanToInt(x >= y)}),
    LessOrEqual({x, y -> booleanToInt(x <= y)}),
    Equal({x, y -> booleanToInt(x == y)}),
    NotEqual({x, y -> booleanToInt(x != y)}),
    Or({x, y -> booleanToInt(x != 0 || y != 0)}),
    And({x, y -> booleanToInt(x != 0 && y != 0)})
}

private fun booleanToInt(boolean: Boolean) = if (boolean) 1 else 0