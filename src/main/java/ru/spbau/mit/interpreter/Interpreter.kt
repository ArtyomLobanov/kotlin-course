@file:Suppress("MemberVisibilityCanPrivate")

package ru.spbau.mit.interpreter

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStream
import ru.spbau.mit.interpreter.ExpressionInterpreter.interpretExpression
import ru.spbau.mit.parser.FunLanguageLexer
import ru.spbau.mit.parser.FunLanguageParser
import ru.spbau.mit.parser.FunLanguageParser.*

object Interpreter {

    fun runInterpreter(stream: CharStream) {
        val lexer = FunLanguageLexer(stream)
        val parser = FunLanguageParser(BufferedTokenStream(lexer))
        val languageContext = ExecutionContext(null)
        languageContext.defineFunction("println", PrintFunction)
        interpretFile(parser.file(), languageContext)
    }

    fun interpretFile(file: FileContext, context: ExecutionContext) {
        interpretBlock(file.block(), context)
    }

    fun interpretBlock(block: BlockContext, context: ExecutionContext) {
        val localContext = ExecutionContext(context)
        for (it in block.statement()) {
            interpretStatement(it, localContext)
            if (localContext.isInterrupted()) {
                context.interrupt(localContext.getResult())
                break
            }
        }
    }

    fun interpretStatement(statement: StatementContext, context: ExecutionContext) {
        when {
            statement.assignmentStatement() !== null ->
                interpretAssignmentStatement(statement.assignmentStatement(), context)
            statement.expression() !== null ->
                interpretExpression(statement.expression(), context)
            statement.functionDefinition() !== null ->
                interpretFunctionDefinition(statement.functionDefinition(), context)
            statement.ifStatement() !== null ->
                interpretIfStatement(statement.ifStatement(), context)
            statement.variableDeclaration() !== null ->
                interpretVariableDeclaration(statement.variableDeclaration(), context)
            statement.whileStatement() !== null ->
                interpretWhileStatement(statement.whileStatement(), context)
            statement.returnStatement() !== null ->
                interpretReturnStatement(statement.returnStatement(), context)
            else -> throw UnknownASTNodeException(statement.start.line)
        }
    }

    fun interpretReturnStatement(statement: ReturnStatementContext, context: ExecutionContext) {
        val result = interpretExpression(statement.expression(), context)
        context.interrupt(result)
    }

    fun interpretAssignmentStatement(assignment: AssignmentStatementContext, context: ExecutionContext) {
        val name = assignment.IDENTIFIER().symbol.text
        val expression = assignment.expression()
        val value = if (expression !== null) interpretExpression(expression, context) else 0
        context.setVariable(name, value)
    }

    fun interpretIfStatement(statement: IfStatementContext, context: ExecutionContext) {
        val condition = statement.expression()
        if (interpretExpression(condition, context) != 0) {
            interpretBlock(statement.blockWithBraces(0).block(), context)
        } else if (statement.blockWithBraces(1) != null) {
            interpretBlock(statement.blockWithBraces(1).block(), context)
        }
    }

    fun interpretWhileStatement(loop: WhileStatementContext, context: ExecutionContext) {
        val condition = loop.expression()
        while (interpretExpression(condition, context) != 0) {
            val localContext = ExecutionContext(context)
            interpretBlock(loop.blockWithBraces().block(), localContext)
            if (localContext.isInterrupted()) {
                context.interrupt(localContext.getResult())
                break
            }
        }
    }

    fun interpretVariableDeclaration(declaration: VariableDeclarationContext, context: ExecutionContext) {
        val name = declaration.IDENTIFIER().symbol.text
        val expression = declaration.expression()
        val value = if (expression !== null) interpretExpression(expression, context) else 0
        context.defineVariable(name, value)
    }

    fun interpretFunctionDefinition(definition: FunctionDefinitionContext, context: ExecutionContext) {
        val name = definition.IDENTIFIER().symbol.text
        val argumentsNames = definition.parameterNames().IDENTIFIER()
                .map { it -> it.symbol.text }
                .toList()
        val function = RuntimeFunction(context, definition.blockWithBraces(), argumentsNames)
        context.defineFunction(name, function)
    }
}