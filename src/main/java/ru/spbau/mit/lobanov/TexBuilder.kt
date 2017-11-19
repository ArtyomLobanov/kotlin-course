package ru.spbau.mit.lobanov

import ru.spbau.mit.lobanov.KeyWord.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream

enum class KeyWord(val text: String) {
    BEGIN("\\begin"),
    END("\\end"),
    USE_PACKAGE("\\usepackage"),
    DOCUMENT("document"),
    DOCUMENT_CLASS("\\documentclass"),
    ITEM("\\item"),
    ENUMERATE("enumerate"),
    ITEMIZE("itemize"),
    FRAME("frame"),
    MATH("\\math"),
    NEW_LINE("~\\\\")
}

enum class Alignment(val text: String) {
    LEFT("flushleft"),
    RIGHT("flushright"),
    CENTER("center")
}

class TexContext(private val output: PrintStream) {

    // output utils

    private fun <T> Collection<T>.joinToString(prefix: String, postfix: String): String {
        return when {
            isEmpty() -> ""
            else -> joinToString(prefix = prefix, postfix = postfix, separator = ",")
        }
    }

    private fun extrasToString(extras: Array<out Any>): List<String> = extras.map {
        when (it) {
            is Pair<*, *> -> "${it.first}=${it.second}"
            else -> it.toString()
        }
    }

    private fun <T : TexElement> writeElement(element: T, writeBody: T.() -> Unit) {
        element.writeHeader()
        element.writeBody()
        element.writeFooter()
    }

    // Tex structure

    @DslMarker
    annotation class TexMarker

    @TexMarker
    inner abstract class TexElement {
        protected fun write(text: String) = output.println(text)

        protected fun writeSingleLineCommand(
                name: String,
                argument: String,
                extras: List<String>) {
            write("$name${extras.joinToString("[", "]")}{$argument}")
        }

        open fun writeHeader() {}
        open fun writeFooter() {}
    }

    inner open class Block(private val name: String? = null) : TexElement() {
        override fun writeHeader() {
            if (name != null) {
                writeSingleLineCommand(BEGIN.text, name, emptyList())
            }
        }

        override fun writeFooter() {
            if (name != null) {
                writeSingleLineCommand(END.text, name, emptyList())
            }
        }

        operator fun String.unaryPlus() {
            output.println(this)
        }

        fun enumerate(writeBody: ItemsList.() -> Unit) =
                writeElement(ItemsList(ENUMERATE.text), writeBody)

        fun itemize(writeBody: ItemsList.() -> Unit) =
                writeElement(ItemsList(ITEMIZE.text), writeBody)

        fun alignment(type: Alignment, writeBody: Block.() -> Unit) =
                writeElement(Block(type.text), writeBody)

        fun math(formula: String, vararg extras: Any) =
                writeSingleLineCommand(MATH.text, formula, extrasToString(extras))

        fun block(tag: String, writeBody: Block.() -> Unit) =
                writeElement(Block(tag), writeBody)

        fun command(name: String, argument: String, vararg extras: Any) =
                writeSingleLineCommand("\\$name", argument, extrasToString(extras))

        fun newline() = write(NEW_LINE.text)
    }

    inner class ItemsList(private val name: String) : TexElement() {

        override fun writeHeader() = writeSingleLineCommand(BEGIN.text, name, emptyList())

        override fun writeFooter() =
                writeSingleLineCommand(END.text, name, emptyList())

        fun item(text: String) = writeSingleLineCommand(ITEM.text, text, emptyList())

        fun item(block: Block.() -> Unit) {
            write("${ITEM.text} {")
            writeElement(Block(), block)
            write("}")
        }
    }

    inner class Document : Block(DOCUMENT.text) {
        fun frame(writeBody: Block.() -> Unit) = writeElement(Block(FRAME.text), writeBody)
    }

    inner class TexFile : TexElement() {

        fun usepackage(packageName: String, vararg extras: Any) =
                writeSingleLineCommand(USE_PACKAGE.text, packageName, extrasToString(extras))

        fun documentclass(documentClass: String, vararg extras: Any) =
                writeSingleLineCommand(DOCUMENT_CLASS.text, documentClass, extrasToString(extras))

        fun document(body: Document.() -> Unit) =
                writeElement(Document(), body)

        fun command(name: String, argument: String, vararg extras: Any) =
                writeSingleLineCommand("\\$name", argument, extrasToString(extras))
    }

    fun write(body: TexFile.() -> Unit) {
        writeElement(TexFile(), body)
    }
}

class TexBuilder(private val texStructure: TexContext.TexFile.() -> Unit) {
    fun toPrintStream(output: PrintStream) {
        TexContext(output).write(texStructure)
    }

    override fun toString(): String {
        val baos = ByteArrayOutputStream()
        val output = PrintStream(baos)
        toPrintStream(output)
        output.close()
        return baos.toString()
    }
}

fun tex(texFile: TexContext.TexFile.() -> Unit) = TexBuilder(texFile)




