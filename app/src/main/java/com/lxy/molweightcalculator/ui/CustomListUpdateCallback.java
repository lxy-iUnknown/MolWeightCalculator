package com.lxy.molweightcalculator.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListUpdateCallback;

import com.lxy.molweightcalculator.contract.Contract;

public class CustomListUpdateCallback implements ListUpdateCallback {
    private StatisticsAdapter adapter;

    private static int convert(int value) {
        return value * StatisticsAdapter.COLUMN_COUNT;
    }

    // convertPosition1 and convertPosition2 are identical
    // to help ProGuard inline
    private static int convertPosition1(int position) {
        return convert(position + StatisticsAdapter.HEADER_COUNT);
    }

    private static int convertPosition2(int position) {
        return convert(position + StatisticsAdapter.HEADER_COUNT);
    }

    public void setAdapter(@NonNull StatisticsAdapter adapter) {
        this.adapter = Contract.requireNonNull(adapter);
    }

    // Mass ratio is lazily calculated
    private void recalculateMassRatio(int position) {
        for (var i = 0; i < position; i++) {
            adapter.notifyItemChanged(convertPosition1(i) +
                    StatisticsAdapter.MASS_RATIO_COLUMN);
        }
    }

    @Override
    public void onInserted(int position, int count) {
        adapter.notifyItemRangeInserted(convertPosition1(position), convert(count));
        recalculateMassRatio(position);
    }

    @Override
    public void onRemoved(int position, int count) {
        adapter.notifyItemRangeRemoved(convertPosition1(position), convert(count));
        recalculateMassRatio(position);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        adapter.notifyItemRangeRemoved(convertPosition2(fromPosition),
                StatisticsAdapter.COLUMN_COUNT);
        adapter.notifyItemRangeInserted(convertPosition2(toPosition),
                StatisticsAdapter.COLUMN_COUNT);
    }

    @Override
    public void onChanged(int position, int count, @Nullable Object payload) {
        adapter.notifyItemRangeChanged(convertPosition2(position), convert(count), payload);
    }
}
