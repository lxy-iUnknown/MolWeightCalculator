package com.lxy.molweightcalculator.parsing

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.util.HashCode
import com.lxy.molweightcalculator.util.Utility.appendStatistics
import com.lxy.molweightcalculator.util.readChar
import com.lxy.molweightcalculator.util.writeChar


@Stable
class ParseResult : Parcelable {
    private val list: MutableList<StatisticsItem>
    private var value by mutableLongStateOf(0)

    constructor() {
        this.value = packValue(MAX_START_END, MAX_START_END, ErrorCode.EmptyFormula)
        this.list = SnapshotStateList()
    }

    private constructor(parcel: Parcel) {
        this.value = parcel.readLong()
        val size = parcel.readInt()
        val list = SnapshotStateList<StatisticsItem>()
        for (i in 0 until size) {
            list.add(StatisticsItem(ElementId(parcel.readChar()), parcel.readLong()))
        }
        this.list = list
    }

    fun init(errorCode: ErrorCode) {
        init(MAX_START_END, MAX_START_END, errorCode)
    }

    fun init(start: Int, end: Int, errorCode: ErrorCode) {
        list.clear()
        value = packValue(start, end, errorCode)
    }

    fun init(parseState: ParseState) {
        list.clear()
        list.addAll((0 until parseState.size()).map {
            StatisticsItem(ElementId(parseState.keyAt(it)), parseState.valueAt(it))
        })
        value = parseState.weight.toRawBits()
    }

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

    val weight: Double
        get() {
            requireSucceeded()
            return Double.fromBits(value)
        }

    private val rawErrorCode: Int
        get() {
            requireFailed()
            return (value shr ERROR_CODE_SHIFT).toInt()
        }

    val statistics: MutableList<StatisticsItem> get() = list

    val succeeded get() = list.isNotEmpty()

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

    fun debugToString(): String {
        var sb = StringBuilder()
            .append("MolWeightParseResult{")
        if (succeeded) {
            sb = sb.append("weight=")
                .append(weight)
                .append(", statistics=")
                .appendStatistics(list.map { Pair(it.elementId, it.count) })
        } else {
            var errorString = ERROR_MESSAGES[errorCode.ordinal]
            val end: Int
            if (errorCode.isInvalidBracket) {
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

    override fun hashCode(): Int {
        val hash = HashCode(value)
        for (item in statistics) {
            hash.mix(item.hashCode())
        }
        return hash.build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is ParseResult) {
            if (value != other.value) {
                return false
            }
            val size = list.size
            val statisticsOther = other.list
            if (size != statisticsOther.size) {
                return false
            }
            for (i in 0..size) {
                if (!list[i].simpleEquals(statisticsOther[i])) {
                    return false
                }
            }
            return true
        }
        return false
    }

    companion object {
        private const val START_END_BITS = 30
        private const val ERROR_CODE_SHIFT = START_END_BITS * 2

        private const val START_END_MASK = (1 shl START_END_BITS) - 1
        private const val MAX_START_END = START_END_MASK

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

        @Suppress("unused")
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParseResult> {
            override fun createFromParcel(parcel: Parcel): ParseResult {
                return ParseResult(parcel)
            }

            override fun newArray(size: Int): Array<ParseResult?> {
                return arrayOfNulls(size)
            }
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

        fun canParse(length: Int): Boolean {
            return isValidStartEnd(length)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(value)
        val size = list.size
        parcel.writeInt(size)
        for (i in 0 until size) {
            val item = list[i]
            parcel.writeChar(item.elementId.value)
            parcel.writeLong(item.count)
        }
    }

    override fun describeContents(): Int {
        return 0
    }
}
