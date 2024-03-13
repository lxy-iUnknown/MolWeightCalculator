package com.lxy.molweightcalculator.util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;

import timber.log.Timber;

public class GlobalContext extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context = null;

    static {
        if (BuildConfig.DEBUG) {
            Timber.Tree tree = Utility.kotlinCast(new Timber.DebugTree());
            Timber.plant(tree);
        }
    }

    @NonNull
    public static Context get() {
        return Contract.requireNonNull(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = Contract.requireNonNull(getApplicationContext());
    }
}
