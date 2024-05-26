package com.lxy.molweightcalculator.ui

import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2

@JvmInline
value class PaneWeight(private val value: Long) {
    companion object {
        // Two NaN's
        val INVALID = PaneWeight(0x7fc00000_7fc00000L)
    }

    constructor(first: Float, second: Float) : this(packFloats(first, second))

    val first get() = unpackFloat1(value)

    val second get() = unpackFloat2(value)
}