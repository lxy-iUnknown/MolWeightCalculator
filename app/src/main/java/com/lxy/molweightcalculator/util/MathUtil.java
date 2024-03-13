package com.lxy.molweightcalculator.util;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Operator;
import com.lxy.molweightcalculator.contract.Value;

public class MathUtil {
    private static void checkArguments(long x, long y) {
        if (BuildConfig.DEBUG) {
            Contract.requireOperation(new Value<>("x", x), Value.ZERO_L, Operator.GE);
            Contract.requireOperation(new Value<>("y", y), Value.ZERO_L, Operator.GE);
        }
    }

    // Similar to Math.addExact, but assume x >= 0 and y >= 0
    public static long addExact(long x, long y) {
        checkArguments(x, y);
        return x + y;
    }

    // Similar to Math.multiplyExact, but assume x >= 0 and y >= 0
    public static long multiplyExact(long x, long y) {
        checkArguments(x, y);
        long r = x * y;
        if (((x | y) >>> 31 != 0) && (y != 0) && (r / y != x)) {
            return -1;
        }
        return r;
    }
}
