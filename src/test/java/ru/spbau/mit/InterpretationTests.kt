package ru.spbau.mit

import org.junit.Test
import ru.spbau.mit.ast.*
import kotlin.test.assertEquals

class InterpretationTests {
    @Test
    fun functionCall() {
        val tree = ASTree(
                File(
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
                                        1),
                                        ReturnStatement(
                                                FunctionCall(
                                                        "foo",
                                                        listOf(
                                                                Literal(4, 4)
                                                        ),
                                                        4
                                                ),
                                                4
                                        )
                                ),
                                1),
                        1
                )
        )
        val context = ExecutionContext(null)
        tree.evaluate(context)
        assertEquals(5, context.getResult())
    }

    @Test
    fun expressionEvaluating() {
        val tree = ASTree(
                File(
                        Block(
                                listOf(
                                        ReturnStatement(
                                                BinaryExpression(
                                                        BinaryExpression(
                                                                BinaryExpression(
                                                                        Literal(2, 1),
                                                                        Literal(3, 1),
                                                                        Operator.Multiply,
                                                                        1
                                                                ),
                                                                Literal(4, 1),
                                                                Operator.Plus,
                                                                1
                                                        ),
                                                        Literal(7, 1),
                                                        Operator.Remainder,
                                                        1
                                                ),
                                                1
                                        )
                                ),
                                1
                        ),
                        1)
        )

        val context = ExecutionContext(null)
        tree.evaluate(context)
        assertEquals(3, context.getResult())
    }
}