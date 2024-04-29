package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.ui.StatisticsItem;
import com.lxy.molweightcalculator.ui.StatisticsItemList;
import com.lxy.molweightcalculator.util.Utility;

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
public class FormulaParser {
    private static final int LEFT_BRACKET = 0;
    private static final int RIGHT_BRACKET = 1;
    private static final int LEFT_SQUARE_BRACKET = 2;
    private static final int RIGHT_SQUARE_BRACKET = 3;
    private static final int LEFT_CURLY_BRACKET = 4;
    private static final int RIGHT_CURLY_BRACKET = 5;
    private static final int PURGE_THRESHOLD = 128 * 1024; // 64 KB
    @NonNull
    private static final FormulaParser PARSER = new FormulaParser();
    @NonNull
    private final StateStack stack = new StateStack(Utility.INITIAL_CAPACITY);
    private CharSequence formula;
    private int length;
    private int i;

    @NonNull
    public static FormulaParseResult parse(@NonNull CharSequence formula) {
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
        if (MemoryUsage.getUsage() > PURGE_THRESHOLD) {
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
            return (char) (firstChar - 'A');
        }
        var secondChar = formula.charAt(i);
        if (secondChar < 'a' || secondChar > 'z') {
            return (char) (firstChar - 'A');
        }
        i++;
        return (char) (firstChar * 26 + secondChar - (('A' - 1) * 26 + 'a'));
    }

    private void handleLeftBracket(int bracket) {
        stack.push(bracket, i++);
    }

    @Nullable
    private FormulaParseResult handleRightBracket(int leftBracket, int rightBracket) {
        var top1 = stack.pop();
        var top2 = stack.peekNoThrow();
        if (top2 == null || top1.bracket != leftBracket) {
            return new FormulaParseResult(i, rightBracket, ParseErrorCode.MISMATCHED_BRACKET);
        }
        if (top1.statistics.size() == 0) {
            return new FormulaParseResult(i - 1, i, ParseErrorCode.NO_ELEMENT);
        }
        i++;
        var start = i;
        var count = parseQuantity();
        if (count < 0) {
            return new FormulaParseResult(start, i, ParseErrorCode.ELEMENT_COUNT_TOO_LARGE);
        }
        if (!top2.statistics.merge(top1.statistics, count)) {
            return FormulaParseResult.ELEMENT_COUNT_OVERFLOW;
        }
        var weight = top2.weight + top1.weight * count;
        if (!Double.isFinite(weight)) {
            return FormulaParseResult.ELEMENT_COUNT_OVERFLOW;
        }
        top2.weight = weight;
        return null;
    }

    @NonNull
    private FormulaParseResult parse() {
        if (length == 0) {
            if (BuildConfig.DEBUG) {
                Timber.d("Empty formula");
            }
            return FormulaParseResult.EMPTY_FORMULA;
        }
        stack.push(-1, -1);
        while (i < length) {
            // Inlined parseCompound
            var start = i;
            var elementId = parseElementId();
            if ((short) elementId >= 0) {
                var state = stack.peek();
                var quantity = parseQuantity();
                if (quantity < 0) {
                    return new FormulaParseResult(start, i, ParseErrorCode.ELEMENT_COUNT_TOO_LARGE);
                }
                if (!state.statistics.addValueOrPut(elementId, quantity)) {
                    return FormulaParseResult.ELEMENT_COUNT_OVERFLOW;
                }
                var elementWeight = Element.getWeightFromId(elementId);
                if (Double.isNaN(elementWeight)) {
                    if (BuildConfig.DEBUG) {
                        Timber.d("Invalid element: %s",
                                Element.getElementNameFromId(elementId));
                    }
                    return new FormulaParseResult(start, i, ParseErrorCode.INVALID_ELEMENT);
                }
                var weight = state.weight + elementWeight * quantity;
                if (!Double.isFinite(weight)) {
                    return FormulaParseResult.ELEMENT_COUNT_OVERFLOW;
                }
                state.weight = weight;
            } else {
                var ch = formula.charAt(i);
                switch (ch) {
                    case '(' -> handleLeftBracket(LEFT_BRACKET);
                    case '[' -> handleLeftBracket(LEFT_SQUARE_BRACKET);
                    case '{' -> handleLeftBracket(LEFT_CURLY_BRACKET);
                    case ')' -> {
                        var parseResult = handleRightBracket(LEFT_BRACKET, RIGHT_BRACKET);
                        if (parseResult != null) {
                            return parseResult;
                        }
                    }
                    case ']' -> {
                        var parseResult = handleRightBracket(LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET);
                        if (parseResult != null) {
                            return parseResult;
                        }
                    }
                    case '}' -> {
                        var parseResult = handleRightBracket(LEFT_CURLY_BRACKET, RIGHT_CURLY_BRACKET);
                        if (parseResult != null) {
                            return parseResult;
                        }
                    }
                    default -> {
                        return new FormulaParseResult(i, i + 1, ParseErrorCode.INVALID_TOKEN);
                    }
                }
            }
        }
        var state = stack.pop();
        if (!stack.isEmpty()) {
            return new FormulaParseResult(state.start, state.bracket,
                    ParseErrorCode.MISMATCHED_BRACKET);
        }
        tryPurgeMemory();
        var statistics = state.statistics;
        var weight = state.weight;
        var list = new StatisticsItemList(statistics.size(), weight);
        var size = statistics.size();
        for (var i = 0; i < size; i++) {
            list.set(i, new StatisticsItem(statistics.keyAt(i), statistics.valueAt(i)));
        }
        return new FormulaParseResult(weight, list);
    }
}
