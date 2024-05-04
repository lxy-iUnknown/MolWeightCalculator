package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.util.IStatistics;
import com.lxy.molweightcalculator.util.MathUtil;
import com.lxy.molweightcalculator.util.TraverseFunction;
import com.lxy.molweightcalculator.util.Utility;

import java.util.Arrays;

public class ParseState {
    public static final int STATE_SIZE = 8 + // Object
            4 + // keys
            4 + // values
            4 + // size
            4 + // bracket
            4 + // start
            8;  // weight

    public static final int DEFAULT_BRACKET = -1;
    public static final int DEFAULT_START = -1;
    @NonNull
    private static final String[] BRACKET_STRINGS = {
            "(", ")", "[", "]", "{", "}"
    };
    private static final int STATE_ITEM_SIZE = Character.BYTES + Long.BYTES;
    @NonNull
    private static final char[] EMPTY_CHAR = new char[0];
    @NonNull
    private static final long[] EMPTY_LONG = new long[0];
    @NonNull
    private char[] keys;
    @NonNull
    private long[] values;
    private int size;
    private int bracket;
    private int start;
    private double weight;

    public ParseState(int bracket, int start) {
        this.keys = EMPTY_CHAR;
        this.values = EMPTY_LONG;
        reset(bracket, start);
    }

    public static String getBracketString(@Bracket int bracket) {
        return BRACKET_STRINGS[bracket - Bracket.MIN_BRACKET];
    }

    public void reset(int bracket, int start) {
        this.size = 0;
        this.bracket = bracket;
        this.start = start;
        this.weight = 0;
    }

    private void insert(int index, char key, long value) {
        var capacity = capacity();
        if (size + 1 <= capacity) {
            // Keys
            System.arraycopy(keys, index, keys, index + 1, size - index);
            keys[index] = key;
            // Values
            System.arraycopy(values, index, values, index + 1, size - index);
            values[index] = value;
        } else {
            var newSize = Utility.growSize(size);
            MemoryUsage.memoryAllocated(STATE_ITEM_SIZE, newSize);
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

    private int capacity() {
        return keys.length;
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
    public boolean merge(@NonNull ParseState other, long count) {
        int m = size, n = other.size;
        char[] keys1 = keys, keys2 = other.keys;
        long[] values1 = values, values2 = other.values;
        var capacity2 = other.capacity();
        var total = capacity() + capacity2;
        var newKeys = new char[total];
        var newValues = new long[total];
        int i = 0, j = 0, newSize = 0;
        long newValue;
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
                newValue = MathUtil.multiplyAddExact(values1[i++], values2[j++], count);
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
        MemoryUsage.memoryAllocated(STATE_ITEM_SIZE, capacity2);
        keys = newKeys;
        values = newValues;
        size = newSize;
        return true;
    }

    public boolean addValueOrPut(char key, long delta) {
        var i = indexOfKey(key);
        if (i >= 0) {
            var newValue = MathUtil.addExact(valueAt(i), delta);
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

    public int getBracket() {
        return bracket;
    }

    public int getStart() {
        return start;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @NonNull
    @Override
    public String toString() {
        if (!BuildConfig.DEBUG) {
            return super.toString();
        }
        var sb = new StringBuilder("ParseState(statistics=");
        IStatistics.appendStatistics(sb, new IStatistics() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public void forEach(@NonNull TraverseFunction function) {
                for (var i = 0; i < ParseState.this.size(); i++) {
                    function.visit(keyAt(i), valueAt(i));
                }
            }
        });
        return sb.append(", bracket=\"")
                .append(bracket < 0 ? "<no bracket>" : getBracketString(bracket))
                .append("\", start=")
                .append(start)
                .append(", weight=")
                .append(weight)
                .append(')').toString();
    }
}
