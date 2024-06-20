package com.lxy.molweightcalculator.util;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

// This part is moved to Java due to broken PolymorphicSignature compilation in Kotlin
// See https://youtrack.jetbrains.com/issue/KT-60591/
@SuppressWarnings("unchecked")
public class InvokeUtil {
    @NonNull
    public static <T> T invoke(@NonNull MethodHandle handle) throws Throwable {
        return (T) handle.invoke();
    }

    @NonNull
    public static <T1, T2> Object invoke(
            @NonNull MethodHandle handle,
            @NonNull T1 t1, @NonNull T2 t2
    ) throws Throwable {
        return handle.invoke(t1, t2);
    }

    public static <T1, T2> void invokeVoid(
            @NonNull MethodHandle handle,
            @NonNull T1 t1, @NonNull T2 t2
    ) throws Throwable {
        handle.invoke(t1, t2);
    }

    @NonNull
    public static <T1, T2, T3, T4, R> R invoke(
            @NonNull MethodHandle handle,
            @NonNull T1 t1, @NonNull T2 t2, @NonNull T3 t3, @NonNull T4 t4
    ) throws Throwable {
        return (R) handle.invoke(t1, t2, t3, t4);
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public static class Api33Impl {
        private static final VarHandle BYTE_TO_SHORT =
                MethodHandles.byteArrayViewVarHandle(
                        short[].class, ByteOrder.LITTLE_ENDIAN
                );

        private static final VarHandle BYTE_TO_INT =
                MethodHandles.byteArrayViewVarHandle(
                        int[].class, ByteOrder.LITTLE_ENDIAN
                );

        public static void VarHandle_setShort(@NonNull byte[] array, int index, short value) {
            BYTE_TO_SHORT.set(array, index, value);
        }

        public static void VarHandle_setInt(@NonNull byte[] array, int index, int value) {
            BYTE_TO_INT.set(array, index, value);
        }
    }
}
