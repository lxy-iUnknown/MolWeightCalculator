package com.lxy.molweightcalculator.util;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.compose.runtime.snapshots.SnapshotStateList;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

import kotlin.jvm.functions.Function1;

// This part is moved to Java due to broken PolymorphicSignature compilation in Kotlin
// See https://youtrack.jetbrains.com/issue/KT-60591/
public class InvokeUtil {

    @NonNull
    public static <T, U> Object SnapshotList_mutate(
            @NonNull MethodHandle handle,
            @NonNull SnapshotStateList<T> list,
            @NonNull Function1<U, Object> builder) throws Throwable {
        return handle.invoke(list, builder);
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
