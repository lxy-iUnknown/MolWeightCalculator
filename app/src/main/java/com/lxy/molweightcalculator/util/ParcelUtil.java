package com.lxy.molweightcalculator.util;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Value;
import com.lxy.molweightcalculator.ui.StatisticsItem;
import com.lxy.molweightcalculator.ui.StatisticsItemList;

public class ParcelUtil {
    private ParcelUtil() {

    }

    public static boolean readBoolean(@NonNull Parcel src) {
        Contract.requireNonNull(src);
        return src.readByte() != 0;
    }

    public static void writeBoolean(@NonNull Parcel dest, boolean value) {
        Contract.requireNonNull(dest);
        dest.writeByte(value ? (byte) 1 : (byte) 0);
    }

    public static char readChar(@NonNull Parcel src) {
        Contract.requireNonNull(src);
        int value = src.readInt();
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("char", value),
                    (int) Character.MIN_VALUE, (int) Character.MAX_VALUE);
        }
        return (char) value;
    }

    public static void writeChar(@NonNull Parcel dest, char value) {
        Contract.requireNonNull(dest);
        dest.writeInt(value);
    }

    @NonNull
    public static StatisticsItemList readStatistics(@NonNull Parcel src, double weight) {
        Contract.requireNonNull(src);
        var size = src.readInt();
        var list = new StatisticsItemList(size, weight);
        for (int i = 0; i < size; i++) {
            list.set(i, new StatisticsItem(readChar(src), src.readLong()));
        }
        return list;
    }

    public static void writeStatistics(@NonNull Parcel dest,
                                       @NonNull StatisticsItemList statistics) {
        Contract.requireNonNull(dest);
        Contract.requireNonNull(statistics);
        var size = statistics.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            var item = statistics.get(i);
            writeChar(dest, item.getElementId());
            dest.writeLong(item.getCount());
        }
    }
}
