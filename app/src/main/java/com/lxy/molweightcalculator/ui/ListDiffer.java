package com.lxy.molweightcalculator.ui;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;

import com.lxy.molweightcalculator.contract.Contract;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ListDiffer {
    @NonNull
    private static final Executor MAIN_EXECUTOR = new MainThreadExecutor();
    @NonNull
    private static final Executor BACKGROUND_EXECUTOR = Executors.newFixedThreadPool(2);
    @NonNull
    private final ListUpdateCallback updateCallback;
    @Nullable
    private StatisticsItemList list;
    private int maxScheduledGeneration;

    public ListDiffer(@NonNull ListUpdateCallback updateCallback) {
        this.updateCallback = Contract.requireNonNull(updateCallback);
    }

    @Nullable
    public StatisticsItemList getList() {
        return list;
    }

    public void submitList(StatisticsItemList newList) {
        var runGeneration = ++maxScheduledGeneration;
        if (newList == list) {
            return;
        }
        if (newList == null) {
            list = null;
            return;
        }
        if (list == null) {
            list = newList;
            return;
        }
        final var oldList = list;
        BACKGROUND_EXECUTOR.execute(() -> {
            final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldList.size();
                }

                @Override
                public int getNewListSize() {
                    return newList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return oldList.get(oldItemPosition).hashCode() == newList.get(newItemPosition).hashCode();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
                }
            }, true);

            MAIN_EXECUTOR.execute(() -> {
                if (maxScheduledGeneration == runGeneration) {
                    list = newList;
                    result.dispatchUpdatesTo(updateCallback);
                }
            });
        });
    }

    private static class MainThreadExecutor implements Executor {
        final Handler handler = new Handler(Looper.getMainLooper());

        MainThreadExecutor() {
        }

        @Override
        public void execute(@NonNull Runnable command) {
            handler.post(command);
        }
    }
}
