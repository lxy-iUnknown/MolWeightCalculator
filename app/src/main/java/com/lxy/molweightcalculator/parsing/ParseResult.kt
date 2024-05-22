package com.lxy.molweightcalculator.parsing

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.ULocale
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.util.IStatistics
import com.lxy.molweightcalculator.util.Utility
import com.lxy.molweightcalculator.util.readBool
import com.lxy.molweightcalculator.util.readStatistics
import com.lxy.molweightcalculator.util.writeBool
import com.lxy.molweightcalculator.util.writeStatistics
import java.math.RoundingMode
import java.text.FieldPosition

@Immutable
class ParseResult private constructor(
    val statistics: List<StatisticsItem>, private val value: Long
) : Parcelable {

    private constructor(errorCode: ErrorCode) : this(MAX_START_END, MAX_START_END, errorCode)

    constructor(start: Int, end: Int, errorCode: ErrorCode) :
            this(EMPTY_LIST, packValue(start, end, errorCode))

    constructor(statistics: List<StatisticsItem>, weight: Double) :
            this(statistics, weight.toRawBits())

    private fun requireSucceeded() {
        if (BuildConfig.DEBUG) {
            Contract.require(succeeded, "Not succeeded")
        }
    }

    private fun requireFailed() {
        if (BuildConfig.DEBUG) {
            Contract.require(!succeeded, "Succeeded")
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeParseResult(dest, this)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getWeightString(precision: Int): String {
        requireSucceeded()
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(
                Value("precision", precision),
                0, Utility.MAX_PRECISION
            )
        }
        val weight = weight
        val formats = if (weight < MIN_SCIENTIFIC_THRESHOLD)
            NORMAL_FORMATS
        else
            EXPONENTIAL_FORMATS
        val sb = STRING_BUFFER
        sb.setLength(0)
        return formats[precision].format(weight, sb, FIELD_POSITION).toString()
    }

    val succeeded get() = statistics !== EMPTY_LIST

    private val weight: Double
        get() {
            requireSucceeded()
            return Double.fromBits(value)
        }

    private val rawErrorCode: Int
        get() {
            requireFailed()
            return (value shr ERROR_CODE_SHIFT).toInt()
        }

    val errorCode get() = ErrorCode.valueOf(rawErrorCode)

    val start: Int
        get() {
            requireFailed()
            return ((value shr START_END_BITS) and START_END_MASK.toLong()).toInt()
        }

    val end: Int
        get() {
            requireFailed()
            return (value and START_END_MASK.toLong()).toInt()
        }

    val hasStartEnd: Boolean
        get() {
            requireFailed()
            return ((1 shl rawErrorCode) and NO_START_END_MASK) == 0
        }

    val isInvalidBracket: Boolean
        get() {
            requireFailed()
            return rawErrorCode == ErrorCode.MismatchedBracket.ordinal
        }

    fun debugToString(): String {
        var sb = StringBuilder()
            .append("MolWeightParseResult{")
        if (succeeded) {
            sb = sb.append("weight=")
                .append(weight)
                .append(", statistics=")
            IStatistics.appendStatistics(sb, object : IStatistics {
                override fun size(): Int {
                    return statistics.size
                }

                override fun forEach(function: IStatistics.TraverseFunction) {
                    for (item in statistics) {
                        function.visit(item.elementId, item.count)
                    }
                }
            })
        } else {
            var errorString = ERROR_MESSAGES[errorCode.ordinal]
            val end: Int
            if (isInvalidBracket) {
                errorString = errorString.format(ParseState.getBracketString(this.end))
                end = start + 1
            } else {
                end = this.end
            }
            sb.append(errorString)
                .append(", start=")
                .append(start)
                .append(", end=")
                .append(end)
        }
        return sb.append("}").toString()
    }

    companion object {
        private const val FIXED_POINT_PERCENT_MULTIPLIER = MassRatio.FIXED_POINT_MULTIPLIER * 100L
        private const val MIN_SCIENTIFIC_THRESHOLD = 1e10

        private const val START_END_BITS = 30
        private const val ERROR_CODE_SHIFT = START_END_BITS * 2

        private const val START_END_MASK = (1 shl START_END_BITS) - 1
        private const val MAX_START_END = START_END_MASK

        private val NO_START_END_MASK = (1 shl ErrorCode.EmptyFormula.ordinal) or
                (1 shl ErrorCode.ElementCountOverflow.ordinal) or
                (1 shl ErrorCode.WeightOverflow.ordinal) or
                (1 shl ErrorCode.FormulaTooLong.ordinal)


        private val EMPTY_LIST = emptyList<StatisticsItem>()

        private val ERROR_MESSAGES = arrayOf(
            "EMPTY_FORMULA",
            "NO_ELEMENT",
            "MISMATCHED_BRACKET(\"%s\")",
            "INVALID_TOKEN",
            "INVALID_ELEMENT",
            "ELEMENT_COUNT_TOO_LARGE",
            "ELEMENT_COUNT_OVERFLOW",
            "WEIGHT_OVERFLOW",
            "FORMULA_TOO_LONG"
        )

        // 1.7976931e+308
        private val STRING_BUFFER = StringBuffer(14)
        private val NORMAL_FORMATS: Array<DecimalFormat>
        private val EXPONENTIAL_FORMATS: Array<DecimalFormat>
        private val FIELD_POSITION = FieldPosition(0)

        val EMPTY_FORMULA = ParseResult(ErrorCode.EmptyFormula)
        val ELEMENT_COUNT_OVERFLOW = ParseResult(ErrorCode.ElementCountOverflow)
        val WEIGHT_OVERFLOW = ParseResult(ErrorCode.WeightOverflow)
        val FORMULA_TOO_LONG = ParseResult(ErrorCode.FormulaTooLong)

        @Suppress("unused")
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParseResult?> {
            override fun createFromParcel(source: Parcel): ParseResult {
                return readParseResult(source)
            }

            override fun newArray(size: Int): Array<ParseResult?> {
                return arrayOfNulls(size)
            }
        }

        init {
            val range = 0..Utility.PRECISION_COUNT + 1
            val toArray = { it: Int -> arrayOfNulls<DecimalFormat>(it) }
            val symbols: DecimalFormatSymbols = DecimalFormatSymbols
                .getInstance(ULocale.ROOT)
            symbols.exponentSeparator = "x10^"
            val halfEven = RoundingMode.HALF_EVEN.ordinal

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
                decimalFormat.roundingMode = halfEven
                return decimalFormat
            }

            NORMAL_FORMATS = range
                .map { buildDecimalFormat(it, "") }
                .stream().toArray(toArray)
            EXPONENTIAL_FORMATS = range
                .map { buildDecimalFormat(it, "E0") }
                .stream().toArray(toArray)
        }

        private fun deepEquals(
            list1: List<StatisticsItem>,
            list2: List<StatisticsItem>
        ): Boolean {
            if (list1 === list2) {
                return true
            }
            val size = list1.size
            if (size != list2.size) {
                return false
            }
            for (i in 0..size) {
                if (!list1[i].simpleEquals(list2[i])) {
                    return false
                }
            }
            return true
        }

        private fun isValidStartEnd(value: Int): Boolean {
            return (value shr START_END_BITS) == 0
        }

        private fun packValue(start: Int, end: Int, errorCode: ErrorCode): Long {
            if (BuildConfig.DEBUG) {
                Contract.require(isValidStartEnd(start), "Invalid start $start")
                Contract.require(isValidStartEnd(end), "Invalid start $end")
            }
            return (end.toLong()) or
                    (start.toLong() shl START_END_BITS) or
                    (errorCode.ordinal.toLong() shl ERROR_CODE_SHIFT)
        }

        private fun readParseResult(source: Parcel): ParseResult {
            val statistics: List<StatisticsItem> = if (source.readBool()) {
                source.readStatistics()
            } else {
                EMPTY_LIST
            }
            return ParseResult(statistics, source.readLong())
        }

        private fun writeParseResult(dest: Parcel, result: ParseResult) {
            val succeeded = result.succeeded
            dest.writeBool(succeeded)
            if (succeeded) {
                dest.writeStatistics(result.statistics)
            } else {
                dest.writeInt(result.rawErrorCode)
            }
            dest.writeLong(result.value)
        }

        fun canParse(length: Int): Boolean {
            return isValidStartEnd(length)
        }
    }

    fun calculateMassRatio(item: StatisticsItem): Int {
        if (weight == 0.0) {
            // Special case
            return 0
        }
        return ((item.count * item.elementId.weight *
                FIXED_POINT_PERCENT_MULTIPLIER) / weight).toInt()
    }

    fun sort(comparator: Comparator<StatisticsItem>): ParseResult {
        requireSucceeded()
        return ParseResult(statistics.sortedWith(comparator), value)
    }
}
