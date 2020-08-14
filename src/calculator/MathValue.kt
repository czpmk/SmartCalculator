package calculator

data class MathValue(private val _value: String) {
    private val mathSymbols = listOf("+", "-", "*", "/", "^", "(", ")", "=")
    lateinit var type: String
    var isValid: Boolean = false
    var value: String = ""
        set(newValue) {
            interpretArguments(newValue)
            field = newValue
        }

    init {
        value = _value
    }

    private fun isLiteral(newArgument: String): Boolean {
        if (newArgument[0] in '0'..'9' || newArgument[0] == '-' && newArgument.length > 1) {
            for (i in 1 until newArgument.length) {
                if (newArgument[i] !in '0'..'9') {
                    return false
                }
            }
        } else {
            return false
        }
        return true
    }

    private fun isMathSymbol(newArgument: String): Boolean {
        return newArgument in mathSymbols
    }

    private fun isVariable(newArgument: String): Boolean {
        for (character in newArgument) {
            if (character !in 'a'..'z' && character !in 'A'..'Z') {
                return false
            }
        }
        return true
    }

    private fun interpretArguments(newArgument: String) {
        when {
            isMathSymbol(newArgument) -> {
                type = "MATH-SYMBOL"
                isValid = true
            }
            isLiteral(newArgument) -> {
                type = "LITERAL"
                isValid = true
            }
            isVariable(newArgument) -> {
                type = "VARIABLE"
                isValid = true
            }
            else -> {
                type = "OTHER"
                isValid = false
            }
        }
    }
}
