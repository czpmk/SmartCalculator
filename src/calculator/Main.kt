package calculator

import java.util.*

val scanner = Scanner(System.`in`)

class MathInput {
    private var newLine = arrayOf<String>()
    var arrayOfNumbers: Array<Double?> = arrayOf()
    private var arrayOfCommands: Array<String?> = arrayOf()
    private val validCommands = arrayOf("+", "-", "*", "/", "/exit")
    val isCorrect: Boolean
        get() {
            return validate()
        }

    val exit: Boolean
        get() {
            return "/exit" in arrayOfCommands
        }

    fun read() {
        val inputToList = scanner.nextLine().split(" ")
        newLine = inputToList.filter {element -> element.isNotEmpty() }.toTypedArray()
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
        return true
    }

}

object Calculator {
    /** Returns sum of all non-null, numerical input elements*/
    private fun addition(newInput: MathInput): Double {
        val newList: List<Double> = newInput.arrayOfNumbers.filterNotNull()
        return newList.sum()
    }

    /** Only checks if input contains no forbidden strings or /exit
     * an prints sum*/
    fun nextAction(): Boolean {
        val newInput = MathInput()
        newInput.read()
        if (newInput.isCorrect) {
            Archives.saveInput(newInput)
            if (!newInput.exit) {
                println(addition(newInput).toInt())
            } else {
                println("Bye!")
                return true
            }
        } else {
            println("Invalid input")
        }
        return false
    }

    /** Saves every MathInput object*/
    object Archives {
        private var inputArchives: Array<MathInput> = arrayOf()

        fun saveInput(newInput: MathInput) {
            inputArchives += newInput
        }
    }
}

fun main() {
    // repeat until "/exit" is found
    while (true) {
        if (Calculator.nextAction()) break
    }
}