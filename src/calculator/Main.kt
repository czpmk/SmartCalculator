package calculator

import java.util.*

val input = Scanner(System.`in`)

object Equation {

    private fun addition() {
        val x = input.nextInt()
        val y = input.nextInt()
        val outcome = x + y
        Archives.update(x, y, outcome)
        print(outcome)
    }

    fun nextAction(action: String) {
        when (action) {
            "add" -> addition()
        }
    }

    object Archives {
        private var actionArchives = arrayOf<Array<Int>>()

        fun update(arg1: Int, arg2: Int, arg3: Int) {
            actionArchives += arrayOf<Int>(arg1, arg2, arg3)
        }
    }
}

fun main() {
    Equation.nextAction("add")
}
