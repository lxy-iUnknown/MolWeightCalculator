package com.lxy.molweightcalculator.util

import com.lxy.molweightcalculator.parsing.Bracket
import com.lxy.molweightcalculator.parsing.ElementId

object Utility {
    private val BRACKET_STRINGS = arrayOf(
        "", "(", ")", "[", "]", "{", "}"
    )

    const val MAX_PRECISION = 10
    const val PRECISION_COUNT = MAX_PRECISION
    const val DEFAULT_PRECISION = MAX_PRECISION / 2

    const val INITIAL_CAPACITY = 8

    const val BACKGROUND_THRESHOLD = 1000

    val VALUE_RANGE = 0f..(MAX_PRECISION.toFloat())

    fun getBracketString(bracket: Int): String {
        return BRACKET_STRINGS[bracket]
    }

    fun getBracketString(bracket: Bracket): String {
        return getBracketString(bracket.ordinal)
    }

    // (size * 1.5).toInt()
    fun incrementSize(size: Int) = (size * 3) shr 1

    fun growSize(size: Int): Int {
        return if (size < INITIAL_CAPACITY / 2)
            INITIAL_CAPACITY
        else
            incrementSize(size)
    }

    fun StringBuilder.appendStatistics(
        statistics: List<Pair<ElementId, Long>>
    ): StringBuilder {
        var count = statistics.size
        this.append('(')
        statistics.forEach {
            this.append(it.first.elementName)
            this.append('=')
            this.append(it.second)
            if (--count > 0) {
                this.append(", ")
            }
        }
        return this.append(')')
    }
}