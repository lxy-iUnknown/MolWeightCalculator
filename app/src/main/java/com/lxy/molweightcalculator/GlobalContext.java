package com.lxy.molweightcalculator;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.lxy.molweightcalculator.util.Contract;
import com.lxy.molweightcalculator.util.Utility;

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

    @Override
    public void onCreate() {
        super.onCreate();
        context = Contract.requireNonNull(getApplicationContext());
    }

    @NonNull
    public static Context get() {
        if (BuildConfig.DEBUG) {
            return Contract.requireNonNull(context);
        }
        return context;
    }

    @NonNull
    public static Resources getResource() {
        return Contract.requireNonNull(get().getResources());
    }

    @NonNull
    public static String getResourceString(@StringRes int id) {
        return Contract.requireNonNull(getResource().getString(id));
    }

    @NonNull
    public static String getResourceString(@StringRes int id, @NonNull Object... args) {
        return Contract.requireNonNull(getResource().getString(id, args));
    }
}
