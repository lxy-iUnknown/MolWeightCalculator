package com.lxy.molweightcalculator.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lxy.molweightcalculator.BuildConfig;

import timber.log.Timber;

public class TimberUtil {
    private static final Object[] EMPTY_ARGS = new Object[0];

    // This method never returns. Just make javac happy
    public static <T> T errorAndThrowException(@Nullable Throwable cause, @NonNull String message) {
        if (BuildConfig.DEBUG) {
            Timber.e(cause, message, EMPTY_ARGS);
        }
        throw new RuntimeException(message, cause);
    }

    // This method never returns. Just make javac happy
    @SuppressWarnings("UnusedReturnValue")
    public static <T> T errorAndThrowException(@NonNull String message) {
        return errorAndThrowException(null, message);
    }

    // This method never returns. Just make javac happy
    public static <T> T unreachable() {
        return errorAndThrowException("Unreachable");
    }
}
