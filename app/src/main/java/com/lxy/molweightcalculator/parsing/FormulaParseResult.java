package com.lxy.molweightcalculator.parsing;

import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.util.ULocale;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.R;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Value;
import com.lxy.molweightcalculator.ui.StatisticsItemList;
import com.lxy.molweightcalculator.util.GlobalContext;
import com.lxy.molweightcalculator.util.IStatistics;
import com.lxy.molweightcalculator.util.ParcelUtil;
import com.lxy.molweightcalculator.util.TraverseFunction;
import com.lxy.molweightcalculator.util.Utility;

import java.math.RoundingMode;
import java.text.FieldPosition;

import timber.log.Timber;

public class FormulaParseResult implements Parcelable {
    @NonNull
    public static final Creator<FormulaParseResult> CREATOR = new Creator<>() {
        @NonNull
        @Override
        public FormulaParseResult createFromParcel(Parcel in) {
            return new FormulaParseResult(in);
        }

        @NonNull
        @Override
        public FormulaParseResult[] newArray(int size) {
            return new FormulaParseResult[size];
        }
    };
    @NonNull
    public static final FormulaParseResult EMPTY_FORMULA =
            new FormulaParseResult(ParseErrorCode.EMPTY_FORMULA);
    @NonNull
    public static final FormulaParseResult ELEMENT_COUNT_OVERFLOW =
            new FormulaParseResult(ParseErrorCode.ELEMENT_COUNT_OVERFLOW);
    private static final double MIN_SCIENTIFIC_THRESHOLD = 1e10;
    @NonNull
    private static final String[] ERROR_MESSAGES = {
            "EMPTY_FORMULA",
            "NO_ELEMENT",
            "MISMATCHED_BRACKET(\"%s\")",
            "INVALID_TOKEN",
            "INVALID_ELEMENT",
            "ELEMENT_COUNT_TOO_LARGE",
            "ELEMENT_COUNT_OVERFLOW",
            "WEIGHT_OVERFLOW",
    };
    @NonNull
    private static final String[] BRACKET_STRINGS = {
            "(", ")", "[", "]", "{", "}"
    };
    @NonNull
    private static final StringBuffer STRING_BUFFER = new StringBuffer(10); // 1.7977e308
    @NonNull
    private static final DecimalFormat[] NORMAL_FORMATS;
    @NonNull
    private static final DecimalFormat[] EXPONENTIAL_FORMATS;
    @NonNull
    private static final String[] ERROR_STRINGS;
    @NonNull
    private static final FieldPosition FIELD_POSITION = new FieldPosition(0);
    private static final @ParseErrorCode int DEFAULT_ERROR_CODE = ParseErrorCode.EMPTY_FORMULA;

    static {
        final var PRECISION_COUNT = Utility.MAX_PRECISION + 1;

        var symbols = DecimalFormatSymbols.getInstance(ULocale.ROOT);
        symbols.setExponentSeparator("x10^");

        var normalFormats = new DecimalFormat[PRECISION_COUNT];
        var exponentialFormats = new DecimalFormat[PRECISION_COUNT];
        for (var precision = 0; precision < PRECISION_COUNT; precision++) {
            normalFormats[precision] = getDecimalFormat(precision, false, symbols);
            exponentialFormats[precision] = getDecimalFormat(precision, true, symbols);
        }
        NORMAL_FORMATS = normalFormats;
        EXPONENTIAL_FORMATS = exponentialFormats;
        ERROR_STRINGS = GlobalContext.get().getResources().getStringArray(R.array.error_stings);
    }

    private final long value;
    @ParseErrorCode
    private final int errorCode;
    @Nullable
    private final StatisticsItemList statistics;

    private FormulaParseResult(@NonNull Parcel in) {
        var succeeded = ParcelUtil.readBoolean(in);
        var value = in.readLong();
        if (succeeded) {
            this.statistics = ParcelUtil.readStatistics(in, Double.longBitsToDouble(value));
            this.errorCode = DEFAULT_ERROR_CODE;
        } else {
            this.statistics = null;
            this.errorCode = validateErrorCode(in.readInt());
        }
        this.value = value;
    }

    public FormulaParseResult(int start, int end, @ParseErrorCode int errorCode) {
        this.value = ((long) start << 32) | end;
        this.statistics = null;
        this.errorCode = errorCode;
    }

    public FormulaParseResult(double weight, @NonNull StatisticsItemList statistics) {
        this.value = Double.doubleToRawLongBits(weight);
        this.errorCode = DEFAULT_ERROR_CODE;
        this.statistics = Contract.requireNonNull(statistics);
    }

    private FormulaParseResult(@ParseErrorCode int errorCode) {
        this(-1, -1, errorCode);
    }

    @NonNull
    private static DecimalFormat getDecimalFormat(int precision,
                                                  boolean showExponential,
                                                  @NonNull DecimalFormatSymbols symbols) {
        var format = new DecimalFormat("", Contract.requireNonNull(symbols));
        if (showExponential) {
            format.setMinimumExponentDigits((byte) 1);
            format.setMinimumIntegerDigits(1);
            format.setMaximumIntegerDigits(1);
        }
        format.setMaximumFractionDigits(precision);
        format.setMinimumFractionDigits(precision);
        format.setGroupingUsed(false);
        format.setRoundingMode(RoundingMode.HALF_UP.ordinal());
        if (BuildConfig.DEBUG) {
            Timber.d("Precision: %d, Show exponential: %b, Decimal format: %s",
                    precision, showExponential, format.toPattern());
        }
        return format;
    }

    private static @ParseErrorCode int validateErrorCode(@ParseErrorCode int errorCode) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("errorCode", errorCode),
                    ParseErrorCode.MINIMUM, ParseErrorCode.MAXIMUM);
        }
        return errorCode;
    }

    public static boolean hasStartEnd(@ParseErrorCode int errorCode) {
        final var NO_START_END = (1 << ParseErrorCode.EMPTY_FORMULA) |
                (1 << ParseErrorCode.ELEMENT_COUNT_OVERFLOW) |
                (1 << ParseErrorCode.WEIGHT_OVERFLOW);

        validateErrorCode(errorCode);
        return (NO_START_END & (1 << errorCode)) == 0;
    }

    public static boolean isInvalidBracket(@ParseErrorCode int errorCode) {
        validateErrorCode(errorCode);
        return errorCode == ParseErrorCode.MISMATCHED_BRACKET;
    }

    public static int extractStart(long startEnd) {
        return (int) (startEnd >>> 32);
    }

    public static int extractEnd(long startEnd) {
        return (int) (startEnd);
    }

    private void requireSucceeded() {
        if (BuildConfig.DEBUG) {
            Contract.require(isSucceeded(), "Not succeeded");
        }
    }

    private void requireFailed() {
        if (BuildConfig.DEBUG) {
            Contract.require(!isSucceeded(), "Not failed");
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        var succeeded = isSucceeded();
        ParcelUtil.writeBoolean(dest, succeeded);
        dest.writeLong(value);
        if (succeeded) {
            ParcelUtil.writeStatistics(dest, getStatistics());
        } else {
            dest.writeInt(errorCode);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public StatisticsItemList getStatistics() {
        requireSucceeded();
        return Contract.requireNonNull(statistics);
    }

    public double getWeight() {
        requireSucceeded();
        return Double.longBitsToDouble(value);
    }

    @ParseErrorCode
    public int getErrorCode() {
        return errorCode;
    }

    @NonNull
    public String getWeightString(int precision) {
        if (isSucceeded()) {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(new Value<>("precision", precision),
                        0, Utility.MAX_PRECISION);
            }
            var weight = getWeight();
            var formats = weight < MIN_SCIENTIFIC_THRESHOLD ? NORMAL_FORMATS : EXPONENTIAL_FORMATS;
            var sb = STRING_BUFFER;
            sb.setLength(0);
            return formats[precision].format(weight, sb, FIELD_POSITION).toString();
        } else {
            var errorString = ERROR_STRINGS[errorCode - ParseErrorCode.MINIMUM];
            if (isInvalidBracket(errorCode)) {
                errorString = String.format(errorString, extractEnd(getStartEnd()));
            }
            return errorString;
        }
    }

    public boolean isSucceeded() {
        return statistics != null;
    }

    public long getStartEnd() {
        requireFailed();
        return value;
    }

    @SuppressWarnings("DataFlowIssue")
    @NonNull
    public String debugToString() {
        var sb = new StringBuilder()
                .append("MolWeightParseResult{");
        if (isSucceeded()) {
            sb = sb.append("weight=")
                    .append(getWeight())
                    .append(", statistics=");
            IStatistics.appendStatistics(sb, new IStatistics() {
                @Override
                public int size() {
                    return statistics.size();
                }

                @Override
                public void forEach(@NonNull TraverseFunction function) {
                    for (var item : statistics.getItems()) {
                        function.visit(item.getElementId(), item.getCount());
                    }
                }
            });
        } else {
            var startEnd = getStartEnd();
            var start = extractStart(startEnd);
            var end = extractEnd(startEnd);
            var isInvalidBracket = isInvalidBracket(errorCode);
            var errorString = ERROR_MESSAGES[errorCode - ParseErrorCode.MINIMUM];
            if (isInvalidBracket) {
                errorString = String.format(errorString, BRACKET_STRINGS[end]);
            }
            sb.append(errorString)
                    .append(", start=")
                    .append(start)
                    .append(", end=")
                    .append(isInvalidBracket ? start + 1 : end);
        }
        return sb.append("}").toString();
    }
}
