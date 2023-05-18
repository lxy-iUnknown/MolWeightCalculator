package com.lxy.molweightcalculator.ui;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.parsing.ElementData;
import com.lxy.molweightcalculator.util.Contract;
import com.lxy.molweightcalculator.util.Utility;

public class StatisticsRow implements Row {
    private final char elementId;
    private final int count;
    private final int fixedPointRatio;

    private static int roundToFixedPointRatio(float ratio) {
        if (BuildConfig.DEBUG) {
            // ±Infinity and NaN -> false
            Contract.require(ratio >= 0 && ratio <= 1, "Invalid ratio range");
        }
        // 0.23456789(23.456789%) > 23.4568% -> 234568
        // 0.23456789 -> 2345.6789 -> 234567.89 -> 234568
        return Math.round(ratio * (Utility.FIXED_POINT_MULTIPLIER * 100 /* percentage */));
    }

    @NonNull
    private static StringBuilder appendDigit(@NonNull StringBuilder sb, int digit) {
        if (BuildConfig.DEBUG) {
            Contract.require(digit >= 0 && digit <= 9, "Invalid digit");
        }
        return sb.append((char) ('0' + digit));
    }

    @NonNull
    private static StringBuilder appendTwoDigits(StringBuilder sb, int value) {
        if (BuildConfig.DEBUG) {
            Contract.require(value >= 0 && value <= 99, "Invalid value range");
        }
        // 45
        int div = value / 10; // 4
        return appendDigit(appendDigit(sb, div), value - div * 10); // 5
    }

    @NonNull
    private static StringBuilder appendPercentIntegerPart(StringBuilder sb, int value) {
        if (BuildConfig.DEBUG) {
            Contract.require(value >= 0 && value <= 100, "Invalid value range");
        }
        if (value < 10) {
            return appendDigit(sb, value);
        } else if (value < 100) {
            return appendTwoDigits(sb, value);
        }
        return sb.append('1').append('0').append('0'); // 100
    }

    public StatisticsRow(int count, char elementId, float ratio) {
        if (BuildConfig.DEBUG) {
            Contract.require(count >= 0, "Invalid count");
        }
        this.elementId = elementId;
        this.count = count;
        this.fixedPointRatio = roundToFixedPointRatio(ratio);
    }

    public char getElementId() {
        return elementId;
    }

    public int getOrdinal() {
        return ElementData.getOrdinal(getElementId());
    }

    public int getFixedPointRatio() {
        return fixedPointRatio;
    }

    public int getCount() {
        return count;
    }

    @NonNull
    @Override
    public String elementNameString() {
        return ElementData.getElementName(getElementId());
    }

    @NonNull
    @Override
    public String elementCountString() {
        return String.valueOf(getCount());
    }

    @NonNull
    @Override
    public String massRatioString() {
        final int MAX_CHAR_COUNT = 2 /* integer part */ +
                1 /* decimal point */ +
                4 /* fraction part */ +
                1 /* percent char */;

        int div, rem;
        StringBuilder sb = new StringBuilder(MAX_CHAR_COUNT);
        // 123456 -> 12.3456%(0.123456)
        div = fixedPointRatio / Utility.FIXED_POINT_MULTIPLIER; // 12
        rem = fixedPointRatio - div * Utility.FIXED_POINT_MULTIPLIER; // 3456
        appendPercentIntegerPart(sb, div).append('.');
        div = rem / 100; // 34
        return appendTwoDigits(appendTwoDigits(sb, div) /* 34 */,
                rem - div * 100 /* 56 */)
                .append('%')
                .toString();
    }
}
