package com.lxy.molweightcalculator.parsing

enum class ErrorCode {
    EmptyFormula,
    NoElement,
    MismatchedBracket,
    InvalidToken,
    InvalidElement,
    ElementCountTooLarge,
    ElementCountOverflow,
    WeightOverflow,
    FormulaTooLong;

    companion object {
        fun valueOf(value: Int): ErrorCode = entries[value]
    }
}