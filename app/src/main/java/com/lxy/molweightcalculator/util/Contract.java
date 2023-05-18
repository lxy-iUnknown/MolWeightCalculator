package com.lxy.molweightcalculator.util;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import timber.log.Timber;

public class Contract {
    public static void require(boolean value, String message) {
        if (!value) {
            if (BuildConfig.DEBUG) {
                Timber.wtf("Contract failed: \"%s\"", message);
            }
            throw new RuntimeException(message);
        }
    }

    public static <T> T requireNonNull(T value) {
        if (BuildConfig.DEBUG) {
            require(value != null, "Null value");
        }
        return value;
    }

    public static void validateStartEndIndex(@NonNull CharSequence value, int startIndex, int endIndex) {
        requireNonNull(value);
        if (BuildConfig.DEBUG) {
            require(startIndex <= endIndex,
                    "startIndex" + startIndex + ") > endIndex(" + endIndex + ")");
            require(startIndex >= 0,
                    "startIndex(" + startIndex + ") < 0");
            require(endIndex <= value.length(),
                    "endIndex(" + endIndex + ") > length(" + value.length() + ")");
        }
    }

    public static boolean isInRangeInclusive(char ch, char min, char max) {
        if (BuildConfig.DEBUG) {
            Contract.require(min <= max, "Invalid range: " + min  + " > " + max);
        }
        return ch >= min && ch <= max;
    }

    public static boolean isLatin1UpperCaseLetter(char ch) {
        return isInRangeInclusive(ch, 'A', 'Z');
    }

    public static boolean isLatin1LowerCaseLetter(char ch) {
        return isInRangeInclusive(ch, 'a', 'z');
    }

    public static boolean isLatin1Digit(char ch) {
        return isInRangeInclusive(ch, '0', '9');
    }

    public static void requireLatin1UpperCaseLetter(char ch) {
        if (BuildConfig.DEBUG) {
            Contract.require(isLatin1UpperCaseLetter(ch),
                    "Character '" + ch + "' is not Latin-1 upper-case letter");
        }
    }

    public static void requireLatin1LowerCaseLetter(char ch) {
        if (BuildConfig.DEBUG) {
            Contract.require(isLatin1LowerCaseLetter(ch),
                    "Character '" + ch + "' is not Latin-1 lower-case letter");
        }
    }

    public static void requireLatin1Digit(char ch) {
        if (BuildConfig.DEBUG) {
            Contract.require(isLatin1Digit(ch), "Character '" + ch + "' is not Latin-1 digit");
        }
    }

    public static void requireLatin1AlphaNumeric(char ch) {
        if (BuildConfig.DEBUG) {
            Contract.require(isLatin1Digit(ch) ||
                    isLatin1UpperCaseLetter(ch) ||
                    isLatin1LowerCaseLetter(ch), "Character '" + ch + "' is not Latin-1 alphanumeric");
        }
    }
}
