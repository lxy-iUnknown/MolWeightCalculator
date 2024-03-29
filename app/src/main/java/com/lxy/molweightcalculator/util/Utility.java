package com.lxy.molweightcalculator.util;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Operator;
import com.lxy.molweightcalculator.contract.Value;

public class Utility {
    public static final int MAX_PRECISION = 4;
    public static final int FIXED_POINT_MULTIPLIER = 1_0_0_0_0; // Four digits after decimal point
    public static final int INITIAL_CAPACITY = 8;

    public static void checkInitialCapacity(int initialCapacity) {
        if (BuildConfig.DEBUG) {
            Contract.requireOperation(new Value<>(
                    "initialCapacity", initialCapacity), Value.ZERO_I, Operator.GE);
        }
    }

    public static int growSize(int currentSize) {
        return currentSize < INITIAL_CAPACITY / 2 ? INITIAL_CAPACITY : currentSize * 2;
    }

    /**
     * Cast from kotlin class to kotlin superclass or interface.
     * See <a href=https://github.com/JakeWharton/timber/issues/459>this issue</a>
     *
     * @param t Object of type {@link T}
     * @return Object of type {@link U}
     */
    @SuppressWarnings({"unchecked"})
    public static <T, U> U kotlinCast(T t) {
        return (U) t;
    }
}
