package calculator

import java.util.*
import kotlin.math.pow

val scanner = Scanner(System.`in`)
val mathSymbols = arrayOf("+", "-", "*", "/", "^", "(", ")", "=")
const val helpMessage = "The program is a calculator that can return results of basic\n" +
        "calculations and keep in memory previous inputs, commands, and results.\n" +
        "Supported action: addition, subtraction.\n" +
        "Type /exit to exit."

// extensions
fun MutableList<String>.replaceByIdx(toReplace: String, startIdx: Int, lastIdx: Int): MutableList<String> {
    this[startIdx] = toReplace
    return this.filterIndexed { index: Int, _: String -> index !in (startIdx + 1)..(lastIdx) }.toMutableList()
}

fun MutableList<Boolean>.replaceByIdx(toReplace: Boolean, startIdx: Int, lastIdx: Int): MutableList<Boolean> {
    this[startIdx] = toReplace
    return this.filterIndexed { index: Int, _: Boolean -> index !in (startIdx + 1)..(lastIdx) }.toMutableList()
}

fun MutableList<String>.firstIdx(value: String, startIdx: Int = 0, lastIdx: Int = this.lastIndex): Int {
    val result = this.subList(startIdx, lastIdx + 1).indexOf(value)
    return if (result == -1) {
        result
    } else {
        result + startIdx
    }
}

fun MutableList<String>.lastIdx(value: String, startIdx: Int = 0, lastIdx: Int = this.lastIndex): Int {
    val result = this.subList(startIdx, lastIdx + 1).lastIndexOf(value)
    return if (result == -1) {
        result
    } else {
        result + startIdx
    }
}


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
        interpretArguments()
    }

    /** Replaces elements with a corresponding result */
    fun update(toReplace: String, startIdx: Int, lastIdx: Int = -1) {
        if (lastIdx == -1) {
            inputList[startIdx] = toReplace
            interpretedList[startIdx] = false
            typeList[startIdx] = ""
        } else {
            inputList = inputList.replaceByIdx(toReplace, startIdx, lastIdx)
            interpretedList = interpretedList.replaceByIdx(false, startIdx, lastIdx)
            typeList = typeList.replaceByIdx("", startIdx, lastIdx)
        }
        interpretArguments()
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

    /** returns last index in expression, or last before ")" symbol */
    fun getLastIdx(startIdx: Int): Int {
        return if (")" in value.subList(startIdx, value.size)) {
            // -1 because an expression ends before ")" symbol, not at it
            value.firstIdx(")", startIdx, value.lastIndex) - 1
        } else {
            value.lastIndex
        }
    }

    /** returns index of last bracket in expression, or last after start index if specified */
    fun findBrackets(startIdx: Int = 0): Pair<Boolean, Int> {
        val openBracketsIdx = value.lastIdx("(", startIdx)
        return if (openBracketsIdx == -1) {
            Pair(false, openBracketsIdx)
        } else {
            Pair(true, openBracketsIdx)
        }
    }

    /** Analyzes each string in inputList, assigns corresponding type in typeList and sets true
     * in interpretedList if type is correct */
    private fun interpretArguments() {
        arrayLoop@ for (idx in inputList.indices) {
            if (!interpretedList[idx]) {
                when {
                    isLiteral(idx) -> continue@arrayLoop
                    isVariable(idx) -> continue@arrayLoop
                    isMathSymbol(idx) -> continue@arrayLoop
                }
            }
        }
    }

    /** Contains each initialized variable and its value */
    companion object Archives {
        private var variables = mutableMapOf<String, Double>()

        fun containsVar(name: String): Boolean {
            return variables.containsKey(name)
        }

        fun updateVar(name: String, value: Double) {
            if (variables.containsKey(name)) {
                variables.replace(name, value)
            } else {
                variables[name] = value
            }
        }

        fun readVar(name: String): Double {
            return variables.getValue(name)
        }
    }
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

    private fun isAssignment(line: MathLine): String {
        return when {
            "=" !in line.value -> "NO ASSIGNMENT"
            else -> "ASSIGNMENT"
        }
    }

    private fun isCorrectExpression(line: MathLine, startIdx: Int): Boolean {
        val values = line.value.subList(startIdx, line.value.lastIndex + 1)
        val brackets = values.filter { i -> i == "(" || i == ")" }.toMutableList()

        var isLastMathSymbol = true
        indicesLoop@ for (i in startIdx..line.value.lastIndex) {
            if (line.value[i] in listOf("(", ")")) {
                continue@indicesLoop
            } else {
                when {
                    line.typeList[i] in listOf("LITERAL") -> {
                        if (isLastMathSymbol) {
                            isLastMathSymbol = false
                        } else {
                            return false
                        }
                    }
                    line.typeList[i] == "MATH" -> {
                        if (isLastMathSymbol) {
                            return false
                        } else {
                            isLastMathSymbol = true
                        }
                    }
                }
            }

        }
//      brackets validation
        while (brackets.isNotEmpty()) {
            val bracketIdx = brackets.lastIdx("(")
            when {
                bracketIdx == -1 -> {
                    return false
                }
                brackets[bracketIdx + 1] != ")" -> {
                    return false
                }
                else -> {
                    brackets.removeAt(bracketIdx + 1)
                    brackets.removeAt(bracketIdx)
                }
            }
        }
        return true
    }

    private fun isValid(line: MathLine): Boolean {
        when {
            line.value.isEmpty() -> return false
            isCommand(line) -> return false
            // multiple assignments
            line.value.filter { i -> i == "=" }.count() > 1 -> {
                println("Invalid assignment")
                return false
            }
            // assignment in a wrong place
            line.value.size > 2 && line.value.subList(2, line.value.size).contains("=") -> {
                println("Invalid assignment")
                return false
            }
            //
            !line.correctSyntax -> {
                println("Invalid identifier")
                return false
            }
        }
        val startIdx = if (isAssignment(line) == "ASSIGNMENT") {
            2
        } else 0

        // check if variable has been initialized
        for (i in startIdx..line.value.lastIndex) {
            if (line.typeList[i] == "VARIABLE") {
                if (MathLine.containsVar(line.value[i])) {
                    line.update(MathLine.readVar(line.value[i]).toString(), i)
                } else {
                    println("Unknown variable")
                    return false
                }
            }
        }

        // check if the expression is valid
        if (!isCorrectExpression(line, startIdx)) {
            println("Invalid expression")
            return false
        }
        return true
    }

    private fun subtractionToNegative(line: MathLine, startIdx: Int) {
        val lastIdx = line.getLastIdx(startIdx)
        while (true) {
            if ("-" in line.value.subList(startIdx, lastIdx)) {
                val minusIdx = line.value.firstIdx("-", startIdx, lastIdx)
                val result = line.value[minusIdx + 1].toDouble() * (-1)
                line.update("+", minusIdx)
                line.update(result.toString(), minusIdx + 1)
            } else {
                break
            }
        }
    }

    private fun addition(line: MathLine, startIdx: Int) {
        var lastIdx: Int
        while (true) {
            lastIdx = line.getLastIdx(startIdx)
            if ("+" in line.value.subList(startIdx, lastIdx)) {
                val addIdx = line.value.lastIdx("+", startIdx, lastIdx)
                val result: Double = line.value[addIdx - 1].toDouble() + line.value[addIdx + 1].toDouble()
                line.update(result.toString(), addIdx - 1, addIdx + 1)
            } else {
                break
            }
        }
    }

    private fun multiplication(line: MathLine, startIdx: Int) {
        var lastIdx: Int
        while (true) {
            lastIdx = line.getLastIdx(startIdx)
            if ("*" in line.value.subList(startIdx, lastIdx)) {
                val multiplyIdx = line.value.lastIdx("*", startIdx, lastIdx)
                val result: Double = line.value[multiplyIdx - 1].toDouble() * line.value[multiplyIdx + 1].toDouble()
                line.update(result.toString(), multiplyIdx - 1, multiplyIdx + 1)
                continue
            } else if ("/" in line.value.subList(startIdx, lastIdx)) {
                val divideIdx = line.value.lastIdx("/", startIdx, lastIdx)
                if (line.value[divideIdx + 1].toDouble() == 0.0) {
                    println("Division by 0!")
                    break
                }
                val result: Double = line.value[divideIdx - 1].toDouble() / line.value[divideIdx + 1].toDouble()
                line.update(result.toString(), divideIdx - 1, divideIdx + 1)
                continue
            } else {
                break
            }
        }
    }

    private fun power(line: MathLine, startIdx: Int) {
        var lastIdx: Int
        while (true) {
            lastIdx = line.getLastIdx(startIdx)
            if ("^" in line.value.subList(startIdx, lastIdx)) {
                val powerIdx = line.value.lastIdx("^", startIdx, lastIdx)
                val result: Double = line.value[powerIdx - 1].toDouble().pow(line.value[powerIdx + 1].toDouble())
                line.update(result.toString(), powerIdx - 1, powerIdx + 1)
            } else break
        }
    }

    private fun solve(line: MathLine, startIdx: Int = 0) {
        do {
            val (brackets, tempStartIdx) = line.findBrackets(startIdx)
            if (brackets) {
                solve(line, tempStartIdx + 1)
                val result = line.value[tempStartIdx + 1]
                line.update(result, tempStartIdx, tempStartIdx + 2)
            } else {
                subtractionToNegative(line, startIdx)
                power(line, startIdx)
                multiplication(line, startIdx)
                addition(line, startIdx)
            }
        } while (brackets)
    }

    private fun result(line: MathLine) {
        when (isAssignment(line)) {
            "ASSIGNMENT" -> {
                solve(line)
                MathLine.updateVar(line.value[0], line.value[2].toDouble())
            }
            "NO ASSIGNMENT" -> {
                solve(line)
                println(line.value[0].toDouble().toInt())
            }
        }
    }

    fun next(): Boolean {
        val newCalculation = MathLine()
        newCalculation.readLine()
        if (isValid(newCalculation)) {
            result(newCalculation)
        }
        return exitCommand
    }
}

fun main() {
    while (true) {
        if (Calculator.next()) break
    }
}