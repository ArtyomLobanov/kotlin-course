package ru.spbau.mit.ast

import kotlinx.coroutines.experimental.runBlocking
import java.io.*
import java.util.*
import kotlin.coroutines.experimental.*


private val trueConstant = Literal(1, 0)

class Debugger(private val output: PrintStream) {
    private var root: ASTree? = null
    private var listener: DebugListener? = null
    private val breakpointsConditions = HashMap<Int, Expression>()
    private val breakpointsDescriptions = HashMap<Int, String>()

    fun run(input: InputStream) {
        val reader = BufferedReader(InputStreamReader(input))
        while (true) {
            try {
                output.print(generatePrefix())
                val line = reader.readLine() ?: break
                val scanner = Scanner(line)
                if (!scanner.hasNext()) {
                    output.println("Warning: empty command ignored")
                    continue
                }
                when (scanner.next()) {
                    "load" -> load(scanner.nextLine().trim())
                    "breakpoint" -> addBreakpoint(scanner.nextInt())
                    "condition" -> addBreakpointWithCondition(scanner.nextInt(), scanner.nextLine().trim())
                    "list" -> printBreakpoints()
                    "remove" -> removeBreakpoint(scanner.nextInt())
                    "run" -> start()
                    "evaluate" -> evaluate(scanner.nextLine())
                    "stop" -> stop()
                    "continue" -> resume()
                }
                if (scanner.hasNext()) {
                    output.println("Warning: Extra arguments were ignored")
                }
            } catch (e: NoSuchElementException) {
                output.println("Error: some arguments missed")
            } catch (e: InputMismatchException) {
                output.println("Error: wrong types of arguments")
            } catch (e: FileNotFoundException) {
                output.println("Error: file wasn't found")
            } catch (e: Exception) {
                output.println("Error(${e.javaClass.canonicalName}): ${e.message}")
            }
        }
    }

    private fun load(filename: String) {
        root = parseFile(filename)
        breakpointsConditions.clear()
        breakpointsDescriptions.clear()
        output.println("Program loaded.")
    }

    private fun printBreakpoints() {
        output.println("List of breakpoints:")
        breakpointsDescriptions.entries
                .sortedBy { e -> e.key }
                .forEach { e ->
                    output.println("   At line ${e.key}, condition: ${e.value}")
                }
        output.println()
    }

    private fun stop() {
        listener = null
    }

    private fun resume() = listener?.state?.continuation?.resume(Unit)
            ?: output.println("Error: there is nothing to continue")

    private fun start() {
        listener = DebugListener()
        val action: suspend () -> Unit = {
            evaluate(root!!, output, listener)
        }
        action.startCoroutine(object : Continuation<Unit> {
            override val context: CoroutineContext = EmptyCoroutineContext

            override fun resume(value: Unit) {}

            override fun resumeWithException(exception: Throwable) {}
        })
    }

    private fun evaluate(code: String) = listener?.let {
        val expression = parseExpression(code)
        val result = runBlocking {
            evaluate(expression, listener!!.state!!.context)
        }
        output.println("=$result")
    } ?: output.println("Error: command isn't available now - run any program first")

    private fun removeBreakpoint(line: Int) {
        if (line !in breakpointsConditions) {
            output.println("Warning: there is no breakpoints on line $line")
        } else {
            breakpointsConditions.remove(line)
            breakpointsDescriptions.remove(line)
        }
    }

    private fun addBreakpoint(line: Int) {
        if (line in breakpointsConditions) {
            output.println("Warning: breakpoint at line $line was overwritten")
        }
        breakpointsConditions[line] = trueConstant
        breakpointsDescriptions[line] = "empty"
    }

    private fun addBreakpointWithCondition(line: Int, condition: String) {
        if (condition.isEmpty()) {
            output.println("Error: condition expression is missed")
            return
        }
        val expression = parseExpression(condition)
        if (line in breakpointsConditions) {
            output.println("Warning: breakpoint at line $line was overwritten")
        }
        breakpointsConditions[line] = expression
        breakpointsDescriptions[line] = condition
    }

    private fun generatePrefix(): String = listener?.let {
        "line=${listener!!.state!!.node.line},elementType=${listener!!.state!!.node.javaClass.simpleName}>"
    } ?: ">"

    private inner class DebugListener : ExecutionListener {
        var state: ExecutionState? = null

        override suspend fun visitNode(node: ASTNode, context: ExecutionContext) {
            val condition = breakpointsConditions[node.line] ?: return
            if (evaluate(condition, context) == 0) return
            suspendCoroutine<Unit> { continuation ->
                state = ExecutionState(context, continuation, node)
            }
        }

        override suspend fun executionFinished() {
            listener = null
        }
    }

    private data class ExecutionState(
            val context: ExecutionContext,
            val continuation: Continuation<Unit>,
            val node: ASTNode
    )
}