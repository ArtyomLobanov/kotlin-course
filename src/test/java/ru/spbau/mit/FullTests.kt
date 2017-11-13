package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import org.junit.Test
import ru.spbau.mit.ast.ASTBuilder
import ru.spbau.mit.ast.ExecutionContext
import kotlin.test.assertEquals

class FullTests {
    @Test
    fun test1() {
        val tree = ASTBuilder.buildAST(CharStreams.fromString(
                "return 2*3+2"
        ))
        val context = ExecutionContext(null)
        tree.evaluate(context)
        assertEquals(8, context.getResult())
    }
}