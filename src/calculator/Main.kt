package calculator

import java.util.*

val scanner = Scanner(System.`in`)
val mathSymbols = arrayOf('+', '-')

class MathInput {
    private var newLine = arrayOf<String>()
    var arrayOfArguments: Array<Double?> = arrayOf()
    var arrayOfCommands: Array<String?> = arrayOf()
    val isCorrect: Boolean
        get() {
            return length % 2 != 0 && validateSyntax() && validateEquation()
        }
    val exit: Boolean
        get() {
            if ("/exit" in arrayOfCommands) {
                println("Bye!")
                return true
            }
            return false
        }
    val help: Boolean
        get() {
            if ("/help" in arrayOfCommands) {
                println("The program calculates the sum of numbers. Multiple arithmetic symbols " +
                        "(e.g. -- = +; --- = -, ++ = +) are supported")
                return true
            }
            return false
        }
    private val length: Int
        get() {
            return arrayOfArguments.size
        }
    val lastIndex: Int
        get() {
            return arrayOfArguments.lastIndex
        }

    fun read() {
        val inputToList = scanner.nextLine().split(" ")
        newLine = inputToList.filter { element -> element.isNotEmpty() }.toTypedArray()
        interpret()
    }

    private fun interpret() {
        for (element in newLine) {
            try {
                arrayOfArguments += element.toDouble()
                arrayOfCommands = arrayOfCommands.plus(element = null)
            } catch (e: NumberFormatException) {
                arrayOfArguments = arrayOfArguments.plus(element = null)
                arrayOfCommands += element
            }
        }
    }

    private fun repeatedSymbolCheck(elementsIndex: Int): Boolean {
        val element = arrayOfCommands[elementsIndex]
        for (i in element!!) {
            if (i !in mathSymbols || i != element[0]) {
                return false
            }
        }
        if (element.length > 1) convertRepeatedSymbol(elementsIndex)
        return true
    }

    private fun convertRepeatedSymbol(elementsIndex: Int) {
        val element = arrayOfCommands[elementsIndex]
        if (element!![0] == '+') {
            arrayOfCommands[elementsIndex] = "+"
        } else {
            if (element.length % 2 == 0) {
                arrayOfCommands[elementsIndex] = "+"
            } else {
                arrayOfCommands[elementsIndex] = "-"
            }
        }
    }

    private fun validateSyntax(): Boolean {
        for (i in arrayOfCommands.indices) {
            if (arrayOfCommands[i] != null) {
                if (!repeatedSymbolCheck(i)) {
                    return false
                }
            }
        }
        return arrayOfArguments.isNotEmpty()
    }

    private fun validateEquation(): Boolean {
        for (i in arrayOfArguments.indices) {
            if (i % 2 == 0) {
                if (arrayOfArguments[i] == null || arrayOfCommands[i] != null) {
                    return false
                }
            } else {
                if (arrayOfArguments[i] != null || arrayOfCommands[i] == null) {
                    return false
                }
            }
        }
        return true
    }
}

object Calculator {
    private var archives = arrayOf<MathInput>()

    /** Saves every MathInput object*/
    private fun saveInput(newInput: MathInput) {
        archives += newInput
    }

    /** Returns sum of arguments in MathInput object by range*/
    private fun addition(input: MathInput, rangeBegin: Int = 0, rangeEnd: Int = input.lastIndex): Double {
        var sum = 0.0
        var nextArgument: Double
        for (i in (rangeBegin..rangeEnd)) {
            if (input.arrayOfArguments[i] != null) {
                nextArgument = input.arrayOfArguments[i]!!
                if (i - 1 in input.arrayOfCommands.indices) {
                    if (input.arrayOfCommands[i - 1] == "-") {
                        nextArgument *= -1
                    }
                }
                sum += nextArgument
            }
        }
        return sum
    }

    /** At this point only returns sum of all non-null, numerical input elements*/
    private fun calculate(): Double {
        return addition(archives.last())
    }

    /** Complete pipeline for each equation*/
    fun nextAction(): Boolean {
        val newInput = MathInput()
        newInput.read()
        if (newInput.exit) return true
        if (newInput.help) return false
        if (newInput.isCorrect) {
            saveInput(newInput)
            println(calculate().toInt())
        }
        return false
    }
}

fun main() {
    // repeat until "/exit" is found
    while (true) {
        if (Calculator.nextAction()) break
    }
}