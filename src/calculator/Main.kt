package calculator

import java.util.*

val scanner = Scanner(System.`in`)
val mathSymbols = arrayOf('+', '-')
const val helpMessage = "The program is a calculator that can return results of basic " +
        "calculations and keep in memory previous inputs, commands, and results. " +
        "Supported action: addition, subtraction. " +
        "Type /exit to exit."

class MathInput {
    private var newLine = arrayOf<String>()
    var arrayOfArguments: Array<Double?> = arrayOf()
    var arrayOfCommands: Array<String?> = arrayOf()
    val isCorrect: Boolean
        get() {
            if (!isCommand && !isEmpty) return validateEquation() && validateSyntax()
            return false
        }
    var isEmpty: Boolean = true
    var isCommand: Boolean = false
    var unknownCommand: Boolean = false
    var exit: Boolean = false
    var help: Boolean = false
    val lastIndex: Int
        get() {
            return arrayOfArguments.lastIndex
        }

    private fun checkIfCommand() {
        if (newLine.first()[0] == '/') {
            isCommand = true
            when (newLine.first()) {
                "/exit" -> exit = true
                "/help" -> help = true
                else -> unknownCommand = true
            }
        }
    }

    fun read() {
        val newInput = scanner.nextLine().split(" ")
        newLine = newInput.filter { element -> element.isNotEmpty() }.toTypedArray()
        isEmpty = newLine.isEmpty()
        if (!isEmpty) {
            checkIfCommand()
            if (!isCommand) {
                interpret()
            }
        }
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

    private fun validateEquation(): Boolean {
        when {
            arrayOfArguments.first() == null -> return false
            arrayOfArguments.last() == null -> return false
        }
        for (i in 1 until arrayOfArguments.size) {
            when {
                arrayOfArguments[i] != null && arrayOfArguments[i - 1] != null -> return false
                arrayOfArguments[i] == null && arrayOfArguments[i - 1] == null -> return false
            }
        }
        return true
    }

    private fun validateSyntax(): Boolean {
        for (i in arrayOfCommands.indices) {
            if (arrayOfCommands[i] != null) {
                if (!mathSymbolsCheck(i)) return false
            }
        }
        return arrayOfArguments.isNotEmpty()
    }

    private fun mathSymbolsCheck(elementsIndex: Int): Boolean {
        val element = arrayOfCommands[elementsIndex]
        if (element!!.length == 1) {
            if (element[0] in mathSymbols) return true
        } else {
            if (element[0] in mathSymbols) {
                for (i in 1 until element.length) {
                    if (element[i] != element[0]) return false
                }
                convertRepeatedSymbol(elementsIndex)
                return true
            }
        }
        return false
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
        if (newInput.isEmpty) return false
        if (newInput.isCommand) {
            when {
                newInput.exit -> {
                    println("Bye!")
                    return true
                }
                newInput.help -> {
                    println(helpMessage)
                }
                newInput.unknownCommand -> println("Unknown command")
            }
            return false
        }
        if (newInput.isCorrect) {
            saveInput(newInput)
            println(calculate().toInt())
        } else {
            println("Invalid expression")
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