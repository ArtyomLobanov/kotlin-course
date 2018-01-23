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
                "return 2*3+2*2 + 4 * 4"
        ))
        val context = ExecutionContext(null)
        tree.evaluate(context)
        assertEquals(26, context.getResult())
    }

    @Test
    fun test2() {
        val tree = ASTBuilder.buildAST(CharStreams.fromString(
                "return (2*3+2*2 < (0 -4) * (0 -4))"
        ))
        val context = ExecutionContext(null)
        tree.evaluate(context)
        assertEquals(1, context.getResult())
    }

    @Test
    fun test3() {
        val tree = ASTBuilder.buildAST(CharStreams.fromString(
                "var t = 1\n" +
                        "fun p(x) {\n" +
                        "t = t * x\n" +
                        "}\n" +
                        "p(2)\n" +
                        "p(p(2) + 1)\n" +
                        "return t"
        ))
        val context = ExecutionContext(null)
        tree.evaluate(context)
        assertEquals(4, context.getResult())
    }

    @Test
    fun test4() {
        val tree = ASTBuilder.buildAST(CharStreams.fromString(
                "var n = 24\n" +
                        "var t = 1\n" +
                        "var cnt\n" +
                        "while(t <= n){\n" +
                        "   if (n % t == 0) {\n" +
                        "       cnt = cnt + 1\n" +
                        "   }\n" +
                        "   t = t + 1\n" +
                        "}\n" +
                        "return cnt\n"
        ))
        val context = ExecutionContext(null)
        tree.evaluate(context)
        assertEquals(8, context.getResult())
    }
}