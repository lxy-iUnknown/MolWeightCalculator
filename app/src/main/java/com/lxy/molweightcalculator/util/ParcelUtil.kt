package com.lxy.molweightcalculator.util

import android.os.Parcel
import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.parsing.ElementId
import com.lxy.molweightcalculator.parsing.StatisticsItem


fun Parcel.readBool(): Boolean {
    return this.readByte() != 0.toByte()
}

fun Parcel.writeBool(value: Boolean) {
    this.writeByte(if (value) 1 else 0)
}

fun Parcel.readChar(): Char {
    val value = this.readInt()
    if (BuildConfig.DEBUG) {
        Contract.requireInRangeInclusive(
            Value("char", value),
            Character.MIN_VALUE.code, Character.MAX_VALUE.code
        )
    }
    return value.toChar()
}

fun Parcel.writeChar(value: Char) {
    this.writeInt(value.code)
}

fun Parcel.readStatistics(): List<StatisticsItem> {
    val size = this.readInt()
    val list = ArrayList<StatisticsItem>(size)
    for (i in 0 until size) {
        list[i] = StatisticsItem(ElementId(this.readChar()), this.readLong())
    }
    return list
}

fun Parcel.writeStatistics(statistics: List<StatisticsItem>) {
    val size = statistics.size
    this.writeInt(size)
    for (i in 0 until size) {
        val item = statistics[i]
        this.writeChar(item.elementId.value)
        this.writeLong(item.count)
    }
}
