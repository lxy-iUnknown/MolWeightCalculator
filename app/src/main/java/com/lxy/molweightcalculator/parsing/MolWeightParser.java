package com.lxy.molweightcalculator.parsing;

import android.util.SparseIntArray;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.util.Contract;
import com.lxy.molweightcalculator.util.Utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class MolWeightParser {
    @NonNull
    private static final Pattern PARSE_PATTERN = Pattern.compile("([A-Z][a-z]?)(\\d*)");

    /**
     * Simple number parsing algorithm modified from {@link Integer#parseInt(String, int)},
     * assuming all characters of input number string are digits
     * @param number Input number string
     * @param startIndex Start index(inclusive)
     * @param endIndex End index(exclusive)
     * @return Parsed number. If there is an overflow, return value will be negative
     * */
    private static int parseElementCount(@NonNull CharSequence number, int startIndex, int endIndex) {
        final int OVERFLOW = -1;
        final int RADIX = 10;
        final int LIMIT = -Integer.MAX_VALUE;
        final int MULTIPLY_LIMIT = LIMIT / RADIX;

        Contract.validateStartEndIndex(number, startIndex, endIndex);
        if (startIndex == endIndex) {
            return 1;
        }
        int result = 0;
        for (int i = startIndex; i < endIndex; i++) {
            if (result < MULTIPLY_LIMIT) {
                return OVERFLOW;
            }
            int digit = number.charAt(i) - '0';
            result *= RADIX;
            if (result < LIMIT + digit) {
                return OVERFLOW;
            }
            result -= digit;
        }
        return -result;
    }

    private static void validateFormula(@NonNull CharSequence formula) {
        if (BuildConfig.DEBUG) {
            for (int i = 0; i < formula.length(); i++) {
                Contract.requireLatin1AlphaNumeric(formula.charAt(i));
            }
        }
    }

    @NonNull
    public static MolWeightParseResult parse(@NonNull CharSequence formula) {
        final int GROUP_ELEMENT_NAME = 1;
        final int GROUP_ELEMENT_COUNT = 2;

        if (formula.length() == 0) {
            if (BuildConfig.DEBUG) {
                Timber.d("Empty formula");
            }
            return MolWeightParseResult.EMPTY_FORMULA;
        }
        validateFormula(formula);
        Matcher match = PARSE_PATTERN.matcher(formula);
        SparseIntArray statistics = new SparseIntArray(Utility.INITIAL_CAPACITY);
        double weight = 0;
        int start, end = 0, prevEnd = 0;
        while (match.find()) {
            start = match.start();
            end = match.end();
            if (start != prevEnd) {
                if (BuildConfig.DEBUG) {
                    Timber.d("Invalid formula \"%s\"", formula);
                }
                return new MolWeightParseResult(ParseError.INVALID_FORMULA, formula.toString());
            }
            prevEnd = end;
            char element = ElementData.parse(formula, match.start(GROUP_ELEMENT_NAME), match.end(GROUP_ELEMENT_NAME));
            ElementInfo info = ElementData.getElementInfo(element);
            if (BuildConfig.DEBUG) {
                Timber.d("Element name input: \"%s\", Parsed element: %s", formula.subSequence(
                        match.start(GROUP_ELEMENT_NAME), match.end(GROUP_ELEMENT_NAME)),
                        ElementData.debugToString(element));
            }
            if (!info.isValid()) {
                String name = ElementData.getElementName(element);
                if (BuildConfig.DEBUG) {
                    Timber.d("Invalid element: %s", name);
                }
                return new MolWeightParseResult(ParseError.INVALID_ELEMENT, name);
            }
            float elementWeight = info.getMolecularWeight();
            int count = parseElementCount(formula,
                    match.start(GROUP_ELEMENT_COUNT), match.end(GROUP_ELEMENT_COUNT));
            if (BuildConfig.DEBUG) {
                Timber.d("Element count input: \"%s\", element count: %d", formula.subSequence(
                        match.start(GROUP_ELEMENT_COUNT), match.end(GROUP_ELEMENT_COUNT)), count);
            }
            if (count < 0) {
                if (BuildConfig.DEBUG) {
                    Timber.d("Element count overflow");
                }
                return MolWeightParseResult.ELEMENT_COUNT_OVERFLOW;
            }
            statistics.put(element, count + statistics.get(element, 0));
            weight += elementWeight * count;
        }
        if (end != formula.length()) {
            if (BuildConfig.DEBUG) {
                Timber.d("Invalid formula %s", formula);
            }
            return MolWeightParseResult.INVALID_FORMULA;
        }
        if (weight > Float.MAX_VALUE) {
            if (BuildConfig.DEBUG) {
                Timber.d("Total weight overflow, value: %f", weight);
            }
            return MolWeightParseResult.WEIGHT_OVERFLOW;
        }
        return new MolWeightParseResult((float) weight, statistics);
    }
}

