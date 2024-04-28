package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
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

    // Simple algorithm for merging two sorted array
    public boolean merge(@NonNull StatisticsMap other, long count) {
        var m = size;
        var n = other.size;
        var keys1 = keys;
        var keys2 = other.keys;
        var values1 = values;
        var values2 = other.values;
        var total = m + n;
        var newKeys = new char[total];
        var newValues = new long[total];
        long newValue;
        int i = 0, j = 0, newSize = 0;
        while (i < m && j < n) {
            var key1 = keys1[i];
            var key2 = keys2[j];
            if (key1 < key2) {
                newKeys[newSize] = key1;
                newValues[newSize] = values1[i++];
            } else if (key1 > key2) {
                newValue = MathUtil.multiplyExact(values2[j++], count);
                if (newValue < 0) {
                    return false;
                }
                newKeys[newSize] = key2;
                newValues[newSize] = newValue;
            } else {
                newValue = MathUtil.multiplyExact(values2[j++], count);
                if (newValue < 0) {
                    return false;
                }
                newValue = MathUtil.addExact(values1[i++], newValue);
                if (newValue < 0) {
                    return false;
                }
                newKeys[newSize] = key1;
                newValues[newSize] = newValue;
            }
            newSize++;
        }
        while (i < m) {
            newKeys[newSize] = keys1[i];
            newValues[newSize++] = values1[i++];
        }
        while (j < n) {
            newValue = MathUtil.multiplyExact(values2[j], count);
            if (newValue < 0) {
                return false;
            }
            newKeys[newSize] = keys2[j++];
            newValues[newSize++] = newValue;
        }
        keys = newKeys;
        values = newValues;
        size = newSize;
        return true;
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
}
