package com.lxy.molweightcalculator.parsing;

import android.app.ActivityManager;
import android.content.Context;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.util.GlobalContext;
import com.lxy.molweightcalculator.util.MathUtil;

import timber.log.Timber;

public class MemoryUsage {
    private static final long PURGE_THRESHOLD;

    static {
        final var KB = 1024;
        final var MB = KB * KB;
        final var GB = MB * KB;

        var am = (ActivityManager) GlobalContext.get().getSystemService(Context.ACTIVITY_SERVICE);
        var memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        var available = memoryInfo.availMem;
        if (BuildConfig.DEBUG) {
            if (available < KB) {
                Timber.d("Available memory: %dB", available);
            } else if (available < MB) {
                Timber.d("Available memory: %.3fKB", available / (double) KB);
            } else if (available < GB) {
                Timber.d("Available memory: %.3fMB", available / (double) MB);
            } else {
                Timber.d("Available memory: %.3fGB", available / (double) GB);
            }
        }
        // Default threshold: Available memory / 256
        // Minimum threshold: 64KB
        PURGE_THRESHOLD = Math.min(available >>> 8, 64 * 1024);
    }

    private static long USAGE;

    private MemoryUsage() {
    }

    public static void memoryAllocated(long size, int count) {
        if (BuildConfig.DEBUG) {
            Contract.require(MathUtil.multiplyAddExact(USAGE, size, count) > 0,
                    "Overflow occurred when calculating memory usage");
        }
        USAGE += size * count;
    }

    public static boolean shouldPurge() {
        return USAGE > PURGE_THRESHOLD;
    }

    public static void reset() {
        USAGE = 0;
    }
}
