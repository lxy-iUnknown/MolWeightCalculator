package com.lxy.molweightcalculator.parsing;

import androidx.annotation.IntDef;

@IntDef({ErrorCode.NO_ERROR,
        ErrorCode.EMPTY_FORMULA,
        ErrorCode.NO_ELEMENT,
        ErrorCode.MISMATCHED_BRACKET,
        ErrorCode.INVALID_TOKEN,
        ErrorCode.INVALID_ELEMENT,
        ErrorCode.ELEMENT_COUNT_TOO_LARGE,
        ErrorCode.ELEMENT_COUNT_OVERFLOW,
        ErrorCode.WEIGHT_OVERFLOW,
})
public @interface ErrorCode {
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
