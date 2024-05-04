package com.lxy.molweightcalculator.ui;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Value;

public class MassRatio {
    public static final int FIXED_POINT_MULTIPLIER = 1_0_0_0_0_0_0_0;

    private MassRatio() {

    }

    private static int appendDigit(@NonNull char[] buffer, int index, int digit) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("digit", digit), 0, 9);
        }
        buffer[index++] = (char) ('0' + digit);
        return index;
    }

    private static int appendTwoDigits(@NonNull char[] buffer, int index, int value) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("value", value), 0, 99);
        }
        // 45
        var div = value / 10; // 4
        return appendDigit(buffer, appendDigit(buffer, index, div), value - div * 10); // 5
    }

    private static int appendPercentIntegerPart(@NonNull char[] buffer, int index, int value) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("value", value), 0, 100);
        }
        if (value < 10) {
            return appendDigit(buffer, index, value);
        } else if (value < 100) {
            return appendTwoDigits(buffer, index, value);
        } else {
            // 100
            buffer[index++] = '1';
            buffer[index++] = '0';
            buffer[index++] = '0';
            return index;
        }
    }

    @NonNull
    public static String massRatioString(int massRatio) {
        final int MAX_CHAR_COUNT = 3 /* integer part */ +
                1 /* decimal point */ +
                7 /* fraction part */ +
                1 /* percent char */;

        var buffer = new char[MAX_CHAR_COUNT];
        var index = 0;
        int div, rem;
        // 123456789 -> 12.3456789%(0.123456789)
        div = massRatio / FIXED_POINT_MULTIPLIER;
        rem = massRatio - div * FIXED_POINT_MULTIPLIER;
        index = appendPercentIntegerPart(buffer, index, div);
        buffer[index++] = '.';
        div = rem / 100000;
        rem -= div * 100000;
        index = appendTwoDigits(buffer, index, div);
        div = rem / 1000;
        rem -= div * 1000;
        index = appendTwoDigits(buffer, index, div);
        div = rem / 10;
        rem -= div * 10;
        index = appendTwoDigits(buffer, index, div);
        index = appendDigit(buffer, index, rem);
        buffer[index++] = '%';
        return new String(buffer, 0, index);
    }
}
