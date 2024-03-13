package com.lxy.molweightcalculator.contract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Value<T extends Comparable<T>> {
    public static final Value<Integer> ZERO_I = new Value<>(0);
    public static final Value<Long> ZERO_L = new Value<>(0L);

    @Nullable
    private final String name;
    @NonNull
    private final T value;

    public Value(@NonNull T value) {
        this(null, value);
    }

    public Value(@Nullable String name, @NonNull T value) {
        this.name = name;
        this.value = Contract.requireNonNull(value);
    }

    @NonNull
    @Override
    public String toString() {
        var valueString = value.toString();
        if (name == null) {
            return valueString;
        }
        return name + "(" + valueString + ")";
    }

    @Nullable
    public String getName() {
        return name;
    }

    @NonNull
    public T getValue() {
        return value;
    }
}
