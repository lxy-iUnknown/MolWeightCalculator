package com.lxy.molweightcalculator.parsing;

import androidx.annotation.IntDef;

@IntDef({ParseErrorCode.NO_ERROR,
        ParseErrorCode.EMPTY_FORMULA,
        ParseErrorCode.NO_ELEMENT,
        ParseErrorCode.MISMATCHED_BRACKET,
        ParseErrorCode.INVALID_TOKEN,
        ParseErrorCode.INVALID_ELEMENT,
        ParseErrorCode.ELEMENT_COUNT_TOO_LARGE,
        ParseErrorCode.ELEMENT_COUNT_OVERFLOW,
        ParseErrorCode.WEIGHT_OVERFLOW,
})
public @interface ParseErrorCode {
    int NO_ERROR = 0;
    int EMPTY_FORMULA = 1;
    int NO_ELEMENT = 2;
    int MISMATCHED_BRACKET = 3;
    int INVALID_TOKEN = 4;
    int INVALID_ELEMENT = 5;
    int ELEMENT_COUNT_TOO_LARGE = 6;
    int ELEMENT_COUNT_OVERFLOW = 7;
    int WEIGHT_OVERFLOW = 8;
    int MINIMUM = EMPTY_FORMULA;
    int MAXIMUM = WEIGHT_OVERFLOW;
}
