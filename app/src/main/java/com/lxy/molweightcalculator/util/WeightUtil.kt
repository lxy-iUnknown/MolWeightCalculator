package com.lxy.molweightcalculator.util

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.ULocale
import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.parsing.ParseResult
import java.math.RoundingMode
import java.text.FieldPosition

object WeightUtil {
    private const val MIN_SCIENTIFIC_THRESHOLD = 1e10

    // 9999999999.0000000000
    private val STRING_BUFFER = StringBuffer(21)
    private val FIELD_POSITION = FieldPosition(0)
    private val NORMAL_FORMATS: Array<DecimalFormat>
    private val EXPONENTIAL_FORMATS: Array<DecimalFormat>

    init {
        val symbols = DecimalFormatSymbols.getInstance(ULocale.ROOT)
        symbols.exponentSeparator = "x10^"

        fun buildDecimalFormat(precision: Int, suffix: String): DecimalFormat {
            val pattern = StringBuilder("0")
            if (precision != 0) {
                pattern.append('.')
            }
            val decimalFormat = DecimalFormat(
                pattern.append("0".repeat(precision))
                    .append(suffix)
                    .toString(), symbols
            )
            decimalFormat.roundingMode = RoundingMode.HALF_EVEN.ordinal
            return decimalFormat
        }

        NORMAL_FORMATS = Array(Utility.PRECISION_COUNT + 1) {
            buildDecimalFormat(it, "")
        }
        EXPONENTIAL_FORMATS = Array(Utility.PRECISION_COUNT + 1) {
            buildDecimalFormat(it, "E0")
        }
    }

    fun getWeightString(parseResult: ParseResult, precision: Int): String {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(
                Value("precision", precision),
                0, Utility.MAX_PRECISION
            )
        }
        val weight = parseResult.weight
        val formats = if (weight < MIN_SCIENTIFIC_THRESHOLD)
            NORMAL_FORMATS
        else
            EXPONENTIAL_FORMATS
        val sb = STRING_BUFFER
        sb.setLength(0)
        return formats[precision].format(weight, sb, FIELD_POSITION).toString()
    }
}