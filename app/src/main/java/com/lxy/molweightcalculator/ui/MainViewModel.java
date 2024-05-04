package com.lxy.molweightcalculator.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.lxy.molweightcalculator.parsing.ParseResult;
import com.lxy.molweightcalculator.util.Utility;

public class MainViewModel extends ViewModel {
    public static final int NO_COMPARATOR_POSITION = 0;
    @NonNull
    private final SavedStateHandler<Integer> precision =
            new SavedStateHandler<>("Precision", Utility.MAX_PRECISION / 2);
    @NonNull
    private final SavedStateHandler<ParseResult> result =
            new SavedStateHandler<>("Result",
                    ParseResult.EMPTY_FORMULA);
    @NonNull
    private final SavedStateHandler<String> formula =
            new SavedStateHandler<>("Formula", "");
    @NonNull
    private final SavedStateHandler<Integer> sortOrder =
            new SavedStateHandler<>("SortOrder", NO_COMPARATOR_POSITION);
    @NonNull
    private final SavedStateHandler<Integer> sortMethod =
            new SavedStateHandler<>("SortMethod", NO_COMPARATOR_POSITION);
    @NonNull
    private final SavedStateHandle handle;

    public MainViewModel(@NonNull SavedStateHandle handle) {
        this.handle = handle;
    }

    @NonNull
    public MutableLiveData<Integer> getPrecision() {
        return precision.getLiveData(handle);
    }

    @NonNull
    public MutableLiveData<ParseResult> getResult() {
        return result.getLiveData(handle);
    }

    @NonNull
    public MutableLiveData<String> getFormula() {
        return formula.getLiveData(handle);
    }

    @NonNull
    public MutableLiveData<Integer> getSortMethod() {
        return sortMethod.getLiveData(handle);
    }

    @NonNull
    public MutableLiveData<Integer> getSortOrder() {
        return sortOrder.getLiveData(handle);
    }
}