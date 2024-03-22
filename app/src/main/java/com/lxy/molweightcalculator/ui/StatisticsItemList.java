package com.lxy.molweightcalculator.ui;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.parsing.Element;

import java.util.Arrays;
import java.util.Comparator;

public class StatisticsItemList {
    private final StatisticsItem[] items;
    private final double weight;

    public StatisticsItemList(@NonNull StatisticsItemList list) {
        Contract.requireNonNull(list);
        this.weight = list.weight;
        this.items = list.items.clone();
    }

    public StatisticsItemList(int size, double weight) {
        this.weight = weight;
        this.items = new StatisticsItem[size];
    }

    public int getMassRatio(StatisticsItem item) {
        if (weight == 0) {
            // Special case
            return 0;
        }
        // (a * (b * X)) / c
        return (int) (item.getCount() *
                Element.getScaledWeightFromId(item.getElementId()) / weight);
    }

    public int size() {
        return items.length;
    }

    public StatisticsItem get(int index) {
        return items[index];
    }

    public void set(int index, StatisticsItem value) {
        items[index] = value;
    }

    public StatisticsItem[] getItems() {
        return items;
    }

    public void sort(@NonNull Comparator<StatisticsItem> comparator) {
        Arrays.sort(items, 0, size(), Contract.requireNonNull(comparator));
    }
}
