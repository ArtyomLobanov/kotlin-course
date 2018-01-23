package ru.spbau.mit.ast

import java.io.*
import java.util.*
import kotlin.coroutines.experimental.*


private val trueConstant = Literal(1, 0)

class Debugger(private val output: PrintStream) {
    private var root: ASTree? = null
    private var listener: DebugListener? = null
    private val breakpointsConditions = HashMap<Int, Expression>()
    private val breakpointsDescriptions = HashMap<Int, String>()

    suspend fun run(input: InputStream) {
        val reader = BufferedReader(InputStreamReader(input))
        while (true) {
            try {
                output.print(prefix())
                val line = reader.readLine() ?: break
                val scanner = Scanner(line)
                if (!scanner.hasNext()) {
                    output.println("Warning: empty command ignored")
                    continue
                }
                when (scanner.next()) {
                    "load" -> load(scanner.nextLine().trim())
                    "breakpoint" -> breakpoint(scanner.nextInt())
                    "condition" -> condition(scanner.nextInt(), scanner.nextLine().trim())
                    "list" -> list()
                    "remove" -> remove(scanner.nextInt())
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

    private suspend fun load(filename: String) {
        root = parseFile(filename)
        breakpointsConditions.clear()
        breakpointsDescriptions.clear()
        output.println("Program loaded.")
    }

    private suspend fun list() {
        output.println("List of breakpoints:")
        breakpointsDescriptions.entries
                .sortedBy { e -> e.key }
                .forEach { e ->
                    output.println("   At line ${e.key}, condition: ${e.value}")
                }
        output.println()
    }

    private suspend fun stop() {
        listener = null
    }

    private suspend fun resume() = listener?.continuation?.resume(Unit)
            ?: output.println("Error: there is nothing to continue")

    private suspend fun start() {
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

    private suspend fun evaluate(code: String) = when {
        listener != null -> {
            val expression = parseExpression(code)
            val result = evaluate(expression, listener!!.context!!)
            output.println("=$result")
        }
        else -> output.println("Error: command isn't available now - run any program first")
    }

    private suspend fun remove(line: Int) {
        if (!breakpointsConditions.containsKey(line)) {
            output.println("Warning: there is no breakpoints on line $line")
        } else {
            breakpointsConditions.remove(line)
            breakpointsDescriptions.remove(line)
        }
    }

    private suspend fun breakpoint(line: Int) {
        if (breakpointsConditions.containsKey(line)) {
            output.println("Warning: breakpoint at line $line was overwritten")
        }
        breakpointsConditions[line] = trueConstant
        breakpointsDescriptions[line] = "empty"
    }

    private suspend fun condition(line: Int, condition: String) {
        if (condition.isEmpty()) {
            output.println("Error: condition expression is missed")
            return
        }
        val expression = parseExpression(condition)
        if (breakpointsConditions.containsKey(line)) {
            output.println("Warning: breakpoint at line $line was overwritten")
        }
        breakpointsConditions[line] = expression
        breakpointsDescriptions[line] = condition
    }

    private fun prefix(): String = when (listener) {
        null -> ">"
        else -> "line=${listener?.node?.line},elementType=${listener?.node!!.javaClass.simpleName}>"
    }

    private inner class DebugListener : ExecutionListener {
        var context: ExecutionContext? = null
        var continuation: Continuation<Unit>? = null
        var node: ASTNode? = null

        override suspend fun visitNode(node: ASTNode, context: ExecutionContext) {
            val condition = breakpointsConditions[node.line] ?: return
            if (evaluate(condition, context) == 0) return
            suspendCoroutine<Unit> { continuation ->
                this.context = context
                this.continuation = continuation
                this.node = node
            }
        }

        override suspend fun executionFinished() {
            listener = null
        }
    }
}
