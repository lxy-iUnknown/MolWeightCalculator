package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.util.Contract;
import com.lxy.molweightcalculator.util.Utility;

public class ElementInfo {
    @NonNull
    public static final ElementInfo INVALID = new ElementInfo(0, 0);

    private final int ordinal;
    private final int molecularWeight;

    private ElementInfo(int ordinal, int molecularWeight) {
        this.ordinal = ordinal;
        this.molecularWeight = molecularWeight;
    }

    @NonNull
    public static ElementInfo of(int ordinal, int molecularWeight) {
        if (BuildConfig.DEBUG) {
            Contract.require(ordinal > 0, "Invalid ordinal");
            Contract.require(molecularWeight > 0, "Invalid molecular weight");
        }
        return new ElementInfo(ordinal, molecularWeight);
    }

    public int getOrdinal() {
        return ordinal;
    }

    public float getMolecularWeight() {
        return Utility.fixedToFloat(molecularWeight);
    }

    public boolean isValid() {
        return ordinal > 0;
    }
}
