package com.lxy.molweightcalculator.parsing

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Operator
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.util.multiplyAddExact

object MemoryUsage {
    private const val PURGE_THRESHOLD = 64 * 1024L // 64 KB

    private var USAGE: Long = 0

    fun memoryAllocated(size: Long, count: Int) {
        if (BuildConfig.DEBUG) {
            Contract.requireOperation(
                Value("size", size),
                Value.zeroValue(), Operator.GE
            )
            Contract.requireOperation(
                Value("count", count),
                Value.zeroValue(), Operator.GE
            )
            Contract.require(
                USAGE.multiplyAddExact(size, count.toLong()) > 0,
                "Overflow occurred when calculating memory usage"
            )
        }
        USAGE += size * count
    }

    fun shouldPurge(): Boolean {
        return USAGE > PURGE_THRESHOLD
    }

    fun reset() {
        USAGE = 0
    }
}
