package com.lxy.molweightcalculator.util;

import com.lxy.molweightcalculator.BuildConfig;

public class Utility {
    public static final int MAX_PRECISION = 4;
    public static final int FIXED_POINT_MULTIPLIER = 10000;

    public static final int INITIAL_CAPACITY = 10;

    public static int fixedFromFloat(float value) {
        if (BuildConfig.DEBUG) {
            Contract.require(Float.isFinite(value), "Finite value required");
        }
        return Math.round(value * FIXED_POINT_MULTIPLIER);
    }

    public static float fixedToFloat(int value) {
        return value / (float) FIXED_POINT_MULTIPLIER;
    }

    /**
     * Cast from kotlin class to kotlin superclass or interface.
     * See <a href=https://github.com/JakeWharton/timber/issues/459>this issue</a>
     *
     * @param t Object of type {@link T}
     * @return Object of type {@link U}
     */
    @SuppressWarnings({"RedundantCast", "unchecked"})
    public static <T, U> U kotlinCast(T t) {
        return (U) (Object) t;
    }
}
