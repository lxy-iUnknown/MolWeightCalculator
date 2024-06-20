package com.lxy.molweightcalculator.ui

import android.annotation.SuppressLint
import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.parsing.StatisticsItem
import com.lxy.molweightcalculator.util.FormatUtil.asciiToString
import com.lxy.molweightcalculator.util.FormatUtil.setInt
import com.lxy.molweightcalculator.util.FormatUtil.setShort
import com.lxy.molweightcalculator.util.FormatUtil.varHandleAvailable
import com.lxy.molweightcalculator.util.div10
import com.lxy.molweightcalculator.util.div100
import com.lxy.molweightcalculator.util.div10K
import com.lxy.molweightcalculator.util.div1M

@JvmInline
value class MassRatio(val value: Int) {
    companion object {
        // Android is always little-endian
        private const val ONE_HUNDRED = 0x3030303100.toInt()
        private const val MAX_CHAR_COUNT = 3 /* integer part */ +
                1 /* integer part padding */ +
                1 /* decimal point */ +
                6 /* fraction part */ +
                1 /* percent char */

        private const val FIXED_POINT_MULTIPLIER = 1000000
        private const val FIXED_POINT_PERCENT_MULTIPLIER = FIXED_POINT_MULTIPLIER * 100L

        private const val INT_BOUNDARY = (1L shr 52).toDouble()

        private val TWO_DIGITS = shortArrayOf(
            0x3030, 0x3130, 0x3230, 0x3330, 0x3430, 0x3530, 0x3630, 0x3730, 0x3830, 0x3930,
            0x3031, 0x3131, 0x3231, 0x3331, 0x3431, 0x3531, 0x3631, 0x3731, 0x3831, 0x3931,
            0x3032, 0x3132, 0x3232, 0x3332, 0x3432, 0x3532, 0x3632, 0x3732, 0x3832, 0x3932,
            0x3033, 0x3133, 0x3233, 0x3333, 0x3433, 0x3533, 0x3633, 0x3733, 0x3833, 0x3933,
            0x3034, 0x3134, 0x3234, 0x3334, 0x3434, 0x3534, 0x3634, 0x3734, 0x3834, 0x3934,
            0x3035, 0x3135, 0x3235, 0x3335, 0x3435, 0x3535, 0x3635, 0x3735, 0x3835, 0x3935,
            0x3036, 0x3136, 0x3236, 0x3336, 0x3436, 0x3536, 0x3636, 0x3736, 0x3836, 0x3936,
            0x3037, 0x3137, 0x3237, 0x3337, 0x3437, 0x3537, 0x3637, 0x3737, 0x3837, 0x3937,
            0x3038, 0x3138, 0x3238, 0x3338, 0x3438, 0x3538, 0x3638, 0x3738, 0x3838, 0x3938,
            0x3039, 0x3139, 0x3239, 0x3339, 0x3439, 0x3539, 0x3639, 0x3739, 0x3839, 0x3939
        )

        @Suppress("SameParameterValue")
        private fun writeDigit(buffer: ByteArray, index: Int, digit: Int) {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("digit", digit), 0, 9)
            }
            buffer[index] = ('0'.code + digit).toByte()
        }

        @SuppressLint("NewApi")
        private fun writeTwoDigits(buffer: ByteArray, index: Int, value: Int) {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("value", value), 0, 99)
            }
            if (varHandleAvailable()) {
                buffer.setShort(index, TWO_DIGITS[value])
            } else {
                val div = value.div10()
                buffer[index] = ('0'.code + div).toByte()
                buffer[index + 1] = ('0'.code + value - div * 10).toByte()
            }
        }

        private fun appendTwoDigits(buffer: ByteArray, index: Int, value: Int): Int {
            writeTwoDigits(buffer, index, value)
            return index + 2
        }

        // Round to even, assuming 0 <= value <= 1e8
        private fun roundToEven(value: Double): Double {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(Value("value", value), 0.0, 1e8)
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
            val buffer = ByteArray(MAX_CHAR_COUNT)
            var index = 4
            var temp = this.value
            // 123456789 -> 123.456789%
            var div = temp.div1M()
            temp -= div * FIXED_POINT_MULTIPLIER
            val start = if (div < 10) {
                writeDigit(buffer, 3, div)
                3
            } else if (div < 100) {
                writeTwoDigits(buffer, 2, div)
                2
            } else {
                if (varHandleAvailable()) {
                    buffer.setInt(0, ONE_HUNDRED)
                } else {
                    buffer[1] = '1'.code.toByte()
                    buffer[2] = '0'.code.toByte()
                    buffer[3] = '0'.code.toByte()
                }
                1
            }
            buffer[index++] = '.'.code.toByte()
            // 456789
            div = temp.div10K()
            temp -= div * 10_000
            index = appendTwoDigits(buffer, index, div)
            // 6789
            div = temp.div100()
            // 67
            index = appendTwoDigits(buffer, index, div)
            // 89
            index = appendTwoDigits(buffer, index, temp - div * 100)
            buffer[index++] = '%'.code.toByte()
            return buffer.asciiToString(start, index - start)
        }
}
