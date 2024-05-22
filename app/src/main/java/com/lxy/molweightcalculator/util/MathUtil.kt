package com.lxy.molweightcalculator.util

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Operator
import com.lxy.molweightcalculator.contract.Value

object MathUtil {
    private fun checkArgument(name: String, value: Long) {
        if (BuildConfig.DEBUG) {
            Contract.requireOperation(Value(name, value), Value.zeroValue(), Operator.GE)
        }
    }

    // Similar to Math.addExact, but assume x >= 0 and y >= 0
    fun Long.addExact(other: Long): Long {
        checkArgument("this", this)
        checkArgument("other", other)
        return this + other
    }

    // Similar to Math.multiplyExact, but assume x >= 0 and y >= 0
    fun Long.multiplyExact(other: Long): Long {
        checkArgument("this", this)
        checkArgument("other", other)
        val r = this * other
        if (((this or other) ushr 31 != 0L) && (other != 0L) && (r / other != this)) {
            return -1
        }
        return r
    }

    // Similar to Math.multiplyExact, but assume x >= 0 and y >= 0
    fun Long.multiplyAddExact(x: Long, y: Long): Long {
        checkArgument("this", this)
        checkArgument("x", x)
        checkArgument("y", y)
        val r = this * x
        if (((this or x) ushr 31 != 0L) && (x != 0L) && (r / x != this)) {
            return -1
        }
        return r + y
    }
}
