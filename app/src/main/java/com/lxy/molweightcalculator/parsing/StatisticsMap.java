package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Operator;
import com.lxy.molweightcalculator.contract.Value;
import com.lxy.molweightcalculator.util.IStatistics;
import com.lxy.molweightcalculator.util.MathUtil;
import com.lxy.molweightcalculator.util.TraverseFunction;
import com.lxy.molweightcalculator.util.Utility;

import java.util.Arrays;

public class StatisticsMap {
    @NonNull
    private static final char[] EMPTY_CHAR = new char[0];
    @NonNull
    private static final long[] EMPTY_LONG = new long[0];
    @NonNull
    private char[] keys;
    @NonNull
    private long[] values;
    private int size;

    public StatisticsMap(int initialCapacity) {
        Utility.checkInitialCapacity(initialCapacity);
        if (initialCapacity == 0) {
            keys = EMPTY_CHAR;
            values = EMPTY_LONG;
        } else {
            MemoryUsage.allocate(Character.BYTES + Long.BYTES, initialCapacity);
            values = new long[initialCapacity];
            keys = new char[initialCapacity];
        }
        size = 0;
    }

    private void insert(int index, char key, long value) {
        var capacity = keys.length;
        if (size + 1 <= capacity) {
            // Keys
            System.arraycopy(keys, index, keys, index + 1, size - index);
            keys[index] = key;
            // Values
            System.arraycopy(values, index, values, index + 1, size - index);
            values[index] = value;
        } else {
            var newSize = Utility.growSize(size);
            MemoryUsage.allocate(Character.BYTES + Long.BYTES, newSize);
            // Keys
            var newKeys = new char[newSize];
            System.arraycopy(keys, 0, newKeys, 0, index);
            newKeys[index] = key;
            System.arraycopy(keys, index, newKeys, index + 1, capacity - index);
            keys = newKeys;
            // Values
            var newValues = new long[newSize];
            System.arraycopy(values, 0, newValues, 0, index);
            newValues[index] = value;
            System.arraycopy(values, index, newValues, index + 1, capacity - index);
            values = newValues;
        }
    }

    public int indexOfKey(char key) {
        return Arrays.binarySearch(keys, 0, size, key);
    }

    @SuppressWarnings("unused")
    public long get(char key, long defaultValue) {
        var index = indexOfKey(key);
        if (index >= 0) {
            return valueAt(index);
        }
        return defaultValue;
    }

    @SuppressWarnings("unused")
    public void replaceAll(@NonNull ReplaceFunction function) {
        Contract.requireNonNull(function);
        for (int i = 0; i < size(); i++) {
            setValueAt(i, function.replace(valueAt(i)));
        }
    }

    @SuppressWarnings("unused")
    public void put(char key, long value) {
        int i = indexOfKey(key);
        if (i >= 0) {
            setValueAt(i, value);
        } else {
            i = ~i;
            insert(i, key, value);
            size++;
        }
    }

    public boolean addValueOrPut(char key, long delta) {
        int i = indexOfKey(key);
        if (i >= 0) {
            long newValue = MathUtil.addExact(valueAt(i), delta);
            if (newValue < 0) {
                return false;
            }
            setValueAt(i, newValue);
        } else {
            i = ~i;
            insert(i, key, delta);
            size++;
        }
        return true;
    }

    public char keyAt(int index) {
        return keys[index];
    }

    public long valueAt(int index) {
        return values[index];
    }

    public void setValueAt(int index, long value) {
        values[index] = value;
    }

    public int size() {
        return size;
    }

    @NonNull
    @Override
    public String toString() {
        if (!BuildConfig.DEBUG) {
            return super.toString();
        }
        var sb = new StringBuilder();
        IStatistics.appendStatistics(sb, new IStatistics() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public void forEach(@NonNull TraverseFunction function) {
                for (int i = 0; i < StatisticsMap.this.size(); i++) {
                    function.visit(keyAt(i), valueAt(i));
                }
            }
        });
        return sb.toString();
    }

    public void clear() {
        size = 0;
    }

    public interface ReplaceFunction {
        long replace(long value);
    }
}
