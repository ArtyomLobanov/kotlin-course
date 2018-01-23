package ru.spbau.mit

import kotlinx.coroutines.experimental.runBlocking
import ru.spbau.mit.ast.Debugger

fun main(args: Array<String>) {
    val debugger = Debugger(System.out)
    runBlocking {
        debugger.run(System.`in`)
    }
}