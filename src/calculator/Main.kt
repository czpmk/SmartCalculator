package calculator

import java.lang.NumberFormatException
import java.util.*
import kotlin.math.pow

val scanner = Scanner(System.`in`)
val mathSymbols = arrayOf("+", "-", "*", "/", "^", "(", ")", "=")
const val helpMessage = "The program is a calculator that can return results of basic\n" +
        "calculations and keep in memory previous inputs, commands, and results.\n" +
        "Supported action: addition, subtraction.\n" +
        "Type /exit to exit."

class MathLine {
    private var inputString = ""
    private var inputList = mutableListOf<String>()
    private var interpretedList = mutableListOf<Boolean>()
    var typeList = mutableListOf<String>()
    val value: MutableList<String>
        get() {
            return inputList
        }
    val correctSyntax: Boolean
        get() {
            return false !in interpretedList
        }

    /** Reads new MathLine with a scanner */
    fun readLine() {
        inputString = scanner.nextLine()
        val tempList = inputString.split(" ").filter { arg -> arg.isNotEmpty() }
        inputList = tempList.toMutableList()

        splitByMathSigns()
        initializeTypeMaps()
        interpretLine()

        printStringList(inputList)
        printStringList(typeList)
        printBooleanList(interpretedList)
    }

    /** Replaces elements with a corresponding result */
    fun update(startIdx: Int, lastIdx: Int, toReplace: String) {
        var tempInputList = mutableListOf<String>()
        for (idx in inputList.indices) {

        }
    }

    private fun splitString(stringToSplit: String): MutableList<String> {
        val splitChars = arrayOf('*', '/', '^', '(', ')', '=')
        var tempString = stringToSplit
        val listOfStrings = mutableListOf<String>()
        for (idx in stringToSplit.indices) {
            if (stringToSplit[idx] in splitChars) {
                if (tempString.substringBefore(stringToSplit[idx]).isNotEmpty()) {
                    listOfStrings.add(tempString.substringBefore(stringToSplit[idx]))
                }
                listOfStrings.add(stringToSplit[idx].toString())
                tempString = tempString.substringAfter(stringToSplit[idx])
            }
        }
        if (tempString.isNotEmpty()) listOfStrings.add(tempString)
        return listOfStrings
    }

    /** Splits each string in inputLine if math symbol (except + and -) is found*/
    private fun splitByMathSigns() {
        val tempList = mutableListOf<String>()
        for (stringIdx in inputList.indices) {
            if (inputList[stringIdx].length > 1) {
                for (arg in splitString(inputList[stringIdx])) {
                    tempList.add(arg)
                }
            } else {
                tempList.add(inputList[stringIdx])
            }
        }
        inputList = tempList
    }

    /** Initializes interpretedList and typeList with same size as inputList and default values */
    private fun initializeTypeMaps() {
        val sizeOfArray = inputList.size
        interpretedList = MutableList(sizeOfArray) { false }
        typeList = MutableList(sizeOfArray) { "" }
    }

    private fun isLiteral(idx: Int): Boolean {
        return try {
            inputList[idx].toDouble()
            interpretedList[idx] = true
            typeList[idx] = "LITERAL"
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun multiplePlusOrMinus(idx: Int): Boolean {
        val tempString = inputList[idx]
        val firstChar = tempString[0]
        if (firstChar == '+' || firstChar == '-') {
            for (i in 1 until tempString.length) {
                if (tempString[i] != firstChar) {
                    return false
                }
            }
            inputList[idx] = if (tempString.length % 2 == 1 && firstChar == '-') {
                "-"
            } else {
                "+"
            }
            return true
        }
        return false
    }

    private fun isMathSymbol(idx: Int): Boolean {
        if (inputList[idx] in mathSymbols) {
            interpretedList[idx] = true
            typeList[idx] = "MATH"
            return true
        } else {
            if (multiplePlusOrMinus(idx)) {
                interpretedList[idx] = true
                typeList[idx] = "MATH"
                return true
            }
            return false
        }
    }

    private fun isVariable(idx: Int): Boolean {
        for (character in inputList[idx]) {
            if (character !in 'a'..'z' && character !in 'A'..'Z') {
                return false
            }
        }
        interpretedList[idx] = true
        typeList[idx] = "VARIABLE"
        return true
    }

    /** Analyzes each string in inputList, assigns corresponding type in typeList and sets true
     * in interpretedList if type is correct */
    private fun interpretLine() {
        arrayLoop@ for (idx in inputList.indices) {
            when {
                isLiteral(idx) -> continue@arrayLoop
                isVariable(idx) -> continue@arrayLoop
                isMathSymbol(idx) -> continue@arrayLoop
            }
        }
    }

    /** DEV */
    private fun printStringList(listToPrint: MutableList<String>) {
        for (i in listToPrint) {
            print("'$i' ")
        }
        println()
    }

    /** DEV */
    private fun printBooleanList(listToPrint: MutableList<Boolean>) {
        for (i in listToPrint) {
            print("'$i' ")
        }
        println()
    }

    /** Contains each initialized variable and its value */
//    companion object Archives {
//        val variables = mutableMapOf<String, Double>()
//    }
}

object Calculator {
    private var exitCommand = false

    private fun isCommand(line: MathLine): Boolean {
        if (line.value[0] == "/") {
            if (line.value.size == 2) {
                when (line.value[1]) {
                    "exit" -> {
                        exitCommand = true
                        println("Bye!")
                    }
                    "help" -> println(helpMessage)
                    else -> println("Unknown command")
                }
            } else {
                println("Unknown command")
            }
            return true
        } else {
            return false
        }
    }

    private fun basicValidation(line: MathLine): Boolean {
        when {
            line.value.isEmpty() -> return false
            !line.correctSyntax -> return false
            line.value.count { i -> i == "=" } > 1 -> {
                println("Forbidden multiple assignment")
                return false
            }
            line.value.count { i -> i == "(" } != line.value.count { i -> i == ")" } -> {
                println("Invalid set of brackets")
                return false
            }
            isCommand(line) -> return false
        }
        return true
    }

    private fun expressionValidation(line: MathLine, startIdx: Int, lastIdx: Int): Boolean {
        if (startIdx !in line.value.indices || lastIdx !in line.value.indices) {
            println("Index out of bounds")
            return false
        }
        if ((lastIdx - startIdx) % 2 != 0 || (lastIdx - startIdx) < 0) {
            println("Invalid index range or length (must be odd)\n" +
                    "Range: ($startIdx, $lastIdx), Index: (${line.value.indices.first}, ${line.value.indices.last})")
            return false
        }
        for (idx in (startIdx..lastIdx step (2))) {
            if (line.typeList[idx] !in arrayOf("LITERAL", "VARIABLE")) {
                println("Invalid data: ${line.value[idx]} is not a number")
                return false
            }
        }
        return true
    }

    private fun addition(line: MathLine, startIdx: Int = 0, lastIdx: Int = (line.value.size - 1)): Boolean {
        if (!expressionValidation(line, startIdx, lastIdx)) return false
        var result = line.value[startIdx].toDouble()
        for (idx in (startIdx + 2)..lastIdx step (2)) {
            when (line.value[idx - 1]) {
                "+" -> result += line.value[idx].toDouble()
                "-" -> result -= line.value[idx].toDouble()
                else -> {
                    println("Invalid sign: ${line.value[idx - 1]} is not '-' or '+'")
                    return false
                }
            }
        }
        println(result)
        return true
    }

    private fun multiplication(line: MathLine, startIdx: Int = 0, lastIdx: Int = (line.value.size - 1)): Boolean {
        if (!expressionValidation(line, startIdx, lastIdx)) return false
        var result = line.value[startIdx].toDouble()
        for (idx in (startIdx + 2)..lastIdx step (2)) {
            when (line.value[idx - 1]) {
                "*" -> result *= line.value[idx].toDouble()
                "/" -> {
                    if (line.value[idx].toDouble() == 0.0) {
                        println("Division by 0!")
                        return false
                    } else {
                        result /= line.value[idx].toDouble()
                    }
                }
                else -> {
                    println("Invalid sign: ${line.value[idx - 1]} is not '*' or '/'")
                    return false
                }
            }
        }
        println(result)
        return true
    }

    private fun power(line: MathLine, startIdx: Int, lastIdx: Int = startIdx + 2): Boolean {
        if (!expressionValidation(line, startIdx, lastIdx)) return false
        if ((lastIdx - startIdx) != 2) {
            println("Power function only takes 2 arguments (input: ${(lastIdx - startIdx) / 2} arguments)")
        }
        val result: Double
        if (line.value[lastIdx - 1] == "^") {
            result = line.value[startIdx].toDouble().pow(line.value[lastIdx].toDouble())
            println(result)
        }
        return true
    }

    fun next(): Boolean {
        val newCalculation = MathLine()
        newCalculation.readLine()
        if (basicValidation(newCalculation)) {
//            addition(newCalculation)
//            multiplication(newCalculation)
//            power(newCalculation, 0)
            println()
        }
        return exitCommand
    }
}

fun main() {
    while (true) {
        // if returns exitCommand = true
        if (Calculator.next()) break
    }
}