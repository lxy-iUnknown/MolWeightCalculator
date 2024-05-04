package com.lxy.molweightcalculator.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.lxy.molweightcalculator.R;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.parsing.Element;
import com.lxy.molweightcalculator.util.GlobalContext;

import java.util.Comparator;
import java.util.List;

public class StatisticsAdapter extends RecyclerView.Adapter<StaticsViewHolder> {
    private static final long FIXED_POINT_PERCENT_MULTIPLIER
            = MassRatio.FIXED_POINT_MULTIPLIER * 100L;
    public static final int COLUMN_COUNT = 3;
    public static final int HEADER_COUNT = 1;
    public static final int MASS_RATIO_COLUMN = 2;
    @NonNull
    private static final CustomListUpdateCallback ITEM_UPDATE_CALLBACK;
    @NonNull
    private static final AsyncListDiffer<StatisticsItem> ASYNC_LIST_DIFFER;
    @NonNull
    private static final String ELEMENT_NAME;
    @NonNull
    private static final String ELEMENT_COUNT;
    @NonNull
    private static final String MASS_RATIO;

    static {
        var callback = new CustomListUpdateCallback();
        var differ = new AsyncListDiffer<>(callback,
                new AsyncDifferConfig.Builder<>(new DiffUtil.ItemCallback<StatisticsItem>() {
                    @Override
                    public boolean areItemsTheSame(
                            @NonNull StatisticsItem oldItem, @NonNull StatisticsItem newItem) {
                        return oldItem.hashCode() == newItem.hashCode();
                    }

                    @Override
                    public boolean areContentsTheSame(
                            @NonNull StatisticsItem oldItem, @NonNull StatisticsItem newItem) {
                        return oldItem.equals(newItem);
                    }
                }).build());
        ITEM_UPDATE_CALLBACK = callback;
        ASYNC_LIST_DIFFER = differ;
    }

    static {
        var resources = GlobalContext.get().getResources();
        ELEMENT_NAME = resources.getString(R.string.element_name);
        ELEMENT_COUNT = resources.getString(R.string.element_count);
        MASS_RATIO = resources.getString(R.string.mass_ratio);
    }

    @NonNull
    private final LayoutInflater layoutInflater;
    @NonNull
    private final Comparator<StatisticsItem> ratioAscendingComparator;
    @NonNull
    private final Comparator<StatisticsItem> ratioDescendingComparator;
    private double weight;

    public StatisticsAdapter(@NonNull Context context) {
        layoutInflater = LayoutInflater.from(Contract.requireNonNull(context));
        ratioAscendingComparator = this::ratioCompareKey;
        ratioDescendingComparator = (o1, o2) -> ratioCompareKey(o2, o1);
        ITEM_UPDATE_CALLBACK.setAdapter(this);
    }

    @NonNull
    private static String invalidColumn(int column) {
        return Contract.fail("Invalid column ", column);
    }

    private int ratioCompareKey(@NonNull StatisticsItem o1, @NonNull StatisticsItem o2) {
        var key1 = getMassRatio(o1);
        var key2 = getMassRatio(o2);
        if (key1 == key2) {
            return Integer.compare(o1.getElementId(), o2.getElementId());
        }
        return key1 > key2 ? 1 : -1;
    }

    public int getMassRatio(@NonNull StatisticsItem item) {
        if (weight == 0) {
            // Special case
            return 0;
        }
        return (int) ((item.getCount() *
                Element.getWeightFromId(item.getElementId()) *
                FIXED_POINT_PERCENT_MULTIPLIER) / weight);
    }

    @NonNull
    @Override
    public StaticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StaticsViewHolder(layoutInflater.inflate(
                R.layout.table_item, parent, false));
    }

    @NonNull
    public List<StatisticsItem> getList() {
        return ASYNC_LIST_DIFFER.getCurrentList();
    }

    public double getWeight() {
        return weight;
    }

    @NonNull
    public Comparator<StatisticsItem> getRatioAscendingComparator() {
        return ratioAscendingComparator;
    }

    @NonNull
    public Comparator<StatisticsItem> getRatioDescendingComparator() {
        return ratioDescendingComparator;
    }

    @Override
    public void onBindViewHolder(@NonNull StaticsViewHolder holder, int position) {
        var list = getList();
        var row = position / COLUMN_COUNT;
        var column = position - row * COLUMN_COUNT;
        var textView = holder.getTextView();
        if (row == 0) {
            textView.setText(switch (column) {
                case 0 -> ELEMENT_NAME;
                case 1 -> ELEMENT_COUNT;
                case 2 -> MASS_RATIO;
                default -> invalidColumn(column);
            });
        } else {
            var item = list.get(row - HEADER_COUNT);
            textView.setText(switch (column) {
                case 0 -> Element.getElementNameFromId(item.getElementId());
                case 1 -> String.valueOf(item.getCount());
                case 2 -> MassRatio.massRatioString(getMassRatio(item));
                default -> invalidColumn(column);
            });
        }
    }

    @Override
    public int getItemCount() {
        return (getList().size() + HEADER_COUNT) * COLUMN_COUNT;
    }

    public void submitList(@NonNull List<StatisticsItem> list, double weight) {
        ASYNC_LIST_DIFFER.submitList(Contract.requireNonNull(list));
        this.weight = weight;
    }
}
