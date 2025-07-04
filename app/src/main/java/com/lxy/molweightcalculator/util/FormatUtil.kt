package com.lxy.molweightcalculator.util

import android.annotation.SuppressLint
import android.os.Build

@SuppressLint("NewApi")
object FormatUtil {
    private val VAR_HANDLE_AVAILABLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @Suppress("deprecation")
    fun ByteArray.asciiToString(start: Int, size: Int): String {
        return java.lang.String(this, 0, start, size).toString()
    }

    fun varHandleAvailable(): Boolean {
        return VAR_HANDLE_AVAILABLE
    }

    fun ByteArray.setShort(index: Int, value: Short) {
        InvokeUtil.Api33Impl.VarHandle_setShort(this, index, value)
    }

    fun ByteArray.setInt(index: Int, value: Int) {
        InvokeUtil.Api33Impl.VarHandle_setInt(this, index, value)
    }
}