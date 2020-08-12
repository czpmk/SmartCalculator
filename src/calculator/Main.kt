package calculator

import java.util.*

// extensions
fun String.splitWith(delimitersList: List<Char>): MutableList<String> {
    val tempList = mutableListOf<String>()
    var lastAdded = -1
    for (idx in this.indices) {
        if (this[idx] in delimitersList) {
            if (idx != (lastAdded + 1)) {
                tempList.add(this.substring(lastAdded + 1, idx))
            }
            tempList.add(this[idx].toString())
            lastAdded = idx
        }
    }
    if (lastAdded != this.lastIndex) {
        tempList.add(this.substring(lastAdded + 1, this.lastIndex + 1))
    }
    return tempList
}

fun MutableList<String>.lastIdx(value: String, startIdx: Int = 0, lastIdx: Int = this.lastIndex): Int {
    val result = this.subList(startIdx, lastIdx + 1).lastIndexOf(value)
    return if (result == -1) {
        result
    } else {
        result + startIdx
    }
}

fun MutableList<String>.firstIdx(value: String, startIdx: Int = 0, lastIdx: Int = this.lastIndex): Int {
    val result = this.subList(startIdx, lastIdx + 1).indexOf(value)
    return if (result == -1) {
        result
    } else {
        result + startIdx
    }
}

val scanner = Scanner(System.`in`)
val mathSymbols = arrayOf("+", "-", "*", "/", "^", "(", ")", "=")
const val helpMessage = "The program is a calculator that can return results of basic\n" +
        "calculations and keep in memory previous inputs, commands, and results.\n" +
        "Supported action: addition, subtraction.\n" +
        "Type /exit to exit."

data class MathValue(private val _value: String) {
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
        return try {
            newArgument.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
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
            isLiteral(newArgument) -> {
                type = "LITERAL"
                isValid = true
            }
            isVariable(newArgument) -> {
                type = "VARIABLE"
                isValid = true
            }
            isMathSymbol(newArgument) -> {
                type = "MATH-SYMBOL"
                isValid = true
            }
            else -> {
                type = "OTHER"
                isValid = false
            }
        }
    }
}

class MathExpression(private var newLine: String) {
    private var tempList: MutableList<String>
    private val delimitersList = listOf('+', '-', '*', '/', '^', '(', ')', '=')
    var infixExpression = mutableListOf<MathValue>()
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
            newTempList.addAll(tempList[idx].splitWith(delimitersList))
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
                    else -> println("Invalid command")
                }
            } else {
                println("Invalid command")
            }
            true
        } else {
            false
        }
    }

    private fun validateExpression(): Boolean {
        // is it empty
        if (infixExpression.isEmpty()) {
            println("[S] Empty input")
            return true
        }

        // drop parenthesis and check if empty
        val values = infixExpression.filter { i -> i.value != "(" && i.value != ")" }
        if (values.isEmpty()) {
            println("[S] Contains only the parenthesis")
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
                        println("Invalid expression")
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
            println("Invalid expression: expression ends with operator: ${values.last().value}")
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
                        stack.isEmpty() ->
                            stack.add(infixExpression[i])

                        stack.last().value == "(" ->
                            stack.add(infixExpression[i])

                        infixExpression[i].value == "(" ->
                            stack.add(infixExpression[i])

                        infixExpression[i].value == ")" -> {
                            while (stack.last().value != "(") {
                                postfixExpression.add(stack.last())
                                stack.removeAt(stack.lastIndex)
                            }
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
            if (stack.last().value !in listOf("(", ")")) {
                postfixExpression.add(stack.last())
            }
            stack.removeAt(stack.lastIndex)
        }
    }
}

object Calculator {
    fun next(): Boolean {
        val nextExpression = MathExpression(scanner.nextLine())
        if (!nextExpression.isCommand) {
            if (nextExpression.isValid) {
                solve(nextExpression.postfixExpression)
//                for (i in nextExpression.infixExpression) {
//                    print("${i.value} ")
//                }
//                println()
//                for (i in nextExpression.postfixExpression) {
//                    print("${i.value} ")
//                }
            }
        }
        return nextExpression.exitCode
    }

    fun solve(expression: MutableList<MathValue>) {

    }
}

fun main() {
    while (true) {
        if (Calculator.next()) break
    }
}
