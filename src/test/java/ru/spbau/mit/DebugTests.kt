package ru.spbau.mit

import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import ru.spbau.mit.ast.Debugger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class DebugTests {
    @Test
    fun test1() {
        val output = ByteArrayOutputStream()
        val wrapper = PrintStream(output)
        val debugger = Debugger(wrapper)
        runBlocking {
            debugger.run(input(
                    """load src/test/resources/debugProgram
                        list
                        breakpoint 1
                        condition 6 t > 3
                        list
                        remove 6
                        list""".trimMargin()
            ))
        }
        val expected = """
            >Program loaded.
            >List of breakpoints:

            >>>List of breakpoints:
               At line 1, condition: empty
               At line 6, condition: t > 3

            >>List of breakpoints:
               At line 1, condition: empty

            >""".trimIndent()
        assertEquals(expected, output.toString("UTF-8").trimIndent())
    }

    @Test
    fun test2() {
        val output = ByteArrayOutputStream()
        val wrapper = PrintStream(output)
        val debugger = Debugger(wrapper)
        runBlocking {
            debugger.run(input(
                    """load src/test/resources/debugProgram
                        breakpoint 8
                        run
                        evaluate 2+2
                        continue""".trimMargin()
            ))
        }
        val expected = """
            >Program loaded.
            >>5
            line=8,elementType=FunctionCall>=4
            line=8,elementType=FunctionCall>line=8,elementType=Identifier>""".trimIndent()
        assertEquals(expected, output.toString("UTF-8").trimIndent())
    }

    @Test
    fun test3() {
        val output = ByteArrayOutputStream()
        val wrapper = PrintStream(output)
        val debugger = Debugger(wrapper)
        runBlocking {
            debugger.run(input(
                    """load src/test/resources/debugProgram
                        condition 8 t == 5
                        condition 7 t == 7
                        run
                        continue""".trimMargin()
            ))
        }
        val expected = """
            >Program loaded.
            >>>5
            line=8,elementType=FunctionCall>line=8,elementType=Identifier>""".trimIndent()
        assertEquals(expected, output.toString("UTF-8").trimIndent())
    }

    @Test
    fun test4() {

        val output = ByteArrayOutputStream()
        val wrapper = PrintStream(output)
        val debugger = Debugger(wrapper)
        runBlocking {
            debugger.run(input(
                    """load src/test/resources/debugProgram
                        breakpoint 8
                        run
                        stop""".trimMargin()
            ))
        }
        val expected = """
            >Program loaded.
            >>5
            line=8,elementType=FunctionCall>>""".trimIndent()
        assertEquals(expected, output.toString("UTF-8").trimIndent())
    }

    private fun input(text: String): InputStream {
        return ByteArrayInputStream(text.toByteArray())
    }
}
