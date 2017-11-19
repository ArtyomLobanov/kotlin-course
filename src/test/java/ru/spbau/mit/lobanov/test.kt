package ru.spbau.mit.lobanov

import org.junit.Test
import ru.spbau.mit.lobanov.Alignment.*
import kotlin.test.assertEquals

class TestSource {
    @Test
    fun framesTest() {
        val real = tex {
            document {
                frame {
                    command("frametitle", "Name!")
                    +"First Name"
                    newline()
                    +"Last Name"
                }
                frame {
                    command("frametitle", "Name!")
                    +"First Name"
                    newline()
                    math("34234+3423=21")
                }
            }
        }.toString().trimMargin()
        val expected = "\\begin{document}\n" +
                "\\begin{frame}\n" +
                "\\frametitle{Name!}\n" +
                "First Name\n" +
                "~\\\\\n" +
                "Last Name\n" +
                "\\end{frame}\n" +
                "\\begin{frame}\n" +
                "\\frametitle{Name!}\n" +
                "First Name\n" +
                "~\\\\\n" +
                "\\math{34234+3423=21}\n" +
                "\\end{frame}\n" +
                "\\end{document}".trimMargin()
        assertEquals(expected, real)
    }

    @Test
    fun enumerations() {
        val real = tex {
            document {
                enumerate {
                    item("short item")
                    item {
                        block("formatted_block") {
                            +"Some text"
                        }
                        +"Some text"
                    }
                    item {
                        itemize {
                            item {
                                +"Some text2\n\n"
                                +"Another text"
                            }
                            item("last text")
                        }
                    }
                }
            }
        }.toString().trimMargin()
        val expected = "\\begin{document}\n" +
                "\\begin{enumerate}\n" +
                "\\item{short item}\n" +
                "\\item {\n" +
                "\\begin{formatted_block}\n" +
                "Some text\n" +
                "\\end{formatted_block}\n" +
                "Some text\n" +
                "}\n" +
                "\\item {\n" +
                "\\begin{itemize}\n" +
                "\\item {\n" +
                "Some text2\n" +
                "\n" +
                "\n" +
                "Another text\n" +
                "}\n" +
                "\\item{last text}\n" +
                "\\end{itemize}\n" +
                "}\n" +
                "\\end{enumerate}\n" +
                "\\end{document}".trimMargin()

        assertEquals(expected, real)
    }

    @Test
    fun varargsTest() {
        val real = tex {
            usepackage("pack1", "arg1", "arg2" to "rgb", "size" to 3)
            usepackage("pack2", CENTER)
        }.toString().trimMargin()
        val expected = "\\usepackage[arg1,arg2=rgb,size=3]{pack1}\n" +
                "\\usepackage[CENTER]{pack2}\n".trimMargin()

        assertEquals(expected, real)
    }

    @Test
    fun alignmentsTest() {
        val real = tex {
            document {
                alignment(CENTER) {
                    +"Some text"
                    newline()
                    +"Some anther text"
                }
                alignment(LEFT) {
                    +"Some to left"
                    newline()
                    +"Some anther to left"
                }
                alignment(RIGHT) {
                    +"Some to right"
                    newline()
                    +"Some anther to right"
                }
            }
        }.toString().trimMargin()
        val expected = "\\begin{document}\n" +
                "\\begin{center}\n" +
                "Some text\n" +
                "~\\\\\n" +
                "Some anther text\n" +
                "\\end{center}\n" +
                "\\begin{flushleft}\n" +
                "Some to left\n" +
                "~\\\\\n" +
                "Some anther to left\n" +
                "\\end{flushleft}\n" +
                "\\begin{flushright}\n" +
                "Some to right\n" +
                "~\\\\\n" +
                "Some anther to right\n" +
                "\\end{flushright}\n" +
                "\\end{document}".trimMargin()

        assertEquals(expected, real)
    }

    @Test
    fun documentSetup() {
        val real = tex {
            documentclass("article")
            usepackage("color")
            usepackage("ifxetex")
            usepackage("fontenc", "T2A")
            usepackage("fontspec")
            command("setmainfont", "CMU Serif", "Ligatures" to "TeX")

            document {
                enumerate {
                    item("1")
                    item("2")
                }
                newline()
                +"The End!"
            }
        }.toString().trimMargin()
        val expected = "\\documentclass{article}\n" +
                "\\usepackage{color}\n" +
                "\\usepackage{ifxetex}\n" +
                "\\usepackage[T2A]{fontenc}\n" +
                "\\usepackage{fontspec}\n" +
                "\\setmainfont[Ligatures=TeX]{CMU Serif}\n" +
                "\\begin{document}\n" +
                "\\begin{enumerate}\n" +
                "\\item{1}\n" +
                "\\item{2}\n" +
                "\\end{enumerate}\n" +
                "~\\\\\n" +
                "The End!\n" +
                "\\end{document}".trimMargin()

        assertEquals(expected, real)
    }
}
