package com.lxy.molweightcalculator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

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
import androidx.recyclerview.widget.RecyclerView;

import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.databinding.ActivityMainBinding;
import com.lxy.molweightcalculator.ui.CustomTextWatcher;
import com.lxy.molweightcalculator.ui.MainViewModel;
import com.lxy.molweightcalculator.ui.StatisticsAdapter;
import com.lxy.molweightcalculator.ui.StatisticsItem;
import com.lxy.molweightcalculator.ui.StatisticsItemList;
import com.lxy.molweightcalculator.util.GlobalContext;
import com.lxy.molweightcalculator.util.Utility;

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
    private static final DialogInterface.OnClickListener STATISTICS_DIALOG_CONFIRM_LISTENER =
            (dialog, which) -> dialog.dismiss();
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
    @NonNull
    private static final String STATISTICS_DIALOG_TITLE;
    @NonNull
    private static final String STRING_CONFIRM;
    private static final int ERROR_TEXT_COLOR;

    static {
        var context = GlobalContext.get();
        var resources = context.getResources();
        STATISTICS_DIALOG_TITLE = resources.getString(R.string.statistics_dialog_title);
        STRING_CONFIRM = resources.getString(R.string.confirm);
        ERROR_TEXT_COLOR = context.getColor(R.color.error_text_color);
    }

    private ActivityMainBinding binding;
    private MainViewModel model;

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

    private static int ratioCompareKey(@NonNull StatisticsItemList list,
                                       @NonNull StatisticsItem o1,
                                       @NonNull StatisticsItem o2) {
        Contract.requireNonNull(list);
        var key1 = list.getMassRatio(o1);
        var key2 = list.getMassRatio(o2);
        if (key1 == key2) {
            return Integer.compare(o1.getElementId(), o2.getElementId());
        }
        return key1 > key2 ? 1 : -1;
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
        initializeBinding(binding, model, adapter, initializeDialog(adapter));
        this.binding = binding;
        this.model = model;
        model.getResult().observe(this, result -> {
            if (result.isSucceeded()) {
                adapter.submitList(result.getStatistics());
            }
        });
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

    @NonNull
    private AlertDialog initializeDialog(@NonNull StatisticsAdapter adapter) {
        if (BuildConfig.DEBUG) {
            Timber.d("Initialize dialog");
        }
        var builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(STATISTICS_DIALOG_TITLE)
                .setPositiveButton(STRING_CONFIRM, STATISTICS_DIALOG_CONFIRM_LISTENER);
        var view = (RecyclerView) LayoutInflater.from(builder.getContext())
                .inflate(R.layout.statistics_table, null);
        view.setLayoutManager(new GridLayoutManager(
                this, StatisticsAdapter.COLUMN_COUNT));
        view.setAdapter(Contract.requireNonNull(adapter));
        return builder.setView(view).create();
    }

    private Comparator<StatisticsItem> getComparator(@NonNull StatisticsItemList list) {
        var sortOrderValue = model.getSortOrder().getValue();
        if (sortOrderValue == null) {
            return null;
        }
        var sortMethodValue = model.getSortMethod().getValue();
        if (sortMethodValue == null) {
            return null;
        }
        var sortOrder = (int) sortOrderValue;
        var sortMethod = (int) sortMethodValue;
        return switch (sortOrder) {
            case SORT_ORDER_UNDEFINED -> null;
            case SORT_ORDER_ASCENDING -> switch (sortMethod) {
                case SORT_METHOD_UNDEFINED -> null;
                case SORT_METHOD_DEFAULT -> ELEMENT_ID_ASCENDING;
                case SORT_METHOD_ORDINAL -> ORDINAL_ASCENDING;
                case SORT_METHOD_ELEMENT_COUNT -> ELEMENT_COUNT_ASCENDING;
                case SORT_METHOD_MASS_RATIO -> (o1, o2) -> ratioCompareKey(list, o1, o2);
                default -> invalidSortMethod(sortMethod);
            };
            case SORT_ORDER_DESCENDING -> switch (sortMethod) {
                case SORT_METHOD_UNDEFINED -> null;
                case SORT_METHOD_DEFAULT -> ELEMENT_ID_DESCENDING;
                case SORT_METHOD_ORDINAL -> ORDINAL_DESCENDING;
                case SORT_METHOD_ELEMENT_COUNT -> ELEMENT_COUNT_DESCENDING;
                case SORT_METHOD_MASS_RATIO -> (o1, o2) -> ratioCompareKey(list, o2, o1);
                default -> invalidSortMethod(sortMethod);
            };
            default -> invalidSortOrder(sortOrder);
        };
    }

    private void initializeBinding(@NonNull ActivityMainBinding binding,
                                   @NonNull MainViewModel model,
                                   @NonNull StatisticsAdapter adapter,
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

        var textColor = binding.textMolecularWeight.getCurrentTextColor();
        binding.setModel(model);
        binding.setTextColor(textColor);
        binding.setErrorTextColor(ERROR_TEXT_COLOR);
        binding.showStatisticsDialog.setOnClickListener(v -> {
            var oldList = adapter.getList();
            var comparator = getComparator(oldList);
            if (comparator != null /* null indicates not selected */) {
                var list = new StatisticsItemList(oldList);
                list.sort(comparator);
                adapter.submitList(list);
            }
            dialog.show();
        });
        binding.setLifecycleOwner(this);
    }
}