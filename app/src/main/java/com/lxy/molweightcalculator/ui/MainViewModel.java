package com.lxy.molweightcalculator.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.lxy.molweightcalculator.GlobalContext;
import com.lxy.molweightcalculator.R;
import com.lxy.molweightcalculator.parsing.MolWeightParseResult;
import com.lxy.molweightcalculator.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    private static class SavedStateHandler<T> {
        @NonNull
        private final String key;
        @Nullable
        private final T defaultValue;

        public SavedStateHandler(@NonNull String key, @Nullable T defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @NonNull
        public MutableLiveData<T> getLiveData(@NonNull SavedStateHandle handle) {
            if (!handle.contains(key)) {
                handle.set(key, defaultValue);
            }
            return handle.getLiveData(key);
        }
    }

    private static class HeaderRow implements Row {
        public static final HeaderRow INSTANCE = new HeaderRow();

        private HeaderRow() {

        }

        @NonNull
        private static final String ELEMENT_NAME = GlobalContext.getResourceString(R.string.element_name);

        @NonNull
        private static final String ELEMENT_COUNT = GlobalContext.getResourceString(R.string.element_count);

        @NonNull
        private static final String MASS_RATIO = GlobalContext.getResourceString(R.string.mass_ratio);

        @NonNull
        @Override
        public String elementNameString() {
            return ELEMENT_NAME;
        }

        @NonNull
        @Override
        public String elementCountString() {
            return ELEMENT_COUNT;
        }

        @NonNull
        @Override
        public String massRatioString() {
            return MASS_RATIO;
        }
    }

    public static final int NO_COMPARATOR_POSITION = 0;

    private final @NonNull SavedStateHandler<Integer> precision =
            new SavedStateHandler<>("Precision", Utility.MAX_PRECISION / 2);
    private final @NonNull SavedStateHandler<MolWeightParseResult> result =
            new SavedStateHandler<>("Result",
                    MolWeightParseResult.EMPTY_FORMULA);
    private final @NonNull SavedStateHandler<String> formula =
            new SavedStateHandler<>("Formula", "");
    private final @NonNull SavedStateHandler<Integer> sortOrder =
            new SavedStateHandler<>("SortOrder", NO_COMPARATOR_POSITION);
    private final @NonNull SavedStateHandler<Integer> sortMethod =
            new SavedStateHandler<>("SortMethod", NO_COMPARATOR_POSITION);

    private final @NonNull List<Row> rows;
    private final @NonNull SavedStateHandle handle;

    public MainViewModel(@NonNull SavedStateHandle handle) {
        List<Row> rows = new ArrayList<>(Utility.INITIAL_CAPACITY);
        rows.add(HeaderRow.INSTANCE);
        this.handle = handle;
        this.rows = rows;
    }

    @NonNull
    public MutableLiveData<Integer> getPrecision() {
        return precision.getLiveData(handle);
    }

    @NonNull
    public MutableLiveData<MolWeightParseResult> getResult() {
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

    @NonNull
    public List<Row> getRows() {
        return rows;
    }

    @NonNull
    public List<Row> getNonHeaderRows() {
        return rows.subList(1, rows.size());
    }
}