package ru.spbau.mit

import ru.spbau.mit.ast.Debugger

fun main(args: Array<String>) {
    val debugger = Debugger(System.out)
    debugger.run(System.`in`)
}