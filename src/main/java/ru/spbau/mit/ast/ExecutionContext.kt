package ru.spbau.mit.ast

class ExecutionContext(private val parent: ExecutionContext?) {
    private val variables: MutableMap<String, Int> = HashMap()
    private val functions: MutableMap<String, Function> = HashMap()
    private var resultCode = 0
    private var isInterrupted = false

    fun defineFunction(name: String, function: Function): Boolean {
        if (!functions.containsKey(name)) {
            functions[name] = function
            return true
        }
        return false
    }

    fun defineVariable(name: String, value: Int): Boolean {
        if (!variables.containsKey(name)) {
            variables[name] = value
            return true
        }
        return false
    }

    fun getFunction(name: String): Function? = functions[name] ?: parent?.getFunction(name)
    fun getVariable(name: String): Int? = variables[name] ?: parent?.getVariable(name)

    fun setVariable(name: String, value: Int): Boolean {
        return when {
            variables.containsKey(name) -> {
                variables[name] = value
                return true
            }
            else -> parent?.setVariable(name, value) ?: false
        }
    }

    fun interrupt(resultCode: Int) {
        this.resultCode = resultCode
        isInterrupted = true
    }

    fun getResult() = resultCode
    fun isInterrupted() = isInterrupted
}
