package com.lxy.molweightcalculator.ui

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.text.NumberFormat
import android.icu.util.ULocale
import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.util.Utility
import java.math.RoundingMode
import java.text.FieldPosition

class WeightString(
    val value: StringBuffer,
    val exponentBeginIndex: Int,
    val exponentEndIndex: Int
) {
    companion object {

        private const val MIN_SCIENTIFIC_THRESHOLD = 1e10

        // 9999999999.0000000000
        private const val INITIAL_STRING_BUFFER_SIZE = 21

        private val DEFAULT_FIELD_POSITION = FieldPosition(0)
        private val NORMAL_FORMATS: Array<DecimalFormat>
        private val EXPONENTIAL_FORMATS: Array<DecimalFormat>

        init {
            val symbols = DecimalFormatSymbols.getInstance(ULocale.ROOT)
            symbols.exponentSeparator = "×10"

            fun buildDecimalFormat(precision: Int, exponential: Boolean): DecimalFormat {
                val decimalFormat = DecimalFormat()
                decimalFormat.decimalFormatSymbols = symbols
                decimalFormat.isGroupingUsed = false
                decimalFormat.isDecimalSeparatorAlwaysShown = false
                decimalFormat.minimumIntegerDigits = 1
                if (exponential) {
                    decimalFormat.minimumExponentDigits = 1
                    decimalFormat.maximumIntegerDigits = 1
                }
                decimalFormat.minimumFractionDigits = precision
                decimalFormat.maximumFractionDigits = precision
                decimalFormat.roundingMode = RoundingMode.HALF_EVEN.ordinal
                return decimalFormat
            }

            NORMAL_FORMATS = Array(Utility.PRECISION_COUNT + 1) {
                buildDecimalFormat(it, false)
            }
            EXPONENTIAL_FORMATS = Array(Utility.PRECISION_COUNT + 1) {
                buildDecimalFormat(it, true)
            }
        }

        fun valueOf(parseResult: ParseResult, precision: Int): WeightString {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(
                    Value("precision", precision),
                    0, Utility.MAX_PRECISION
                )
            }
            val weight = parseResult.weight
            val sb = StringBuffer(INITIAL_STRING_BUFFER_SIZE)
            if (weight < MIN_SCIENTIFIC_THRESHOLD) {
                return WeightString(
                    NORMAL_FORMATS[precision].format(weight, sb, DEFAULT_FIELD_POSITION),
                    -1, -1
                )
            } else {
                val fieldPosition = FieldPosition(NumberFormat.Field.EXPONENT)
                return WeightString(
                    EXPONENTIAL_FORMATS[precision].format(weight, sb, fieldPosition),
                    fieldPosition.beginIndex, fieldPosition.endIndex
                )
            }
        }
    }
}