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
        private val NO_START_END_MASK = (1 shl EmptyFormula.ordinal) or
                (1 shl ElementCountOverflow.ordinal) or
                (1 shl WeightOverflow.ordinal) or
                (1 shl FormulaTooLong.ordinal)

        fun valueOf(value: Int): ErrorCode = entries[value]
    }

    val isInvalidBracket get() = this == MismatchedBracket

    val hasStartEnd get() = ((1 shl ordinal) and NO_START_END_MASK) == 0
}