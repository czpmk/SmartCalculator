package calculator

import java.util.*

val input = Scanner(System.`in`)

object Calculations {

    private fun loadVariables(): Pair<Int, Int> {
        val x = input.nextInt()
        val y = input.nextInt()
        return Pair(x, y)
    }

    private fun addition() {
        val (x, y) = loadVariables()
        Archives.update(x, y, x + y)
        print(Archives.lastProduct())
    }

    private fun multiplication() {
        val (x, y) = loadVariables()
        Archives.update(x, y, x * y)
        print(Archives.lastProduct())
    }

    fun nextAction(action: String) {
        when (action) {
            "add" -> addition()
            "multiply" -> multiplication()
        }
    }

    object Archives {
        private var actionArchives = arrayOf<Array<Int>>()

        fun lastProduct(): Int {
            return actionArchives.last().last()
        }

        fun update(arg1: Int, arg2: Int, product: Int) {
            actionArchives += arrayOf(arg1, arg2, product)
        }
    }
}

fun main() {
    Calculations.nextAction("add")
    Calculations.nextAction("multiply")
}
