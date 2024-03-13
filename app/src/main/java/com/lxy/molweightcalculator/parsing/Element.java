package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Value;

public class Element {
    private static final int BASE = 26;
    private static final int ELEMENT_MAX = BASE + BASE * BASE - 1;

    private Element() {
    }

    private static char validateElementId(char elementId) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("elementId", (int) elementId),
                    0, ELEMENT_MAX);
        }
        return elementId;
    }

    @NonNull
    public static String getElementNameFromId(char elementId) {
        validateElementId(elementId);
        if (elementId < BASE) {
            return Character.toString((char) ('A' + elementId));
        } else {
            var buffer = new char[2];
            var div = elementId / BASE;
            buffer[0] = (char) (('A' - 1) + div);
            buffer[1] = (char) ('a' + elementId - div * BASE);
            return new String(buffer, 0, 2);
        }
    }

    public static int getOrdinalFromId(char elementId) {
        return BuildConfig.ELEMENT_ORDINALS[validateElementId(elementId)];
    }

    public static double getWeightFromId(char elementId) {
        return BuildConfig.ELEMENT_WEIGHTS[validateElementId(elementId)];
    }

    public static double getScaledWeightFromId(char elementId) {
        return BuildConfig.ELEMENT_SCALED_WEIGHTS[validateElementId(elementId)];
    }
}
