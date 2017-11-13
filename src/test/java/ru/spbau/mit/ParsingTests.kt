package ru.spbau.mit
import jdk.nashorn.internal.parser.Lexer
import org.antlr.v4.runtime.CharStreams
import kotlin.test.assertEquals
import org.junit.Test
import ru.spbau.mit.parser.FunLanguageLexer

class ParsingTests {
    @Test
    fun loopStatement() {
        val lexer = FunLanguageLexer(CharStreams.fromString("(2+2)"))
        lexer.vocabulary
    }
}
