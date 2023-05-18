package com.lxy.molweightcalculator.ui;

import androidx.annotation.NonNull;

public interface Row {
    @NonNull
    String elementNameString();

    @NonNull
    String elementCountString();

    @NonNull
    String massRatioString();
}
