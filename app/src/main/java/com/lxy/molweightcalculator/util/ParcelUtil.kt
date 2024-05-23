package com.lxy.molweightcalculator.util

import android.os.Build
import android.os.Parcel
import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Value


fun Parcel.readBool(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return this.readBoolean()
    }
    return this.readInt() != 0
}

fun Parcel.writeBool(value: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.writeBoolean(value)
    } else {
        this.writeInt(if (value) 1 else 0)
    }
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
