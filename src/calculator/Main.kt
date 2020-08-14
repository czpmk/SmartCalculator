package calculator

import java.lang.NumberFormatException
import java.math.BigInteger
import java.util.*
import kotlin.math.pow

val scanner = Scanner(System.`in`)

// extensions
fun String.splitWith(delimitersList: List<String>): MutableList<String> {
    val tempList = mutableListOf<String>()
    var lastAdded = -1
    for (idx in this.indices) {
        if (this[idx].toString() in delimitersList) {
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

fun MutableList<MathValue>.firstIdxOf(value: String, startIdx: Int = 0, lastIdx: Int = this.lastIndex): Int {
    val result = this.subList(startIdx, lastIdx + 1).indexOfFirst { i -> i.type == value }
    return if (result == -1) {
        result
    } else {
        result + startIdx
    }
}

fun MutableList<MathValue>.replaceByIdx(value: String, startIdx: Int, lastIdx: Int) {
    this[startIdx] = MathValue(value)
    this.removeAt(lastIdx - 1)
    this.removeAt(lastIdx - 1)
}

object Calculator {
    private var expression = mutableListOf<MathValue>()

    fun next(): Boolean {
        val nextExpression = MathExpression(scanner.nextLine())
        if (!nextExpression.isCommand) {
            if (nextExpression.isValid) {
                expression = nextExpression.postfixExpression
                if (expression.isNotEmpty()) {
                    solve()
                }
            }
        }
        return nextExpression.exitCode
    }

    private fun replaceVariablesWithValues(): Boolean {
        var newValue: String
        for (i in expression.indices) {
            if (expression[i].type == "VARIABLE") {
                if (Archives.exist(expression[i].value)) {
                    newValue = Archives.getValue(expression[i].value)
                    expression[i] = MathValue(newValue)
                } else {
                    println("Invalid assignment: '${expression[i].value}' does not exist")
                    return false
                }
            }
        }
        return true
    }

    private fun solve(): Boolean {
        var isAssignment = false
        var variableName = ""

        if (expression.last().value == "=") {
            if (expression.first().type != "VARIABLE") {
                println("Invalid assignment: can not assign to: '${expression.first().value}'")
                return false
            } else {
                variableName = expression[0].value
                expression.removeAt(expression.lastIndex)
                expression.removeAt(0)
                isAssignment = true
            }
        }

        if (!replaceVariablesWithValues()) return false
        val (result, isResultValid) = getResult()
        if (isAssignment && isResultValid) {
            Archives.update(variableName, result)
        } else {
            println(result)
        }
        return true
    }

    private fun getResult(): Pair<String, Boolean> {
        var nextOperator: Int
        var result = ""
        while (expression.size != 1) {
            nextOperator = expression.firstIdxOf("MATH-SYMBOL")
            if (nextOperator < 2) {
                return Pair("Invalid expression", false)
            }
            val firstOperand = BigInteger(expression[nextOperator - 2].value)
            val secondOperand = BigInteger(expression[nextOperator - 1].value)
            when (expression[nextOperator].value) {
                "+" -> {
                    result = (firstOperand + secondOperand).toString()
                }
                "-" -> {
                    result = (firstOperand - secondOperand).toString()
                }
                "*" -> {
                    result = (firstOperand.multiply(secondOperand)).toString()
                }
                "/" -> {
                    if (secondOperand == BigInteger.ZERO) {
                        return Pair("Division by 0!", false)
                    }
                    result = (firstOperand.div(secondOperand)).toString()
                }
                "^" -> {
                    try {
                        secondOperand.toString().toInt()
                    } catch (e: NumberFormatException) {
                        return Pair("Invalid exponent: operand $secondOperand is to big, max = ${Int.MAX_VALUE}", false)
                    }
                    result = if (secondOperand.toInt() < 0) {
                        if (firstOperand.toDouble().toInt() == 0) {
                            return Pair("Invalid operation: $firstOperand ^ $secondOperand : division by 0!", false)
                        }
                        firstOperand.toDouble().pow(secondOperand.toInt()).toInt().toString()
                    } else {
                        (firstOperand.pow(secondOperand.toInt())).toString()
                    }
                }
            }
            expression.replaceByIdx(result, nextOperator - 2, nextOperator)
        }
        return if (expression.size == 1) {
            Pair(expression[0].value, true)
        } else {
            print(expression)
            Pair("Invalid expression (solve)", false)
        }
    }

    object Archives {
        private val variable = mutableMapOf<String, String>()

        fun exist(name: String): Boolean {
            return variable.containsKey(name)
        }

        fun getValue(name: String): String {
            return variable[name]!!
        }

        fun update(name: String, value: String) {
            variable[name] = value
        }
    }
}

fun main() {
    while (true) {
        if (Calculator.next()) break
    }
}