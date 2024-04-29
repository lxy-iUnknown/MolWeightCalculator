package com.lxy.molweightcalculator.parsing;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.util.MathUtil;

public class MemoryUsage {
    private static int USAGE;

    private MemoryUsage() {
    }

    public static void memoryAllocated(int size, int count) {
        if (BuildConfig.DEBUG) {
            Contract.require(MathUtil.multiplyAddExact(USAGE, size, count) > 0,
                    "Overflow occurred when calculating memory usage");
        }
        USAGE += size * count;
    }

    public static int getUsage() {
        return USAGE;
    }

    public static void reset() {
        USAGE = 0;
    }
}
