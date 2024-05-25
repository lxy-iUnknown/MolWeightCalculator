package com.lxy.molweightcalculator.ui

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Operator
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.parsing.StatisticsItem
import com.lxy.molweightcalculator.util.MathUtil

@JvmInline
value class MassRatio(val value: Int) {
    companion object {
        private const val MAX_CHAR_COUNT = 3 /* integer part */ +
                1 /* decimal point */ +
                7 /* fraction part */ +
                1 /* percent char */

        private const val FIXED_POINT_MULTIPLIER = 1000000
        private const val FIXED_POINT_PERCENT_MULTIPLIER = FIXED_POINT_MULTIPLIER * 100L

        private const val INT_BOUNDARY = (1L shr 52).toDouble()

        private fun appendDigit(buffer: CharArray, index: Int, digit: Int): Int {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("digit", digit), 0, 9)
            }
            buffer[index] = ('0'.code + digit).toChar()
            return index + 1
        }

        private fun appendTwoDigits(buffer: CharArray, index: Int, value: Int): Int {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("value", value), 0, 99)
            }
            val div = MathUtil.div10(value)
            return appendDigit(buffer, appendDigit(buffer, index, div), value - div * 10)
        }

        private fun appendPercentIntegerPart(buffer: CharArray, index: Int, value: Int): Int {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("value", value), 0, 100)
            }
            if (value < 10) {
                return appendDigit(buffer, index, value)
            } else if (value < 100) {
                return appendTwoDigits(buffer, index, value)
            } else {
                // 100
                buffer[index] = '1'
                buffer[index + 1] = '0'
                buffer[index + 2] = '0'
                return index + 3
            }
        }

        // Round to even, assuming value >= 0
        private fun roundToEven(value: Double): Double {
            if (BuildConfig.DEBUG) {
                Contract.requireOperation(
                    Value("value", value), Value.zeroValue(), Operator.GE)
            }
            if (value >= INT_BOUNDARY) {
                return value
            }
            return ((value + INT_BOUNDARY) - INT_BOUNDARY)
        }

        fun valueOf(weight: Double, item: StatisticsItem): MassRatio {
            return MassRatio(
                if (weight == 0.0)
                    0
                else
                    roundToEven(
                        item.count * item.elementId.weight *
                                FIXED_POINT_PERCENT_MULTIPLIER / weight
                    ).toInt()
            )
        }
    }

    val string: String
        get() {
            val buffer = CharArray(MAX_CHAR_COUNT)
            var index = 0
            var temp = this.value
            // 123456789 -> 123.456789%
            var div = MathUtil.div1M(temp)
            temp -= div * FIXED_POINT_MULTIPLIER
            index = appendPercentIntegerPart(buffer, index, div)
            buffer[index++] = '.'
            // 345678
            div = MathUtil.div100K(temp)
            temp -= div * 100000
            index = appendDigit(buffer, index, div)
            // 45678
            div = MathUtil.div10K(temp)
            temp -= div * 10000
            index = appendDigit(buffer, index, div)
            // 5678
            div = MathUtil.div1K(temp)
            temp -= div * 1000
            index = appendDigit(buffer, index, div)
            // 678
            div = MathUtil.div100(temp)
            index = appendDigit(buffer, index, div)
            index = appendTwoDigits(buffer, index, temp - div * 100)
            buffer[index++] = '%'
            return String(buffer, 0, index)
        }
}
