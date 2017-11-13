package ru.spbau.mit.interpreter

class ExecutionContext(private val parent: ExecutionContext?) {
    private val variables: MutableMap<String, Int> = HashMap()
    private val functions: MutableMap<String, Function> = HashMap()
    private var resultCode = 0
    private var isInterrupted = false

    fun defineFunction(name: String, function: Function) {
        if (functions.containsKey(name)) {
            throw RuntimeException()
        }
        functions[name] = function
    }

    fun defineVariable(name: String, value: Int) {
        if (variables.containsKey(name)) {
            throw RuntimeException()
        }
        variables[name] = value
    }

    fun getFunction(name: String): Function? = functions[name] ?: parent?.getFunction(name)
    fun getVariable(name: String): Int? = variables[name] ?: parent?.getVariable(name)

    fun setVariable(name: String, value: Int): Int? {
        return when {
            variables.containsKey(name) -> variables.replace(name, value)
            else -> parent?.setVariable(name, value)
        }
    }

    fun interrupt(resultCode: Int) {
        this.resultCode = resultCode
        isInterrupted = true
    }

    fun getResult() = resultCode
    fun isInterrupted() = isInterrupted
}
