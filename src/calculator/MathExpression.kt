package calculator

class MathExpression(private var newLine: String) {
    private val mathSymbols = listOf("+", "-", "*", "/", "^", "(", ")", "=")
    private val helpMessage = "# SmartCalculator\n" +
            "    Project created with the support of JetBrains Academy as an introduction to Kotlin.\n" +
            "created by: Michał Czapiewski\n" +
            "email: czapiewskimk@gmail.com\n" +
            "github: https://github.com/czpmk\n" +
            "# Purpose\n" +
            "    The program returns results of basic calculations using integer numbers. It supports \n" +
            "expression solving, (e.g. \"5 * (2^n) + (-1)^3\"), assignment operations (e.g. \"a = 5 + n*b\") \n" +
            "and keeps initialized variables in memory. Those can be invoked by typing their name in \n" +
            "an expression.\n" +
            "# Available operations:\n" +
            "(operation; operator)\n" +
            "- addition: \"+\"\n" +
            "- subtraction: \"-\" \n" +
            "- multiplication: \"*\"\n" +
            "- division: \"/\"\n" +
            "- exponentietion: \"^\"\n" +
            "- assignment: \"=\"\n" +
            "- brackets: \"(\", \")\"\n" +
            "# Limitations\n" +
            "    Arguments maximum (minimum) size is not clearly specified. Operations on big Integer \n" +
            "numbers are supported, although recommended maximum (minimum) size is: 1^18 (1^-18).\n" +
            "    A number preceded by a \"-\" symbol will only be interpreted as a negative value when\n" +
            "it is the first argument in an expression or follows \"=\" or \"(\" symbol. It's recommended to \n" +
            "always enclose negative numbers in parentheses.\n" +
            "    Multiple \"+\" or \"-\" symbols are supported, and interpreted as follows: \"+++...+\" = \"+\"\n" +
            "\"---...-\" = \"-\" if there is an odd number of \"-\" symbols, and \"+\" if it is even.\n" +
            "# Commands\n" +
            "    Following commands can be invoked while the program is running. Please, use\n" +
            "those in a new line.\n" +
            "\"/exit\" - terminate the program\n" +
            "\"/help\" - display a help message"
    private var tempList: MutableList<String>
    private var infixExpression = mutableListOf<MathValue>()
    var postfixExpression = mutableListOf<MathValue>()
    var isCommand = false
    var isValid = false
    var exitCode = false

    init {
        // converts directly in newLine string
        convertMultiplePlus()
        convertMultipleMinus()

        // creates temporary list while splitting by whitespace
        tempList = newLine.split(" ").filter { arg -> arg.isNotEmpty() }.toMutableList()

        //splits by math symbol, but does not delete them
        splitByMythSymbol()

        // checks if - before a number is not operation but part of number
        catchNegativeNumbers()

        toInfixExpression()

        isCommand = catchCommand()
        if (!isCommand) {
            isValid = validateExpression()
            if (isValid) {
                toPostfixExpression()
            }
        }
    }

    private fun convertMultiplePlus() {
        var newTempLine = ""
        var numOfSameSigns = 0
        for (idx in newLine.indices) {
            if (newLine[idx] == '+') {
                numOfSameSigns++
            } else {
                if (numOfSameSigns != 0) {
                    newTempLine += '+'
                }
                newTempLine += newLine[idx]
                numOfSameSigns = 0
            }
        }
        if (numOfSameSigns != 0) {
            newTempLine += '+'
        }
        newLine = newTempLine
    }

    private fun convertMultipleMinus() {
        var newTempLine = ""
        var numOfSameSigns = 0
        for (idx in newLine.indices) {
            if (newLine[idx] == '-') {
                numOfSameSigns++
            } else {
                if (numOfSameSigns != 0) {
                    newTempLine += if (numOfSameSigns % 2 == 0) {
                        '+'
                    } else {
                        '-'
                    }
                }
                newTempLine += newLine[idx]
                numOfSameSigns = 0
            }
        }
        if (numOfSameSigns != 0) {
            newTempLine += if (numOfSameSigns % 2 == 0) {
                '+'
            } else {
                '-'
            }
        }
        newLine = newTempLine
    }

    private fun splitByMythSymbol() {
        val newTempList = mutableListOf<String>()
        for (idx in tempList.indices) {
            newTempList.addAll(tempList[idx].splitWith(mathSymbols))
        }
        tempList = newTempList
    }

    private fun catchNegativeNumbers() {
        for (idx in tempList.indices) {
            if (tempList[idx] == "-") {
                if (idx == 0 || tempList[idx - 1] in listOf("(", "=")) {
                    if (idx != tempList.lastIndex) {
                        tempList[idx] = tempList[idx] + tempList[idx + 1]
                        tempList[idx + 1] = ""
                    }
                }
            }
        }
        tempList = tempList.filter { i -> i.isNotEmpty() }.toMutableList()
    }

    private fun catchCommand(): Boolean {
        return if (tempList.isNotEmpty() && tempList[0] == "/") {
            if (tempList.size == 2) {
                when (tempList[1]) {
                    "exit" -> {
                        exitCode = true
                        println("Bye!")
                    }
                    "help" -> println(helpMessage)
                    else -> println("Unknown command. Type /help to display help message")
                }
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    private fun validateExpression(): Boolean {
        // is it empty
        if (infixExpression.isEmpty()) {
            return true
        }

        // drop parenthesis and check if empty
        val values = infixExpression.filter { i -> i.value != "(" && i.value != ")" }
        if (values.isEmpty()) {
            return true
        }

        // contains only valid arguments
        for (arg in infixExpression) {
            if (!arg.isValid) {
                println("Invalid argument: ${arg.value}")
                return false
            }
        }

        // do every math symbol comes after a literal or variable
        var wasLastValueNumeric = false
        for (i in values.indices) {
            when (wasLastValueNumeric) {
                true -> {
                    if (values[i].type == "MATH-SYMBOL") {
                        wasLastValueNumeric = false
                    } else {
                        println("Invalid expression (order)")
                        return false
                    }
                }
                false -> {
                    if (values[i].type == "LITERAL" || values[i].type == "VARIABLE") {
                        wasLastValueNumeric = true
                    } else {
                        println("Invalid expression")
                        return false
                    }
                }
            }
        }

        // does the expression ends with literal
        if (values.last().type != "LITERAL" && values.last().type != "VARIABLE") {
            println("Invalid expression: no operands after: ${values.last().value}")
            return false
        }

        // is "=" in forbidden place
        for (i in (0..(infixExpression.lastIndex)).drop(2)) {
            if (infixExpression[i].value == "=") {
                println("Invalid assignment")
                return false
            }
        }

        // brackets
        val brackets = tempList.filter { i -> i == "(" || i == ")" }.toMutableList()
        while (brackets.isNotEmpty()) {
            val lastOpenIdx = brackets.lastIdx("(")
            if (lastOpenIdx == -1) {
                println("Invalid brackets")
                return false
            }
            val lastCloseIdx = brackets.firstIdx(")", lastOpenIdx)
            if (lastCloseIdx == -1) {
                println("Invalid brackets")
                return false
            }
            brackets.removeAt(lastCloseIdx)
            brackets.removeAt(lastOpenIdx)
        }
        return true
    }

    private fun precedence(argument: MathValue): Int {
        return when (argument.value) {
            "+" -> 1
            "-" -> 1
            "*" -> 2
            "/" -> 2
            "^" -> 3
            else -> 0
        }
    }

    private fun toInfixExpression() {
        for (i in tempList) {
            infixExpression.add(MathValue(i))
        }
    }

    private fun toPostfixExpression() {
        val stack = mutableListOf<MathValue>()
        for (i in infixExpression.indices) {
            when (infixExpression[i].type) {
                "LITERAL" -> postfixExpression.add(infixExpression[i])
                "VARIABLE" -> postfixExpression.add(infixExpression[i])
                "MATH-SYMBOL" -> {
                    when {
                        stack.isEmpty() || stack.last().value == "(" ->
                            stack.add(infixExpression[i])

                        infixExpression[i].value == "(" ->
                            stack.add(infixExpression[i])

                        infixExpression[i].value == ")" -> {
                            while (stack.last().value != "(") {
                                postfixExpression.add(stack.last())
                                stack.removeAt(stack.lastIndex)
                            }
                            stack.removeAt(stack.lastIndex)
                        }
                        precedence(infixExpression[i]) > precedence(stack.last()) ->
                            stack.add(infixExpression[i])

                        precedence(infixExpression[i]) <= precedence(stack.last()) -> {
                            precedenceLoop@ while (stack.isNotEmpty()) {
                                if (stack.last().value == "(") break@precedenceLoop
                                if (precedence(infixExpression[i]) > precedence(stack.last())) break@precedenceLoop
                                postfixExpression.add(stack.last())
                                stack.removeAt(stack.lastIndex)
                            }
                            stack.add(infixExpression[i])
                        }
                    }
                }
            }
        }
        while (stack.isNotEmpty()) {
            if (stack.last().value !in listOf("(", ")"))
                postfixExpression.add(stack.last())
            stack.removeAt(stack.lastIndex)
        }
    }
}
