package com.lxy.molweightcalculator.util

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.parsing.StatisticsItem
import com.lxy.molweightcalculator.ui.MassRatio
import timber.log.Timber

object SortUtil {
    private const val ASCENDING = 0
    private const val DESCENDING = 1

    private const val ELEMENT_ID = 0
    private const val ORDINAL = 1
    private const val ELEMENT_COUNT = 2
    private const val MASS_RATIO = 3

    private fun <T> invalidsSortOrder(sortOrder: Int): T {
        return Contract.fail("Invalid sort order: $sortOrder")
    }

    private fun <T> invalidsSortMethod(sortMethod: Int): T {
        return Contract.fail("Invalid sort method: $sortMethod")
    }

    private fun sortOrderToString(sortOrder: Int): String {
        return when (sortOrder) {
            ASCENDING -> "ASCENDING"
            DESCENDING -> "DESCENDING"
            else -> invalidsSortOrder(sortOrder)
        }
    }

    private fun sortMethodToString(sortMethod: Int): String {
        return when (sortMethod) {
            ELEMENT_ID -> "ELEMENT_ID"
            ORDINAL -> "ORDINAL"
            ELEMENT_COUNT -> "ELEMENT_COUNT"
            MASS_RATIO -> "MASS_RATIO"
            else -> invalidsSortMethod(sortMethod)
        }
    }

    private fun elementIdComparator(o1: StatisticsItem, o2: StatisticsItem): Int {
        return o1.elementId.value.compareTo(o2.elementId.value)
    }

    private fun ordinalComparator(o1: StatisticsItem, o2: StatisticsItem): Int {
        return o1.elementId.ordinal.compareTo(o2.elementId.ordinal)
    }

    private fun countComparator(o1: StatisticsItem, o2: StatisticsItem): Int {
        val key1 = o1.count
        val key2 = o2.count
        if (key1 == key2) {
            return elementIdComparator(o1, o2)
        }
        return if (key1 > key2) 1 else -1
    }

    fun sortStatistics(
        parseResult: ParseResult,
        sortOrder: Int,
        sortMethod: Int
    ) {
        fun massRatioComparator(o1: StatisticsItem, o2: StatisticsItem): Int {
            val weight = parseResult.weight
            val key1 = MassRatio.valueOf(weight, o1).value
            val key2 = MassRatio.valueOf(weight, o2).value
            if (key1 == key2) {
                return elementIdComparator(o1, o2)
            }
            return if (key1 > key2) 1 else -1
        }

        if (BuildConfig.DEBUG) {
            Timber.d("Sort order: ${sortOrderToString(sortOrder)}")
            Timber.d("Sort method: ${sortMethodToString(sortMethod)}")
        }

        val comparator = when (sortOrder) {
            ASCENDING -> {
                when (sortMethod) {
                    ELEMENT_ID -> ::elementIdComparator
                    ORDINAL -> ::ordinalComparator
                    ELEMENT_COUNT -> ::countComparator
                    MASS_RATIO -> ::massRatioComparator
                    else -> invalidsSortMethod(sortMethod)
                }
            }

            DESCENDING -> {
                when (sortMethod) {
                    ELEMENT_ID -> { o1, o2 -> elementIdComparator(o2, o1) }
                    ORDINAL -> { o1, o2 -> ordinalComparator(o2, o1) }
                    ELEMENT_COUNT -> { o1, o2 -> countComparator(o2, o1) }
                    MASS_RATIO -> { o1, o2 -> massRatioComparator(o2, o1) }
                    else -> invalidsSortMethod(sortMethod)
                }
            }

            else -> invalidsSortOrder(sortOrder)
        }
        parseResult.statistics.sortWith(comparator)
    }
}