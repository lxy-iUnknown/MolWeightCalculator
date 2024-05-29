package com.lxy.molweightcalculator.parsing

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.util.Utility
import timber.log.Timber

// Handwritten recursive descent/LL(1) parser
// Grammar:
// Non-terminals:
//     Formula = Compound+
//     Compound = Atom Quantity?
//                | "(" Compound ")" Quantity?
//                | "[" Compound "]" Quantity?
//                | "{" Compound "}" Quantity?
// Terminals:
//     Atom: ("A" .. "Z")("a" .. "z")?
//     Quantity: ("0" .. "9")+
class Parser(private val formula: CharSequence) {
    // Parse stack
    private var parseStack = arrayOfNulls<ParseState?>(Utility.INITIAL_CAPACITY)
    private var stackTop = -1

    // Parse information
    private var length = formula.length
    private var index = 0

    companion object {
        private const val RADIX = 10L
        private const val LIMIT = -Long.MAX_VALUE
        private const val MULTIPLY_LIMIT = LIMIT / RADIX
    }

    private fun checkStack() {
        if (stackIsEmpty) {
            Contract.fail("Stack is empty")
        }
    }

    private val stackIsEmpty get() = stackSize() < 0

    private fun stackGetItem(index: Int): ParseState {
        return parseStack[index]!!
    }

    private fun stackPeekUnchecked(): ParseState {
        return stackGetItem(stackTop)
    }

    private fun stackPeek(): ParseState {
        checkStack()
        return stackPeekUnchecked()
    }

    private fun stackPop(): ParseState {
        checkStack()
        return stackGetItem(stackTop--)
    }

    private fun stackPeekNoThrow(): ParseState? {
        return if (stackIsEmpty) null else stackPeekUnchecked()
    }

    private fun stackSize(): Int {
        return stackTop
    }

    private fun stackPush(bracket: Bracket, start: Int) {
        val s = stackSize() + 1
        if (s == parseStack.size) {
            parseStack = parseStack.copyOf(Utility.growSize(s))
        }
        val state = parseStack[s]
        if (state != null) {
            state.reset(bracket, start)
        } else {
            parseStack[s] = ParseState(bracket, start)
        }
        stackTop = s
    }

    private fun parseQuantity(): Long {
        var count = 0
        var value = 0L
        while (index < length) {
            val ch = formula[index]
            if (ch < '0' || ch > '9') {
                break
            } else {
                count++
                index++
                if (value < MULTIPLY_LIMIT) {
                    return -1
                }
                val digit = (ch.code - '0'.code).toLong()
                value *= RADIX
                if (value < LIMIT + digit) {
                    return -1
                }
                value -= digit
            }
        }
        return if (count == 0) 1 else -value
    }

    private fun parseElementId(): ElementId {
        val firstChar = formula[index]
        if (firstChar < 'A' || firstChar > 'Z') {
            return ElementId.INVALID
        }
        if (++index == length) {
            // EOF
            return ElementId.valueOf(firstChar)
        }
        val secondChar = formula[index]
        if (secondChar < 'a' || secondChar > 'z') {
            return ElementId.valueOf(firstChar)
        }
        index++
        return ElementId.valueOf(firstChar, secondChar)
    }

    private fun handleLeftBracket(bracket: Bracket) {
        stackPush(bracket, index++)
    }

    private fun handleRightBracket(
        leftBracket: Bracket,
        rightBracket: Bracket,
        parseResult: ParseResult
    ): Boolean {
        val top1 = stackPop()
        val top2 = stackPeekNoThrow()
        if (top2 == null || top1.bracket != leftBracket) {
            parseResult.init(index, rightBracket.ordinal, ErrorCode.MismatchedBracket)
            return false
        }
        if (top1.size() == 0) {
            parseResult.init(index - 1, index, ErrorCode.NoElement)
            return false
        }
        index++
        val start = index
        val count = parseQuantity()
        if (count < 0) {
            parseResult.init(start, index, ErrorCode.ElementCountTooLarge)
            return false
        }
        if (!top2.merge(top1, count)) {
            parseResult.init(ErrorCode.ElementCountOverflow)
            return false
        }
        val weight = top2.weight + top1.weight * count
        if (!weight.isFinite()) {
            parseResult.init(ErrorCode.WeightOverflow)
            return false
        }
        top2.weight = weight
        return true
    }

    fun parse(parseResult: ParseResult) {
        if (length == 0) {
            parseResult.init(ErrorCode.EmptyFormula)
            return
        }
        if (!ParseResult.canParse(length)) {
            parseResult.init(ErrorCode.FormulaTooLong)
            return
        }
        stackPush(Bracket.Invalid, -1)
        while (index < length) {
            // Inlined parseCompound
            val start = index
            val elementId = parseElementId()
            if (elementId.isValid) {
                val state = stackPeek()
                val quantity = parseQuantity()
                if (quantity < 0) {
                    parseResult.init(start, index, ErrorCode.ElementCountTooLarge)
                    return
                }
                if (!state.addValueOrPut(elementId.value, quantity)) {
                    parseResult.init(ErrorCode.ElementCountOverflow)
                    return
                }
                val elementWeight = elementId.weight
                if (elementWeight.isNaN()) {
                    if (BuildConfig.DEBUG) {
                        Timber.d("Invalid element: %s", elementId.elementName)
                    }
                    parseResult.init(start, index, ErrorCode.InvalidElement)
                    return
                }
                val weight = state.weight + elementWeight * quantity
                if (!weight.isFinite()) {
                    parseResult.init(ErrorCode.WeightOverflow)
                    return
                }
                state.weight = weight
            } else {
                val ch = formula[index]
                when (ch) {
                    '(' -> handleLeftBracket(Bracket.LeftBracket)
                    '[' -> handleLeftBracket(Bracket.LeftSquareBracket)
                    '{' -> handleLeftBracket(Bracket.LeftCurlyBracket)
                    ')' -> {
                        if (!handleRightBracket(
                                Bracket.LeftBracket,
                                Bracket.RightBracket,
                                parseResult
                            )
                        ) {
                            return
                        }
                    }

                    ']' -> {
                        if (!handleRightBracket(
                                Bracket.LeftSquareBracket,
                                Bracket.RightSquareBracket,
                                parseResult
                            )
                        ) {
                            return
                        }
                    }

                    '}' -> {
                        if (!handleRightBracket(
                                Bracket.LeftCurlyBracket,
                                Bracket.RightCurlyBracket,
                                parseResult
                            )
                        ) {
                            return
                        }
                    }

                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                        parseResult.init(index, index + 1, ErrorCode.NoElement)
                        return
                    }

                    else -> {
                        parseResult.init(index, index + 1, ErrorCode.InvalidToken)
                        return
                    }
                }
            }
        }
        val state = stackPop()
        if (!stackIsEmpty) {
            parseResult.init(
                state.start, state.bracket.ordinal,
                ErrorCode.MismatchedBracket
            )
            return
        }
        parseResult.init(state)
    }
}