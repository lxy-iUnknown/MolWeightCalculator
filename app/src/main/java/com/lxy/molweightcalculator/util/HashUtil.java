package com.lxy.molweightcalculator.util;

public class HashUtil {
    private static final int PRIME = 31;

    public static int mix(int hash1, int hash2) {
        return PRIME * PRIME + PRIME * hash1 + hash2;
    }
}
