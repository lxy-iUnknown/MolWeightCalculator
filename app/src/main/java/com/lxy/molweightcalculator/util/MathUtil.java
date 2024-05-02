package com.lxy.molweightcalculator.util;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Operator;
import com.lxy.molweightcalculator.contract.Value;

public class MathUtil {
    private static void checkArgument(@NonNull String name, long value) {
        Contract.requireOperation(new Value<>(name, value), Value.ZERO_L, Operator.GE);
    }

    // Similar to Math.addExact, but assume x >= 0 and y >= 0
    public static long addExact(long x, long y) {
        if (BuildConfig.DEBUG) {
            checkArgument("x", x);
            checkArgument("y", y);
        }
        return x + y;
    }

    // Similar to Math.multiplyExact, but assume x >= 0 and y >= 0
    public static long multiplyExact(long x, long y) {
        if (BuildConfig.DEBUG) {
            checkArgument("x", x);
            checkArgument("y", y);
        }
        var r = x * y;
        if (((x | y) >>> 31 != 0) && (y != 0) && (r / y != x)) {
            return -1;
        }
        return r;
    }

    // Similar to Math.multiplyExact, but assume x >= 0 and y >= 0
    public static long multiplyAddExact(long a, long x, long y) {
        if (BuildConfig.DEBUG) {
            checkArgument("a", a);
            checkArgument("x", x);
            checkArgument("y", y);
        }
        var r = x * y;
        if (((x | y) >>> 31 != 0) && (y != 0) && (r / y != x)) {
            return -1;
        }
        return a + r;
    }
}
