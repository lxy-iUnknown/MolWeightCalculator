package com.lxy.molweightcalculator.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.R;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Value;
import com.lxy.molweightcalculator.parsing.Element;
import com.lxy.molweightcalculator.util.GlobalContext;
import com.lxy.molweightcalculator.util.Utility;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StaticsViewHolder> {
    public static final int COLUMN_COUNT = 3;
    private static final int HEADER_COUNT = 1;
    private static final int MASS_RATIO_COLUMN = 2;
    @NonNull
    private static final String ELEMENT_NAME;
    @NonNull
    private static final String ELEMENT_COUNT;
    @NonNull
    private static final String MASS_RATIO;

    static {
        var resources = GlobalContext.get().getResources();
        ELEMENT_NAME = resources.getString(R.string.element_name);
        ELEMENT_COUNT = resources.getString(R.string.element_count);
        MASS_RATIO = resources.getString(R.string.mass_ratio);
    }

    @NonNull
    private final LayoutInflater layoutInflater;
    @NonNull
    private final ListDiffer differ;

    public StatisticsAdapter(@NonNull Context context) {
        this.layoutInflater = LayoutInflater.from(Contract.requireNonNull(context));
        this.differ = new ListDiffer(new ListUpdateCallback() {

            private static int convert(int value) {
                return value * COLUMN_COUNT;
            }

            // convertPosition1 and convertPosition2 are identical
            // to help ProGuard inline
            private static int convertPosition1(int position) {
                return convert(position + HEADER_COUNT);
            }

            private static int convertPosition2(int position) {
                return convert(position + HEADER_COUNT);
            }

            // Mass ratio is lazily calculated
            private void recalculateMassRatio(int position) {
                for (int i = 0; i < position; i++) {
                    notifyItemChanged(convertPosition1(i) + MASS_RATIO_COLUMN);
                }
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(convertPosition1(position), convert(count));
                recalculateMassRatio(position);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(convertPosition1(position), convert(count));
                recalculateMassRatio(position);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemRangeRemoved(convertPosition2(fromPosition), COLUMN_COUNT);
                notifyItemRangeInserted(convertPosition2(toPosition), COLUMN_COUNT);
            }

            @Override
            public void onChanged(int position, int count, @Nullable Object payload) {
                notifyItemRangeChanged(convertPosition2(position), convert(count), payload);
            }
        });
    }

    private static int appendDigit(@NonNull char[] buffer, int index, int digit) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("digit", digit), 0, 9);
        }
        buffer[index++] = (char) ('0' + digit);
        return index;
    }

    private static int appendTwoDigits(@NonNull char[] buffer, int index, int value) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("value", value), 0, 99);
        }
        // 45
        var div = value / 10; // 4
        return appendDigit(buffer, appendDigit(buffer, index, div), value - div * 10); // 5
    }

    private static int appendPercentIntegerPart(@NonNull char[] buffer, int index, int value) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("value", value), 0, 100);
        }
        if (value < 10) {
            return appendDigit(buffer, index, value);
        } else if (value < 100) {
            return appendTwoDigits(buffer, index, value);
        } else {
            // 100
            buffer[index++] = '1';
            buffer[index++] = '0';
            buffer[index++] = '0';
            return index;
        }
    }

    @NonNull
    private static String massRatioString(
            @NonNull StatisticsItemList list, @NonNull StatisticsItem item) {
        final int MAX_CHAR_COUNT = 3 /* integer part */ +
                1 /* decimal point */ +
                4 /* fraction part */ +
                1 /* percent char */;

        Contract.requireNonNull(list);
        Contract.requireNonNull(item);
        var fixedPointRatio = list.getMassRatio(item);
        var buffer = new char[MAX_CHAR_COUNT];
        var index = 0;
        int div, rem;
        // 123456 -> 12.3456%(0.123456)
        div = fixedPointRatio / Utility.FIXED_POINT_MULTIPLIER; // 12
        rem = fixedPointRatio - div * Utility.FIXED_POINT_MULTIPLIER; // 3456
        index = appendPercentIntegerPart(buffer, index, div);
        buffer[index++] = '.';
        div = rem / 100; // 34
        index = appendTwoDigits(buffer, appendTwoDigits(buffer, index, div) /* 34 */,
                rem - div * 100 /* 56 */);
        buffer[index++] = '%';
        return new String(buffer, 0, index);
    }

    private static String invalidColumn(int column) {
        return Contract.unreachable("Invalid column ", column);
    }

    @NonNull
    @Override
    public StaticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StaticsViewHolder(layoutInflater.inflate(R.layout.table_item, parent, false));
    }

    @NonNull
    public StatisticsItemList getList() {
        return Contract.requireNonNull(differ.getList());
    }

    @Override
    public void onBindViewHolder(@NonNull StaticsViewHolder holder, int position) {
        var list = getList();
        var row = position / COLUMN_COUNT;
        var column = position - row * COLUMN_COUNT;
        if (row == 0) {
            holder.setText(switch (column) {
                case 0 -> ELEMENT_NAME;
                case 1 -> ELEMENT_COUNT;
                case 2 -> MASS_RATIO;
                default -> invalidColumn(column);
            });
        } else {
            var item = list.get(row - HEADER_COUNT);
            holder.setText(switch (column) {
                case 0 -> Element.getElementNameFromId(item.getElementId());
                case 1 -> String.valueOf(item.getCount());
                case 2 -> massRatioString(list, item);
                default -> invalidColumn(column);
            });
        }
    }

    @Override
    public int getItemCount() {
        return (getList().size() + HEADER_COUNT) * COLUMN_COUNT;
    }

    public void submitList(@NonNull StatisticsItemList list) {
        differ.submitList(Contract.requireNonNull(list));
    }

    public static class StaticsViewHolder extends RecyclerView.ViewHolder {
        public StaticsViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setText(String text) {
            ((TextView) itemView).setText(text);
        }
    }
}
