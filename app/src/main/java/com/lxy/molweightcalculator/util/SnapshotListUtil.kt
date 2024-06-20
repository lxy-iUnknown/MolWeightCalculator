package com.lxy.molweightcalculator.util

import androidx.compose.runtime.snapshots.SnapshotStateList

// Hacking into private method...
private val SnapshotList_mutate = SnapshotStateList::class.java
    .getPrivateMethod("mutate", Function1::class.java)

fun <T> SnapshotStateList<T>.batchUpdate(
    builder: (MutableList<T>) -> Unit
) {
    InvokeUtil.invoke(SnapshotList_mutate, this, builder)
}