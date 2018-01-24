package ru.spbau.mit.ast

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode
import ru.spbau.mit.parser.FunLanguageLexer
import ru.spbau.mit.parser.FunLanguageParser
import ru.spbau.mit.parser.FunLanguageParser.*
import ru.spbau.mit.parser.FunLanguageVisitor

object ASTBuilder : FunLanguageVisitor<ASTNode> {

    fun buildAST(stream: CharStream): ASTree {
        val lexer = FunLanguageLexer(stream)
        val parser = FunLanguageParser(BufferedTokenStream(lexer))
        return ASTree(parser.file().accept(this) as File)
    }


    override fun visitFile(ctx: FileContext): File {
        if (ctx.block() === null) {
            throw SyntaxException(ctx.start.line)
        }
        return File(ctx.block().accept(this) as Block, ctx.start.line)
    }

    override fun visitBlock(ctx: BlockContext): Block {
        val statements = ctx.statement().map { it.accept(this) as Statement }
        return Block(statements, ctx.start.line)
    }

    override fun visitBlockWithBraces(ctx: BlockWithBracesContext): Block {
        return ctx.block().accept(this) as Block
    }

    override fun visitStatement(ctx: StatementContext): Statement {
        if (ctx.childCount != 1) {
            throw SyntaxException(ctx.start.line)
        }
        return ctx.children.first().accept(this) as Statement
    }

    override fun visitFunctionDefinition(ctx: FunctionDefinitionContext): FunctionDefinition {
        val name = ctx.IDENTIFIER().symbol.text
        val argumentsNames = ctx.parameterNames().IDENTIFIER()
                .map { it -> it.symbol.text }
                .toList()
        val body = ctx.blockWithBraces().block().accept(this) as Block
        return FunctionDefinition(name, body, argumentsNames, ctx.start.line)
    }

    override fun visitVariableDeclaration(ctx: VariableDeclarationContext): VariableDeclaration {
        val name = ctx.IDENTIFIER().symbol.text
        val expression = ctx.expression()?.accept(this) as Expression?
        return VariableDeclaration(name, expression, ctx.start.line)
    }

    override fun visitWhileStatement(ctx: WhileStatementContext): WhileStatement {
        val condition = ctx.expression().accept(this) as Expression
        val body = ctx.blockWithBraces().accept(this) as Block
        return WhileStatement(condition, body, ctx.start.line)
    }

    override fun visitIfStatement(ctx: IfStatementContext): IfStatement {
        val condition = ctx.expression().accept(this) as Expression
        val body = ctx.blockWithBraces(0).accept(this) as Block
        val elseBody = ctx.blockWithBraces(1)?.block()?.accept(this) as Block?
        return IfStatement(condition, body, elseBody, ctx.start.line)
    }

    override fun visitAssignmentStatement(ctx: AssignmentStatementContext): AssignmentStatement {
        val name = ctx.IDENTIFIER().symbol.text
        val expression = ctx.expression().accept(this) as Expression
        return AssignmentStatement(name, expression, ctx.start.line)
    }

    override fun visit(tree: ParseTree): File {
        if (tree.childCount != 1) {
            throw SyntaxException(0)
        }
        return tree.getChild(0).accept(this) as File
    }

    override fun visitReturnStatement(ctx: ReturnStatementContext): ReturnStatement {
        val expression = ctx.expression().accept(this) as Expression
        return ReturnStatement(expression, ctx.start.line)
    }

    override fun visitFunctionCall(ctx: FunctionCallContext): FunctionCall {
        val name = ctx.IDENTIFIER().symbol.text
        val arguments = ctx.arguments().expression().map { it.accept(this) as Expression }
        return FunctionCall(name, arguments, ctx.start.line)
    }

    override fun visitExpression(ctx: ExpressionContext): Expression {
        if (ctx.primitiveExpression() !== null) {
            return ctx.primitiveExpression().accept(this) as Expression
        }
        val leftValue = ctx.expression(0).accept(this) as Expression
        val rightValue = ctx.expression(1).accept(this) as Expression
        val operator = when (ctx.op.type) {
            FunLanguageParser.MULTIPLY -> Operator.Multiply
            FunLanguageParser.DIVIDE -> Operator.Divide
            FunLanguageParser.REMAINDER -> Operator.Remainder
            FunLanguageParser.PLUS -> Operator.Plus
            FunLanguageParser.MINUS -> Operator.Minus
            FunLanguageParser.GREATER -> Operator.Greater
            FunLanguageParser.LESS -> Operator.Less
            FunLanguageParser.GREATER_OR_EQUAL -> Operator.GreaterOrEqual
            FunLanguageParser.LESS_OR_EQUAL -> Operator.LessOrEqual
            FunLanguageParser.EQUAL -> Operator.Equal
            FunLanguageParser.NOT_EQUAL -> Operator.NotEqual
            FunLanguageParser.LOGICAL_OR -> Operator.Or
            FunLanguageParser.LOGICAL_AND -> Operator.And
            else -> throw SyntaxException(ctx.start.line)
        }
        return BinaryExpression(leftValue, rightValue, operator, ctx.start.line)
    }

    override fun visitPrimitiveExpression(ctx: PrimitiveExpressionContext): Expression {
        return when {
            ctx.childCount == 1 -> ctx.children.first().accept(this) as Expression
            ctx.childCount == 3 -> ctx.expression().accept(this) as Expression
            else -> throw SyntaxException(ctx.start.line)
        }
    }

    override fun visitTerminal(node: TerminalNode): ASTNode {
        val text = node.symbol.text
        return if (text[0].isDigit()) {
            Literal(text.toInt(), node.symbol.line)
        } else {
            Identifier(text, node.symbol.line)
        }
    }

    // should be never called (parse arguments in visitFunctionCall)
    override fun visitArguments(ctx: ArgumentsContext): ASTNode? {
        throw SyntaxException(ctx.start.line)
    }

    // should be never called (parse parameter names in visitFunctionDefinition)
    override fun visitParameterNames(ctx: ParameterNamesContext): ASTNode {
        throw SyntaxException(ctx.start.line)
    }

    override fun visitErrorNode(node: ErrorNode): ASTNode? {
        throw SyntaxException(node.symbol.line)
    }

    override fun visitChildren(node: RuleNode): ASTNode? {
        throw SyntaxException(node.sourceInterval.a)
    }
}
