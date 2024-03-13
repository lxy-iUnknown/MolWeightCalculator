package com.lxy.molweightcalculator.ui;

import androidx.annotation.Nullable;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Operator;
import com.lxy.molweightcalculator.contract.Value;
import com.lxy.molweightcalculator.parsing.Element;
import com.lxy.molweightcalculator.util.HashUtil;

@SuppressWarnings("ClassCanBeRecord")
public class StatisticsItem {
    private final char elementId;
    private final long count;

    public StatisticsItem(char elementId, long count) {
        if (BuildConfig.DEBUG) {
            Contract.requireOperation(new Value<>("count", count), Value.ZERO_L, Operator.GE);
        }
        this.elementId = elementId;
        this.count = count;
    }

    public char getElementId() {
        return elementId;
    }

    public long getCount() {
        return count;
    }

    public int getOrdinal() {
        return Element.getOrdinalFromId(getElementId());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof StatisticsItem statisticsItem) {
            return statisticsItem.elementId == elementId &&
                    statisticsItem.count == count;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashUtil.mix(Integer.hashCode(elementId), Long.hashCode(count));
    }
}
