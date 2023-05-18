package com.lxy.molweightcalculator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import com.lxy.molweightcalculator.databinding.ActivityMainBinding;
import com.lxy.molweightcalculator.ui.AbstractListAdapter;
import com.lxy.molweightcalculator.ui.MainViewModel;
import com.lxy.molweightcalculator.ui.Row;
import com.lxy.molweightcalculator.ui.StatisticsRow;
import com.lxy.molweightcalculator.parsing.ElementData;
import com.lxy.molweightcalculator.parsing.MolWeightParseResult;
import com.lxy.molweightcalculator.parsing.MolWeightParser;
import com.lxy.molweightcalculator.util.Contract;
import com.lxy.molweightcalculator.util.TimberUtil;
import com.lxy.molweightcalculator.util.Utility;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

import timber.log.Timber;

public class MainActivity extends ComponentActivity {

    private static class CustomTextWatcher implements TextWatcher {
        private MainViewModel model;
        private boolean isOnTextChanged;

        public void setModel(@NonNull MainViewModel model) {
            this.model = Contract.requireNonNull(model);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isOnTextChanged = true;
        }

        @Override
        public void afterTextChanged(Editable s) {
            // https://stackoverflow.com/questions/17535415/textwatcher-events-are-being-fired-multiple-times
            if (isOnTextChanged) {
                MolWeightParseResult result = MolWeightParser.parse(Contract.requireNonNull(s));
                if (BuildConfig.DEBUG) {
                    Timber.d("Parse result: %s", result.debugToString());
                }
                if (result.isSucceeded()) {
                    List<Row> list = model.getNonHeaderRows();
                    list.clear();
                    SparseIntArray statistics = result.getStatistics();
                    float weight = result.getWeight();
                    int size = statistics.size();
                    for (int i = 0; i < size; i++) {
                        char element = (char) statistics.keyAt(i);
                        int count = statistics.valueAt(i);
                        list.add(new StatisticsRow(count, element,
                                count * ElementData.getMolecularWeight(element) / weight));
                    }
                }
                model.getResult().setValue(result);
                isOnTextChanged = false;
            }
        }
    }

    @NonNull
    private static final DialogInterface.OnClickListener STATISTICS_DIALOG_CONFIRM_LISTENER =
            (dialog, which) -> dialog.dismiss();

    @NonNull
    @SuppressWarnings("unchecked")
    private static final Comparator<Row>[][] COMPARATORS = new Comparator[][] {
            // -1
            new Comparator[] { null, null, null, null, null },
            // Ascending
            new Comparator[] {
                    null, // -1
                    getAscendingComparator(StatisticsRow::getElementId),  // Cannot have equal value
                    getAscendingComparator(StatisticsRow::getOrdinal),    // Cannot have equal value
                    getAscendingComparatorWithDefault(StatisticsRow::getCount),
                    getAscendingComparatorWithDefault(StatisticsRow::getFixedPointRatio),
            },
            // Descending
            new Comparator[] {
                    null, // -1
                    getDescendingComparator(StatisticsRow::getElementId), // Cannot have equal value
                    getDescendingComparator(StatisticsRow::getOrdinal),   // Cannot have equal value
                    getDescendingComparatorWithDefault(StatisticsRow::getCount),
                    getDescendingComparatorWithDefault(StatisticsRow::getFixedPointRatio),
            },
    };
    @NonNull
    private static final CustomTextWatcher TEXT_WATCHER = new CustomTextWatcher();
    @NonNull
    private static final String UNSPECIFIED = "Unspecified";
    private ActivityMainBinding binding;
    private MainViewModel model;

    @NonNull
    private static <T> Comparator<T> getAscendingComparator(@NonNull ToIntFunction<T> keyExtractor) {
        return Comparator.comparingInt(keyExtractor);
    }

    @NonNull
    private static <T> Comparator<T> getDescendingComparator(@NonNull ToIntFunction<T> keyExtractor) {
        return (t1, t2) -> Integer.compare(keyExtractor.applyAsInt(t2), keyExtractor.applyAsInt(t1));
    }

    @NonNull
    private static Comparator<StatisticsRow> getAscendingComparatorWithDefault(
            @NonNull ToIntFunction<StatisticsRow> keyExtractor) {
        Contract.requireNonNull(keyExtractor);
        return (row1, row2) -> {
            int key1 = keyExtractor.applyAsInt(row1);
            int key2 = keyExtractor.applyAsInt(row2);
            if (key1 == key2) {
                return Integer.compare(
                        Contract.requireNonNull(row1).getElementId(),
                        Contract.requireNonNull(row2).getElementId());
            }
            return key1 > key2 ? 1 : -1;
        };
    }

    @NonNull
    private static Comparator<StatisticsRow> getDescendingComparatorWithDefault(
            @NonNull ToIntFunction<StatisticsRow> keyExtractor) {
        Contract.requireNonNull(keyExtractor);
        return (row1, row2) -> {
            int key1 = keyExtractor.applyAsInt(row1);
            int key2 = keyExtractor.applyAsInt(row2);
            if (key1 == key2) {
                return Integer.compare(
                        Contract.requireNonNull(row2).getElementId(),
                        Contract.requireNonNull(row1).getElementId());
            }
            return key2 > key1 ? 1 : -1;
        };
    }

    @NonNull
    private static String sortMethodToString(@Nullable Integer sortOrder) {
        if (sortOrder == null) {
            return UNSPECIFIED;
        }
        int value = sortOrder;
        switch (value) {
            case 0:
                return "Default";
            case 1:
                return "Element ordinal";
            case 2:
                return "Element count";
            case 3:
                return "Mass ratio";
            default:
                return TimberUtil.unreachable();
        }
    }

    @NonNull
    private static String sortOrderToString(@Nullable Integer sortMethod) {
        if (sortMethod == null) {
            return UNSPECIFIED;
        }
        int value = sortMethod;
        switch (value) {
            case 0:
                return "Ascending";
            case 1:
                return "Descending";
            default:
                return TimberUtil.unreachable();
        }
    }

    private static int unboxPosition(@Nullable Integer position) {
        if (position == null) {
            return AdapterView.INVALID_POSITION;
        }
        return position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        MainViewModel model = initializeModel();
        AbstractListAdapter<Row> adapter = initializeAdapter(model);
        initializeBinding(binding, model, adapter, initializeDialog(adapter));

        this.binding = binding;
        this.model = model;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomTextWatcher watcher = TEXT_WATCHER;
        watcher.setModel(model);
        // https://blog.danlew.net/2011/10/31/text_watchers_and_on_restore_instance_state_/
        binding.editFormula.addTextChangedListener(watcher);
    }

    @NonNull
    private MainViewModel initializeModel() {
        if (BuildConfig.DEBUG) {
            Timber.d("Initialize model");
        }
        return new ViewModelProvider(this, Utility.kotlinCast(
                new SavedStateViewModelFactory(getApplication(), this)))
                .get(MainViewModel.class);
    }

    @NonNull
    private AbstractListAdapter<Row> initializeAdapter(@NonNull MainViewModel model) {
        if (BuildConfig.DEBUG) {
            Timber.d("Initialize adapter");
        }
        return new AbstractListAdapter<Row>(this, R.layout.statistics_row,
                Contract.requireNonNull(model).getRows()) {

            @Override
            protected void bindView(@NonNull View view, int position) {
                Row row = get(position);
                Contract.<TextView>requireNonNull(
                                view.findViewById(R.id.element_name_column))
                        .setText(row.elementNameString());
                Contract.<TextView>requireNonNull(
                                view.findViewById(R.id.element_count_column))
                        .setText(row.elementCountString());
                Contract.<TextView>requireNonNull(
                                view.findViewById(R.id.mass_ratio_column))
                        .setText(row.massRatioString());
            }
        };
    }

    @NonNull
    private AlertDialog initializeDialog(@NonNull AbstractListAdapter<Row> adapter) {
        if (BuildConfig.DEBUG) {
            Timber.d("Initialize dialog");
        }
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.statistics_dialog_title)
                .setAdapter(adapter, null)
                .setPositiveButton(R.string.confirm, STATISTICS_DIALOG_CONFIRM_LISTENER)
                .create();
    }

    private void initializeBinding(@NonNull ActivityMainBinding binding,
                                   @NonNull MainViewModel model,
                                   @NonNull AbstractListAdapter<Row> adapter,
                                   @NonNull AlertDialog dialog) {
        if (BuildConfig.DEBUG) {
            Timber.d("Initialize binding");
            Timber.d("Sort order: %s, Sort method: %s",
                    sortMethodToString(model.getSortMethod().getValue()),
                    sortOrderToString(model.getSortOrder().getValue()));
        }
        Contract.requireNonNull(binding);
        Contract.requireNonNull(model);
        Contract.requireNonNull(adapter);
        Contract.requireNonNull(dialog);

        binding.setModel(model);
        binding.showStatisticsDialog.setOnClickListener(v -> {
            Comparator<Row> comparator = COMPARATORS
                    [unboxPosition(model.getSortOrder().getValue()) + 1]
                    [unboxPosition(model.getSortMethod().getValue()) + 1];
            if (BuildConfig.DEBUG) {
                Timber.d("Comparator: %s", comparator);
            }
            if (comparator != null /* null indicates not selected */ ) {
                model.getNonHeaderRows().sort(comparator);
            }
            adapter.notifyDataSetChanged();
            dialog.show();
        });
        binding.setLifecycleOwner(this);
    }
}