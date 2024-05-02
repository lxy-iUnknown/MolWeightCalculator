package com.lxy.molweightcalculator.parsing;

import androidx.annotation.IntDef;

@IntDef({Bracket.LEFT_BRACKET,
        Bracket.RIGHT_BRACKET,
        Bracket.LEFT_SQUARE_BRACKET,
        Bracket.RIGHT_SQUARE_BRACKET,
        Bracket.LEFT_CURLY_BRACKET,
        Bracket.RIGHT_CURLY_BRACKET
})
public @interface Bracket {
    int LEFT_BRACKET = 0;
    int RIGHT_BRACKET = 1;
    int LEFT_SQUARE_BRACKET = 2;
    int RIGHT_SQUARE_BRACKET = 3;
    int LEFT_CURLY_BRACKET = 4;
    int RIGHT_CURLY_BRACKET = 5;
    int MIN_BRACKET = LEFT_BRACKET;
}
