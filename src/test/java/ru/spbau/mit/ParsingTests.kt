package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import org.junit.Test
import ru.spbau.mit.ast.*
import kotlin.test.assertEquals

class ParsingTests {
    @Test
    fun functionDefinition() {
        val tree = ASTBuilder.buildAST(CharStreams.fromString(
                "fun foo(a) {\n" +
                        "return a + 1\n" +
                        "}"
        )).root
        val expectedTree = File(
                Block(
                        listOf(FunctionDefinition("foo",
                                Block(
                                        listOf(
                                                ReturnStatement(
                                                        BinaryExpression(
                                                                Identifier("a", 2),
                                                                Literal(1, 2),
                                                                Operator.Plus,
                                                                2
                                                        ),
                                                        2
                                                )
                                        ),
                                        2
                                ),
                                listOf("a"),
                                1)
                        ),
                        1),
                1
        )
        assertEquals(expectedTree, tree)
    }

    @Test
    fun functionCall() {
        val tree = ASTBuilder.buildAST(CharStreams.fromString(
                "foo(a, b / 1)\n" +
                        "//nothing\n" +
                        "2"
        )).root
        val expectedTree = File(
                Block(
                        listOf(
                                FunctionCall("foo",
                                        listOf(
                                                Identifier("a", 1),
                                                BinaryExpression(
                                                        Identifier("b", 1),
                                                        Literal(1, 1),
                                                        Operator.Divide,
                                                        1
                                                )
                                        ),
                                        1
                                ),
                                Literal(2, 3)
                        ),
                        1
                ),
                1)
        assertEquals(expectedTree, tree)
    }

    @Test
    fun whileCycle() {
        val tree = ASTBuilder.buildAST(CharStreams.fromString(
                "while(a > b) {\n" +
                        "a = 3\n" +
                        "}"
        )).root
        val expectedTree = File(
                Block(
                        listOf(
                                WhileStatement(
                                        BinaryExpression(
                                                Identifier("a", 1),
                                                Identifier("b", 1),
                                                Operator.Greater,
                                                1
                                        ),
                                        Block(
                                                listOf(
                                                        AssignmentStatement(
                                                                "a",
                                                                Literal(3, 2),
                                                                2
                                                        )
                                                ),
                                                2
                                        ),
                                        1
                                )
                        ),
                        1
                ),
                1)
        assertEquals(expectedTree, tree)
    }

    @Test
    fun ifStatment() {
        val tree = ASTBuilder.buildAST(CharStreams.fromString(
                "if (a) {\n" +
                        "var a = 3\n" +
                        "} else {\n " +
                        "tmp6 = 8" +
                        "}"
        )).root
        val expectedTree = File(
                Block(
                        listOf(
                                IfStatement(
                                        Identifier("a", 1),
                                        Block(
                                                listOf(
                                                        VariableDeclaration(
                                                                "a",
                                                                Literal(3, 2),
                                                                2
                                                        )
                                                ),
                                                2
                                        ),
                                        Block(
                                                listOf(
                                                        AssignmentStatement(
                                                                "tmp6",
                                                                Literal(8, 4),
                                                                4
                                                        )
                                                ),
                                                4
                                        ),
                                        1
                                )
                        ),
                        1
                ),
                1)
        assertEquals(expectedTree, tree)
    }
}
