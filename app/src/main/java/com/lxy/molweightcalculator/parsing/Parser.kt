package com.lxy.molweightcalculator.parsing

import com.lxy.molweightcalculator.BuildConfig
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
object Parser {
    private val parseStack = ParseStack()
    private lateinit var formula: CharSequence
    private var length = 0
    private var index = 0

    private fun init(formula: CharSequence, length: Int) {
        this.formula = formula
        this.length = length
        this.parseStack.clear()
        this.index = 0
    }

    private fun tryPurgeMemory() {
        if (MemoryUsage.shouldPurge()) {
            parseStack.purgeMemory()
            MemoryUsage.reset()
        }
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
        parseStack.push(bracket, index++)
    }

    private fun handleRightBracket(
        leftBracket: Bracket,
        rightBracket: Bracket
    ): ParseResult? {
        val top1 = parseStack.pop()
        val top2 = parseStack.peekNoThrow()
        if (top2 == null || top1.bracket != leftBracket) {
            return ParseResult(index, rightBracket.ordinal, ErrorCode.MismatchedBracket)
        }
        if (top1.size() == 0) {
            return ParseResult(index - 1, index, ErrorCode.NoElement)
        }
        index++
        val start = index
        val count = parseQuantity()
        if (count < 0) {
            return ParseResult(start, index, ErrorCode.ElementCountTooLarge)
        }
        if (!top2.merge(top1, count)) {
            return ParseResult.ELEMENT_COUNT_OVERFLOW
        }
        val weight = top2.weight + top1.weight * count
        if (!weight.isFinite()) {
            return ParseResult.WEIGHT_OVERFLOW
        }
        top2.weight = weight
        return null
    }

    private fun parse(): ParseResult {
        parseStack.push(ParseState.DEFAULT_BRACKET, ParseState.DEFAULT_START)
        while (index < length) {
            // Inlined parseCompound
            val start = index
            val elementId = parseElementId()
            if (elementId.isValid) {
                val state = parseStack.peek()
                val quantity = parseQuantity()
                if (quantity < 0) {
                    return ParseResult(start, index, ErrorCode.ElementCountTooLarge)
                }
                if (!state.addValueOrPut(elementId.value, quantity)) {
                    return ParseResult.ELEMENT_COUNT_OVERFLOW
                }
                val elementWeight = elementId.weight
                if (elementWeight.isNaN()) {
                    if (BuildConfig.DEBUG) {
                        Timber.d("Invalid element: %s", elementId.elementName)
                    }
                    return ParseResult(start, index, ErrorCode.InvalidElement)
                }
                val weight = state.weight + elementWeight * quantity
                if (!weight.isFinite()) {
                    return ParseResult.WEIGHT_OVERFLOW
                }
                state.weight = weight
            } else {
                val ch = formula[index]
                when (ch) {
                    '(' -> handleLeftBracket(Bracket.LeftBracket)
                    '[' -> handleLeftBracket(Bracket.LeftSquareBracket)
                    '{' -> handleLeftBracket(Bracket.LeftCurlyBracket)
                    ')' -> {
                        val parseResult = handleRightBracket(
                            Bracket.LeftBracket,
                            Bracket.RightBracket
                        )
                        if (parseResult != null) {
                            return parseResult
                        }
                    }

                    ']' -> {
                        val parseResult = handleRightBracket(
                            Bracket.LeftSquareBracket,
                            Bracket.RightSquareBracket
                        )
                        if (parseResult != null) {
                            return parseResult
                        }
                    }

                    '}' -> {
                        val parseResult = handleRightBracket(
                            Bracket.LeftCurlyBracket,
                            Bracket.RightCurlyBracket
                        )
                        if (parseResult != null) {
                            return parseResult
                        }
                    }

                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                        return ParseResult(index, index + 1, ErrorCode.NoElement)
                    }

                    else -> {
                        return ParseResult(index, index + 1, ErrorCode.InvalidToken)
                    }
                }
            }
        }
        val state = parseStack.pop()
        if (!parseStack.isEmpty) {
            return ParseResult(
                state.start, state.bracket.ordinal,
                ErrorCode.MismatchedBracket
            )
        }
        tryPurgeMemory()
        val weight = state.weight
        val list = ArrayList<StatisticsItem>(state.size())
        val size = state.size()
        for (i in 0 until size) {
            list.add(i, StatisticsItem(ElementId(state.keyAt(i)), state.valueAt(i)))
        }
        return ParseResult(list, weight)
    }

    private const val RADIX = 10L
    private const val LIMIT = -Long.MAX_VALUE
    private const val MULTIPLY_LIMIT = LIMIT / RADIX

    fun parse(formula: CharSequence): ParseResult {
        val length = formula.length
        if (length == 0) {
            return ParseResult.EMPTY_FORMULA
        }
        if (!ParseResult.canParse(length)) {
            return ParseResult.FORMULA_TOO_LONG
        }
        init(formula, length)
        return parse()
    }
}