package ru.spbau.mit

import org.antlr.v4.gui.TreeViewer
import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree
import ru.spbau.mit.ast.ASTBuilder
import ru.spbau.mit.ast.ExecutionContext
import ru.spbau.mit.ast.InterpreterException
import ru.spbau.mit.ast.PrintFunction
import ru.spbau.mit.parser.FunLanguageLexer
import ru.spbau.mit.parser.FunLanguageParser
import javax.swing.JFrame
import javax.swing.JPanel


fun main(args: Array<String>) {
    if (args.size != 1) {
        print("Exactly one argument expected (path to file)!")
    }
    try {

        val lexer = FunLanguageLexer(CharStreams.fromFileName(args[0]))
        val parser = FunLanguageParser(BufferedTokenStream(lexer))
        parser.buildParseTree = true
        val tree = ASTBuilder.buildAST(CharStreams.fromFileName(args[0]))
        val context = ExecutionContext(null)
        context.defineFunction("println", PrintFunction(System.out))
        tree.evaluate(context)
    } catch (e: Exception) {
        print("Something went wrong:\n")
        print("Message: ${e.message}\n")
        if (e is InterpreterException) {
            print("At line: ${e.line}\n")
        }
    }
}

fun showTree(tree: ParseTree, parser: Parser) {
    val frame = JFrame("Antlr AST")
    val panel = JPanel()
    val viewer = TreeViewer(parser.ruleNames.asList(), tree)
    viewer.scale = 1.5
    panel.add(viewer)
    frame.add(panel)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(1000, 1000)
    frame.isVisible = true
}
