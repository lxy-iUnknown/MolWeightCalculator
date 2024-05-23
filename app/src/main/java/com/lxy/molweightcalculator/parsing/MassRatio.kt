package com.lxy.molweightcalculator.parsing

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Value

@JvmInline
value class MassRatio(val value: Int) {
    companion object {
        private const val MAX_CHAR_COUNT = 3 /* integer part */ +
                1 /* decimal point */ +
                7 /* fraction part */ +
                1 /* percent char */

        private const val FIXED_POINT_MULTIPLIER = 1000000
        private const val FIXED_POINT_PERCENT_MULTIPLIER = FIXED_POINT_MULTIPLIER * 100L

        // These methods are generated using clang
        // std::int32_t div_x(std::int32_t x) {
        //     __builtin_assume(x >= 0 && x < B);
        //     return x / C;
        // }
        // Calculate x divided by 1 million, assuming 0 <= x <= 100 million
        private fun div1M(n: Int): Int {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("n", n), 0, 100000000)
            }
            return ((n * 1125899907L) ushr 50).toInt()
        }

        // Calculate x divided by 100 thousand, assuming 0 <= x < 1 million
        private fun div100K(n: Int): Int {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("n", n), 0, 999999)
            }
            return (((n ushr 5) * 175921861L) ushr 39).toInt()
        }

        // Calculate x divided by 10 thousand, assuming 0 <= x < 100 thousand
        private fun div10K(n: Int): Int {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("n", n), 0, 99999)
            }
            return ((n * 3518437209L) ushr 45).toInt()
        }

        // Calculate x divided by 1 thousand, assuming 0 <= x < 10 thousand
        private fun div1K(n: Int): Int {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("n", n), 0, 9999)
            }
            return ((n ushr 3) * 8389) ushr 20
        }

        // Calculate x divided by 100, assuming 0 <= x < 1 thousand
        private fun div100(n: Int): Int {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("n", n), 0, 999)
            }
            return ((n ushr 2) * 5243) ushr 17
        }

        // Calculate x divided by 10, assuming 0 <= x < 100
        private fun div10(n: Int): Int {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("n", n), 0, 99)
            }
            return (n * 205) ushr 11
        }

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
            val div = div10(value)
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

        fun valueOf(weight: Double, item: StatisticsItem): MassRatio {
            return MassRatio(
                if (weight == 0.0)
                    0
                else
                    ((item.count * item.elementId.weight *
                            FIXED_POINT_PERCENT_MULTIPLIER) / weight).toInt()
            )
        }
    }

    val string: String
        get() {
            val buffer = CharArray(MAX_CHAR_COUNT)
            var index = 0
            var temp = this.value
            // 123456789 -> 123.456789%
            var div = div1M(temp)
            temp -= div * FIXED_POINT_MULTIPLIER
            index = appendPercentIntegerPart(buffer, index, div)
            buffer[index++] = '.'
            // 345678
            div = div100K(temp)
            temp -= div * 100000
            index = appendDigit(buffer, index, div)
            // 45678
            div = div10K(temp)
            temp -= div * 10000
            index = appendDigit(buffer, index, div)
            // 5678
            div = div1K(temp)
            temp -= div * 1000
            index = appendDigit(buffer, index, div)
            // 678
            div = div100(temp)
            index = appendDigit(buffer, index, div)
            index = appendTwoDigits(buffer, index, temp - div * 100)
            buffer[index++] = '%'
            return String(buffer, 0, index)
        }
}
