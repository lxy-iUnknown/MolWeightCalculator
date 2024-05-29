package com.lxy.molweightcalculator.ui

import androidx.compose.ui.util.packInts
import androidx.compose.ui.util.unpackInt1
import androidx.compose.ui.util.unpackInt2

@JvmInline
value class SortInfo private constructor(private val value: Long) {
    constructor(sortOrder: Int, sortMethod: Int) :
            this(packInts(sortOrder, sortMethod))

    val sortOrder get() = unpackInt1(value)

    val sortMethod get() = unpackInt2(value)
}