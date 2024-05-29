package com.lxy.molweightcalculator.util

import android.annotation.SuppressLint
import android.os.Build

@SuppressLint("NewApi")
object FormatUtil {
    private val VAR_HANDLE_AVAILABLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @Suppress("deprecation")
    fun asciiToString(array: ByteArray, start: Int, size: Int): String {
        return java.lang.String(array, 0, start, size).toString()
    }

    fun varHandleAvailable(): Boolean {
        return VAR_HANDLE_AVAILABLE
    }

    fun setShort(array: ByteArray, index: Int, value: Short) {
        InvokeUtil.Api33Impl.VarHandle_setShort(array, index, value)
    }

    fun setInt(array: ByteArray, index: Int, value: Int) {
        InvokeUtil.Api33Impl.VarHandle_setInt(array, index, value)
    }
}