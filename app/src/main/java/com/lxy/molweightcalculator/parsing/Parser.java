package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.ui.StatisticsItem;

import java.util.ArrayList;

import timber.log.Timber;

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
public class Parser {
    @NonNull
    private static final Parser PARSER = new Parser();
    @NonNull
    private final ParseStack stack = new ParseStack();
    private CharSequence formula;
    private int length;
    private int i;

    @NonNull
    public static ParseResult parse(@NonNull CharSequence formula) {
        var parser = PARSER;
        parser.init(formula);
        return parser.parse();
    }

    private void init(@NonNull CharSequence formula) {
        this.formula = Contract.requireNonNull(formula);
        this.length = formula.length();
        this.stack.clear();
        this.i = 0;
    }

    private void tryPurgeMemory() {
        if (MemoryUsage.shouldPurge()) {
            stack.purgeMemory();
            MemoryUsage.reset();
        }
    }

    private long parseQuantity() {
        final int RADIX = 10;
        final long LIMIT = -Long.MAX_VALUE;
        final long MULTIPLY_LIMIT = LIMIT / RADIX;

        var count = 0;
        var value = 0L;
        while (i < length) {
            var ch = formula.charAt(i);
            if (ch < '0' || ch > '9') {
                break;
            } else {
                count++;
                i++;
                if (value < MULTIPLY_LIMIT) {
                    return -1;
                }
                var digit = ch - '0';
                value *= RADIX;
                if (value < LIMIT + digit) {
                    return -1;
                }
                value -= digit;
            }
        }
        return count == 0 ? 1 : -value;
    }

    private char parseElementId() {
        var firstChar = formula.charAt(i);
        if (firstChar < 'A' || firstChar > 'Z') {
            return Character.MAX_VALUE;
        }
        if (++i == length) {
            // EOF
            return Element.parseElementId(firstChar);
        }
        var secondChar = formula.charAt(i);
        if (secondChar < 'a' || secondChar > 'z') {
            return Element.parseElementId(firstChar);
        }
        i++;
        return Element.parseElementId(firstChar, secondChar);
    }

    private void handleLeftBracket(@Bracket int bracket) {
        stack.push(bracket, i++);
    }

    @Nullable
    private ParseResult handleRightBracket(@Bracket int leftBracket, @Bracket int rightBracket) {
        var top1 = stack.pop();
        var top2 = stack.peekNoThrow();
        if (top2 == null || top1.getBracket() != leftBracket) {
            return new ParseResult(i, rightBracket, ErrorCode.MISMATCHED_BRACKET);
        }
        if (top1.size() == 0) {
            return new ParseResult(i - 1, i, ErrorCode.NO_ELEMENT);
        }
        i++;
        var start = i;
        var count = parseQuantity();
        if (count < 0) {
            return new ParseResult(start, i, ErrorCode.ELEMENT_COUNT_TOO_LARGE);
        }
        if (!top2.merge(top1, count)) {
            return ParseResult.ELEMENT_COUNT_OVERFLOW;
        }
        var weight = top2.getWeight() + top1.getWeight() * count;
        if (!Double.isFinite(weight)) {
            return ParseResult.ELEMENT_COUNT_OVERFLOW;
        }
        top2.setWeight(weight);
        return null;
    }

    @NonNull
    private ParseResult parse() {
        if (length == 0) {
            if (BuildConfig.DEBUG) {
                Timber.d("Empty formula");
            }
            return ParseResult.EMPTY_FORMULA;
        }
        stack.push(ParseState.DEFAULT_BRACKET, ParseState.DEFAULT_START);
        while (i < length) {
            // Inlined parseCompound
            var start = i;
            var elementId = parseElementId();
            if ((short) elementId >= 0) {
                var state = stack.peek();
                var quantity = parseQuantity();
                if (quantity < 0) {
                    return new ParseResult(start, i, ErrorCode.ELEMENT_COUNT_TOO_LARGE);
                }
                if (!state.addValueOrPut(elementId, quantity)) {
                    return ParseResult.ELEMENT_COUNT_OVERFLOW;
                }
                var elementWeight = Element.getWeightFromId(elementId);
                if (Double.isNaN(elementWeight)) {
                    if (BuildConfig.DEBUG) {
                        Timber.d("Invalid element: %s",
                                Element.getElementNameFromId(elementId));
                    }
                    return new ParseResult(start, i, ErrorCode.INVALID_ELEMENT);
                }
                var weight = state.getWeight() + elementWeight * quantity;
                if (!Double.isFinite(weight)) {
                    return ParseResult.ELEMENT_COUNT_OVERFLOW;
                }
                state.setWeight(weight);
            } else {
                var ch = formula.charAt(i);
                switch (ch) {
                    case '(' -> handleLeftBracket(Bracket.LEFT_BRACKET);
                    case '[' -> handleLeftBracket(Bracket.LEFT_SQUARE_BRACKET);
                    case '{' -> handleLeftBracket(Bracket.LEFT_CURLY_BRACKET);
                    case ')' -> {
                        var parseResult = handleRightBracket(Bracket.LEFT_BRACKET, Bracket.RIGHT_BRACKET);
                        if (parseResult != null) {
                            return parseResult;
                        }
                    }
                    case ']' -> {
                        var parseResult = handleRightBracket(Bracket.LEFT_SQUARE_BRACKET, Bracket.RIGHT_SQUARE_BRACKET);
                        if (parseResult != null) {
                            return parseResult;
                        }
                    }
                    case '}' -> {
                        var parseResult = handleRightBracket(Bracket.LEFT_CURLY_BRACKET, Bracket.RIGHT_CURLY_BRACKET);
                        if (parseResult != null) {
                            return parseResult;
                        }
                    }
                    default -> {
                        return new ParseResult(i, i + 1, ErrorCode.INVALID_TOKEN);
                    }
                }
            }
        }
        var state = stack.pop();
        if (!stack.isEmpty()) {
            return new ParseResult(state.getStart(), state.getBracket(),
                    ErrorCode.MISMATCHED_BRACKET);
        }
        tryPurgeMemory();
        var weight = state.getWeight();
        var list = new ArrayList<StatisticsItem>(state.size());
        var size = state.size();
        for (var i = 0; i < size; i++) {
            list.add(i, new StatisticsItem(state.keyAt(i), state.valueAt(i)));
        }
        return new ParseResult(list, weight);
    }
}
