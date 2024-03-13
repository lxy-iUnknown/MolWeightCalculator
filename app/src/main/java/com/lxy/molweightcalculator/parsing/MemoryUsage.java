package com.lxy.molweightcalculator.parsing;

public class MemoryUsage {
    private static int USAGE;

    private MemoryUsage() {
    }

    public static void allocate(int size, int count) {
        USAGE += size * count;
    }

    public static int getUsage() {
        return USAGE;
    }

    public static void reset() {
        USAGE = 0;
    }
}
