package com.lxy.molweightcalculator.parsing;

import androidx.annotation.IntDef;

@IntDef({ParseError.EMPTY_FORMULA,
        ParseError.INVALID_FORMULA,
        ParseError.INVALID_ELEMENT,
        ParseError.ELEMENT_COUNT_OVERFLOW,
        ParseError.WEIGHT_OVERFLOW})
public @interface ParseError {
    int EMPTY_FORMULA = 0;
    int INVALID_FORMULA = 1;
    int INVALID_ELEMENT = 2;
    int ELEMENT_COUNT_OVERFLOW = 3;
    int WEIGHT_OVERFLOW = 4;
}
