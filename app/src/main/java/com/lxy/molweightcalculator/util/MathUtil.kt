package com.lxy.molweightcalculator.util

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Operator
import com.lxy.molweightcalculator.contract.Value

private fun checkArgument(name: String, value: Long) {
    if (BuildConfig.DEBUG) {
        Contract.requireOperation(Value(name, value), Value.zeroValue(), Operator.GE)
    }
}

// These methods are generated using clang
// std::int32_t div_x(std::int32_t x) {
//     __builtin_assume(x >= A && x < B);
//     return x / C;
// }

// Calculate x divided by 1 million, assuming 0 <= x <= 100 million
fun Int.div1M(): Int {
    if (BuildConfig.DEBUG) {
        Contract.requireInRangeInclusive(Value("n", this), 0, 100_000_000)
    }
    return ((this * 1125899907L) ushr 50).toInt()
}

// Calculate x divided by 10 thousand, assuming 0 <= x < 1000 thousand
fun Int.div10K(): Int {
    if (BuildConfig.DEBUG) {
        Contract.requireInRangeInclusive(Value("n", this), 0, 999_999)
    }
    return ((this * 3518437209L) ushr 45).toInt()
}

// Calculate x divided by 100, assuming 0 <= x < 10 thousand
fun Int.div100(): Int {
    if (BuildConfig.DEBUG) {
        Contract.requireInRangeInclusive(Value("n", this), 0, 9_999)
    }
    return ((this ushr 2) * 5243) ushr 17
}

// Calculate x divided by 10, assuming 0 <= x < 100
fun Int.div10(): Int {
    if (BuildConfig.DEBUG) {
        Contract.requireInRangeInclusive(Value("n", this), 0, 99)
    }
    return (this * 205) ushr 11
}

// Calculate x divided by 26, assuming 26 <= x <= 701
fun Int.div26(): Int {
    if (BuildConfig.DEBUG) {
        Contract.requireInRangeInclusive(Value("n", this), 26, 701)
    }
    return (this * 20165) ushr 19
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