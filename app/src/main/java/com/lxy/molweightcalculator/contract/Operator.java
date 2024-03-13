package com.lxy.molweightcalculator.contract;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public abstract class Operator {
    public static final Operator EQ = new Operator(" ≠ ") {
        @Override
        public <T extends Comparable<T>> boolean test(@NonNull T left, @NonNull T right) {
            return left.equals(right);
        }
    };

    public static final Operator NE = new Operator(" = ") {
        @Override
        public <T extends Comparable<T>> boolean test(@NonNull T left, @NonNull T right) {
            return !left.equals(right);
        }
    };

    public static final Operator GT = new Operator(" ≤ ") {
        @Override
        public <T extends Comparable<T>> boolean test(@NonNull T left, @NonNull T right) {
            return left.compareTo(right) > 0;
        }
    };

    public static final Operator GE = new Operator(" < ") {
        @Override
        public <T extends Comparable<T>> boolean test(@NonNull T left, @NonNull T right) {
            return left.compareTo(right) >= 0;
        }
    };


    public static final Operator LT = new Operator(" ≥ ") {
        @Override
        public <T extends Comparable<T>> boolean test(@NonNull T left, @NonNull T right) {
            return left.compareTo(right) < 0;
        }
    };


    public static final Operator LE = new Operator(" > ") {
        @Override
        public <T extends Comparable<T>> boolean test(@NonNull T left, @NonNull T right) {
            return left.compareTo(right) <= 0;
        }
    };

    private final String negatedOpName;

    protected Operator(String negatedOpName) {
        this.negatedOpName = negatedOpName;
    }

    public String getNegatedOpName() {
        return negatedOpName;
    }

    public abstract <T extends Comparable<T>> boolean test(@NonNull T left, @NonNull T right);
}