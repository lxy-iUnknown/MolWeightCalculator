package com.lxy.molweightcalculator.util

import androidx.compose.runtime.snapshots.SnapshotStateList
import java.lang.invoke.MethodHandles

// Hacking into private method...
private val SNAPSHOT_LIST_MUTATE = run {
    val method = SnapshotStateList::class.java
        .getDeclaredMethod("mutate", Function1::class.java)
    method.isAccessible = true
    MethodHandles.lookup().unreflect(method)
}

fun <T> SnapshotStateList<T>.batchUpdate(
    builder: (MutableList<T>) -> Unit
) {
    InvokeUtil.SnapshotList_mutate(SNAPSHOT_LIST_MUTATE, this, builder)
}