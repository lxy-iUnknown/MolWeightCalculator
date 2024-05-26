package com.lxy.molweightcalculator.util;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

// This part is moved to Java due to broken PolymorphicSignature compilation in Kotlin
// See https://youtrack.jetbrains.com/issue/KT-60591/
public class FormatUtil {
    private static final boolean VAR_HANDLE_AVAILABLE =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;

    @SuppressWarnings("deprecation")
    @NonNull
    public static String asciiToString(@NonNull byte[] array, int start, int size) {
        return new String(array, 0, start, size);
    }

    public static boolean varHandleAvailable() {
        return VAR_HANDLE_AVAILABLE;
    }

    @SuppressLint("NewApi")
    public static void setShort(@NonNull byte[] array, int index, short value) {
        Api33Impl.setShort(array, index, value);
    }

    @SuppressLint("NewApi")
    public static void setInt(@NonNull byte[] array, int index, int value) {
        Api33Impl.setInt(array, index, value);
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private static class Api33Impl {
        private static final VarHandle BYTE_TO_SHORT =
                MethodHandles.byteArrayViewVarHandle(
                        short[].class, ByteOrder.LITTLE_ENDIAN);
        private static final VarHandle BYTE_TO_INT =
                MethodHandles.byteArrayViewVarHandle(
                        int[].class, ByteOrder.LITTLE_ENDIAN);

        public static void setShort(@NonNull byte[] array, int index, short value) {
            BYTE_TO_SHORT.set(array, index, value);
        }

        public static void setInt(@NonNull byte[] array, int index, int value) {
            BYTE_TO_INT.set(array, index, value);
        }
    }
}
