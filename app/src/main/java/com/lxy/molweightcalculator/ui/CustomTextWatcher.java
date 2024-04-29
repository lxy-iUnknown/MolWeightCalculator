package com.lxy.molweightcalculator.ui;

import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Operator;
import com.lxy.molweightcalculator.contract.Value;
import com.lxy.molweightcalculator.parsing.FormulaParseResult;
import com.lxy.molweightcalculator.parsing.FormulaParser;

import timber.log.Timber;

public class CustomTextWatcher implements TextWatcher {
    @NonNull
    private static final UnderlineSpan UNDERLINE = new UnderlineSpan();

    @NonNull
    private final MainViewModel model;
    private boolean isOnTextChanged;

    public CustomTextWatcher(@NonNull MainViewModel model) {
        this.model = Contract.requireNonNull(model);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        isOnTextChanged = true;
    }

    @Override
    public void afterTextChanged(Editable s) {
        // https://stackoverflow.com/questions/17535415/textwatcher-events-are-being-fired-multiple-times
        if (isOnTextChanged) {
            s.removeSpan(UNDERLINE);
            var result = FormulaParser.parse(s);
            if (BuildConfig.DEBUG) {
                Timber.d("Parse result: %s", result.debugToString());
            }
            if (!result.isSucceeded()) {
                var errorCode = result.getErrorCode();
                if (FormulaParseResult.hasStartEnd(errorCode)) {
                    var startEnd = result.getStartEnd();
                    var start = FormulaParseResult.extractStart(startEnd);
                    var end = FormulaParseResult.isInvalidBracket(errorCode) ?
                            start + 1 : FormulaParseResult.extractEnd(startEnd);
                    if (BuildConfig.DEBUG) {
                        var startValue = new Value<>("start", start);
                        var endValue = new Value<>("end", end);
                        var lengthValue = new Value<>("length", s.length());
                        Contract.requireOperation(startValue, endValue, Operator.LT);
                        Contract.requireOperation(startValue, Value.ZERO_I, Operator.GE);
                        Contract.requireOperation(endValue, lengthValue, Operator.LE);
                    }
                    s.setSpan(UNDERLINE, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            model.getResult().setValue(result);
            isOnTextChanged = false;
        }
    }
}
