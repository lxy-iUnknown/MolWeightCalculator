package com.lxy.molweightcalculator.util

import com.lxy.molweightcalculator.parsing.ElementId

object Utility {
    const val MAX_PRECISION = 10
    const val PRECISION_COUNT = MAX_PRECISION
    const val DEFAULT_PRECISION = MAX_PRECISION / 2

    const val INITIAL_CAPACITY = 8

    const val COLUMN_COUNT = 3

    val VALUE_RANGE = 0f..(MAX_PRECISION.toFloat())

    fun growSize(currentSize: Int): Int {
        return if (currentSize < INITIAL_CAPACITY / 2) INITIAL_CAPACITY else currentSize * 2
    }

    fun StringBuilder.appendStatistics(
        statistics: List<Pair<ElementId, Long>>
    ): StringBuilder {
        var count = statistics.size
        statistics.forEach {
            this.append(it.first.elementName)
            this.append('=')
            this.append(it.second)
            if (--count > 0) {
                this.append(", ")
            }
        }
        return this
    }
}