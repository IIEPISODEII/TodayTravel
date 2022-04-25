package com.example.todaytravel.util

import kotlin.math.roundToInt

object Extension {

    fun String.indentString(): String {
        val notFancy = toString()
        return buildString(notFancy.length) {
            var indent = 0
            fun StringBuilder.line() {
                appendLine()
                repeat(2 * indent) { append(' ') }
            }

            for (char in notFancy) {
                if (char == ' ') continue

                when (char) {
                    ')', ']' -> {
                        indent--
                        line()
                    }
                }

                if (char == '=') append(' ')
                append(char)
                if (char == '=') append(' ')

                when (char) {
                    '(', '[', ',' -> {
                        if (char != ',') indent++
                        line()
                    }
                }
            }
        }
    }

    // Decimal 좌표계를 받아와 DMS 좌표계로 변환
    fun Double.toDMS(): String {
        val deg = this.toInt()
        val min = (60 * (this - deg)).toInt()
        val sec = ((60 * (60 * (this - deg) - min)) * 1000).roundToInt() / 1000f
        return "$deg° $min′ $sec″"
    }
}