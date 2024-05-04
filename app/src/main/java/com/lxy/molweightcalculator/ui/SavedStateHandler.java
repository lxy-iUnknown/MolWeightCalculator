package com.lxy.molweightcalculator.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.lxy.molweightcalculator.contract.Contract;

public class SavedStateHandler<T> {
    @NonNull
    private final String key;
    @Nullable
    private final T defaultValue;

    public SavedStateHandler(@NonNull String key, @Nullable T defaultValue) {
        this.key = Contract.requireNonNull(key);
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
