package com.lxy.molweightcalculator;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.databinding.ActivityMainBinding;
import com.lxy.molweightcalculator.ui.CustomTextWatcher;
import com.lxy.molweightcalculator.ui.MainViewModel;
import com.lxy.molweightcalculator.ui.StatisticsAdapter;
import com.lxy.molweightcalculator.ui.StatisticsItem;
import com.lxy.molweightcalculator.util.GlobalContext;
import com.lxy.molweightcalculator.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import timber.log.Timber;

public class MainActivity extends ComponentActivity {
    // Sort order constants
    private static final int SORT_ORDER_UNDEFINED = -1;
    private static final int SORT_ORDER_ASCENDING = 0;
    private static final int SORT_ORDER_DESCENDING = 1;
    // Sort method constants
    private static final int SORT_METHOD_UNDEFINED = -1;
    private static final int SORT_METHOD_DEFAULT = 0;
    private static final int SORT_METHOD_ORDINAL = 1;
    private static final int SORT_METHOD_ELEMENT_COUNT = 2;
    private static final int SORT_METHOD_MASS_RATIO = 3;
    @NonNull
    private static final Comparator<StatisticsItem> ELEMENT_ID_ASCENDING =
            MainActivity::elementIdCompareKey;
    @NonNull
    private static final Comparator<StatisticsItem> ELEMENT_ID_DESCENDING =
            (o1, o2) -> MainActivity.elementIdCompareKey(o2, o1);
    @NonNull
    private static final Comparator<StatisticsItem> ORDINAL_ASCENDING =
            MainActivity::ordinalCompareKey;
    @NonNull
    private static final Comparator<StatisticsItem> ORDINAL_DESCENDING =
            (o1, o2) -> MainActivity.ordinalCompareKey(o2, o1);
    @NonNull
    private static final Comparator<StatisticsItem> ELEMENT_COUNT_ASCENDING =
            MainActivity::countCompareKey;
    @NonNull
    private static final Comparator<StatisticsItem> ELEMENT_COUNT_DESCENDING =
            (o1, o2) -> MainActivity.countCompareKey(o2, o1);
    @NonNull
    private static final String UNSPECIFIED = "Unspecified";
    private static final int ERROR_TEXT_COLOR = GlobalContext.get()
            .getColor(R.color.error_text_color);

    private ActivityMainBinding binding;
    private MainViewModel model;


    @NonNull
    private static String sortOrderToString(@Nullable Integer sortMethod) {
        if (sortMethod == null) {
            return UNSPECIFIED;
        }
        int value = sortMethod;
        return switch (value) {
            case 0 -> "Ascending";
            case 1 -> "Descending";
            default -> invalidSortMethod(value);
        };
    }

    @NonNull
    private static String sortMethodToString(@Nullable Integer sortOrder) {
        if (sortOrder == null) {
            return UNSPECIFIED;
        }
        int value = sortOrder;
        return switch (value) {
            case 0 -> "Default";
            case 1 -> "Element ordinal";
            case 2 -> "Element count";
            case 3 -> "Mass ratio";
            default -> invalidSortOrder(sortOrder);
        };
    }

    private static int elementIdCompareKey(@NonNull StatisticsItem o1, @NonNull StatisticsItem o2) {
        return Integer.compare(
                Contract.requireNonNull(o1).getElementId(),
                Contract.requireNonNull(o2).getElementId()
        );
    }

    private static int ordinalCompareKey(@NonNull StatisticsItem o1, @NonNull StatisticsItem o2) {
        return Integer.compare(
                Contract.requireNonNull(o1).getOrdinal(),
                Contract.requireNonNull(o2).getOrdinal()
        );
    }

    private static int countCompareKey(@NonNull StatisticsItem o1, @NonNull StatisticsItem o2) {
        var key1 = Contract.requireNonNull(o1).getCount();
        var key2 = Contract.requireNonNull(o2).getCount();
        if (key1 == key2) {
            return Integer.compare(o1.getElementId(), o2.getElementId());
        }
        return key1 > key2 ? 1 : -1;
    }

    @NonNull
    private static <T> T invalidSortMethod(int sortMethod) {
        return Contract.fail("Invalid sort method ", sortMethod);
    }

    @NonNull
    private static <T> T invalidSortOrder(int sortOrder) {
        return Contract.fail("Invalid sort order ", sortOrder);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityMainBinding binding = DataBindingUtil.setContentView(
                this, R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        var model = initializeModel();
        var adapter = new StatisticsAdapter(this);
        initializeBinding(binding, model, adapter);
        initializeObserver(model, adapter);
        this.binding = binding;
        this.model = model;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // https://blog.danlew.net/2011/10/31/text_watchers_and_on_restore_instance_state_/
        binding.editFormula.addTextChangedListener(new CustomTextWatcher(model));
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

    private void sortStatistics(@NonNull StatisticsAdapter adapter,
            @Nullable Integer sortOrder, @Nullable Integer sortMethod) {
        Contract.requireNonNull(adapter);
        if (sortOrder == null) {
            return;
        }
        if (sortMethod == null) {
            return;
        }
        var sortOrderValue = (int) sortOrder;
        var sortMethodValue = (int) sortMethod;
        var comparator = switch (sortOrderValue) {
            case SORT_ORDER_UNDEFINED -> null;
            case SORT_ORDER_ASCENDING -> switch (sortMethodValue) {
                case SORT_METHOD_UNDEFINED -> null;
                case SORT_METHOD_DEFAULT -> ELEMENT_ID_ASCENDING;
                case SORT_METHOD_ORDINAL -> ORDINAL_ASCENDING;
                case SORT_METHOD_ELEMENT_COUNT -> ELEMENT_COUNT_ASCENDING;
                case SORT_METHOD_MASS_RATIO -> adapter.getRatioAscendingComparator();
                default -> MainActivity.<Comparator<StatisticsItem>>
                        invalidSortMethod(sortMethodValue);
            };
            case SORT_ORDER_DESCENDING -> switch (sortMethodValue) {
                case SORT_METHOD_UNDEFINED -> null;
                case SORT_METHOD_DEFAULT -> ELEMENT_ID_DESCENDING;
                case SORT_METHOD_ORDINAL -> ORDINAL_DESCENDING;
                case SORT_METHOD_ELEMENT_COUNT -> ELEMENT_COUNT_DESCENDING;
                case SORT_METHOD_MASS_RATIO -> adapter.getRatioDescendingComparator();
                default -> MainActivity.<Comparator<StatisticsItem>>
                        invalidSortMethod(sortMethodValue);
            };
            default -> MainActivity.<Comparator<StatisticsItem>>
                    invalidSortOrder(sortOrderValue);
        };
        var list = new ArrayList<>(adapter.getList());
        list.sort(comparator);
        adapter.submitList(list, adapter.getWeight());
    }

    private void initializeBinding(@NonNull ActivityMainBinding binding,
                                   @NonNull MainViewModel model,
                                   @NonNull StatisticsAdapter adapter) {
        if (BuildConfig.DEBUG) {
            Timber.d("Initialize binding");
        }
        Contract.requireNonNull(binding);
        Contract.requireNonNull(model);
        Contract.requireNonNull(adapter);

        binding.setModel(model);
        binding.setTextColor(binding.textMolecularWeight.getCurrentTextColor());
        binding.setErrorTextColor(ERROR_TEXT_COLOR);
        binding.setLifecycleOwner(this);

        var statisticsTable = binding.statisticsTable;
        statisticsTable.setLayoutManager(new GridLayoutManager(
                this, StatisticsAdapter.COLUMN_COUNT));
        statisticsTable.setAdapter(Contract.requireNonNull(adapter));
    }

    private void initializeObserver(@NonNull MainViewModel model,
                                    @NonNull StatisticsAdapter adapter) {
        if (BuildConfig.DEBUG) {
            Timber.d("Initialize observer");
        }
        model.getResult().observe(this, result -> {
            if (result.isSucceeded()) {
                adapter.submitList(result.getStatistics(), result.getWeight());
            } else {
                adapter.submitList(Collections.emptyList(), 0);
            }
        });
        model.getSortOrder().observe(this, v -> {
            if (BuildConfig.DEBUG) {
                Timber.d("Sort order changed to %s", sortOrderToString(v));
            }
            sortStatistics(adapter, v, model.getSortMethod().getValue());
        });
        model.getSortMethod().observe(this, v -> {
            if (BuildConfig.DEBUG) {
                Timber.d("Sort method changed to %s", sortMethodToString(v));
            }
            sortStatistics(adapter, model.getSortOrder().getValue(), v);
        });
    }
}