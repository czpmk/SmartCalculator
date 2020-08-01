package calculator

import java.util.*

val scanner = Scanner(System.`in`)
val validCommands = arrayOf("+", "-", "*", "/", "/exit", "/help")

class MathInput {
    private var newLine = arrayOf<String>()
    var arrayOfNumbers: Array<Double?> = arrayOf()
    private var arrayOfCommands: Array<String?> = arrayOf()
    val isCorrect: Boolean
        get() {
            return validate()
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
                println("The program calculates the sum of numbers")
                return true
            }
            return false
        }

    fun read() {
        val inputToList = scanner.nextLine().split(" ")
        newLine = inputToList.filter { element -> element.isNotEmpty() }.toTypedArray()
        interpret()
    }

    private fun interpret() {
        for (element in newLine) {
            try {
                arrayOfNumbers += element.toDouble()
                arrayOfCommands = arrayOfCommands.plus(element = null)
            } catch (e: NumberFormatException) {
                arrayOfNumbers = arrayOfNumbers.plus(element = null)
                arrayOfCommands += element
            }
        }
    }

    private fun validate(): Boolean {
        for (element in arrayOfCommands) {
            if (element != null && element !in validCommands) {
                return false
            }
        }
        return arrayOfNumbers.isNotEmpty()
    }
}

object Calculator {
    private var archives = arrayOf<MathInput>()

    /** Saves every MathInput object*/
    private fun saveInput(newInput: MathInput) {
        archives += newInput
    }

    /** Returns sum of all non-null, numerical input elements*/
    private fun addition(newInput: MathInput): Double {
        val newList: List<Double> = newInput.arrayOfNumbers.filterNotNull()
        return newList.sum()
    }

    /** Only checks if input contains no forbidden strings, /exit or /help
     * and prints sum*/
    fun nextAction(): Boolean {
        val newInput = MathInput()
        newInput.read()
        saveInput(newInput)
        if (newInput.exit) return true
        if (newInput.help) return false
        if (newInput.isCorrect) {
            println(addition(newInput).toInt())
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