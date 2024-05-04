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
import com.lxy.molweightcalculator.ui.StatisticsItem;
import com.lxy.molweightcalculator.util.GlobalContext;
import com.lxy.molweightcalculator.util.IStatistics;
import com.lxy.molweightcalculator.util.ParcelUtil;
import com.lxy.molweightcalculator.util.TraverseFunction;
import com.lxy.molweightcalculator.util.Utility;

import java.math.RoundingMode;
import java.text.FieldPosition;
import java.util.List;

import timber.log.Timber;

public class ParseResult implements Parcelable {
    @NonNull
    public static final ParseResult EMPTY_FORMULA =
            new ParseResult(ErrorCode.EMPTY_FORMULA);
    @NonNull
    public static final ParseResult ELEMENT_COUNT_OVERFLOW =
            new ParseResult(ErrorCode.ELEMENT_COUNT_OVERFLOW);
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
    private static final StringBuffer STRING_BUFFER = new StringBuffer(10); // 1.7977e308
    @NonNull
    private static final DecimalFormat[] NORMAL_FORMATS;
    @NonNull
    private static final DecimalFormat[] EXPONENTIAL_FORMATS;
    @NonNull
    private static final String[] ERROR_STRINGS;
    @NonNull
    private static final FieldPosition FIELD_POSITION = new FieldPosition(0);
    private static final @ErrorCode int DUMMY_ERROR_CODE = ErrorCode.EMPTY_FORMULA;
    @NonNull
    public static final Creator<ParseResult> CREATOR = new Creator<>() {
        @NonNull
        @Override
        public ParseResult createFromParcel(Parcel source) {
            return ParseResult.readParseResult(source);
        }

        @NonNull
        @Override
        public ParseResult[] newArray(int size) {
            return new ParseResult[size];
        }
    };

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

    @Nullable
    private final List<StatisticsItem> statistics;
    private final long value;
    @ErrorCode
    private final int errorCode;

    private ParseResult(@ErrorCode int errorCode) {
        this(null, -1, errorCode);
    }

    public ParseResult(int start, int end, @ErrorCode int errorCode) {
        this(null, ((long) end << 32) | start, errorCode);
    }

    public ParseResult(@NonNull List<StatisticsItem> statistics, double weight) {
        this(Contract.requireNonNull(statistics),
                Double.doubleToRawLongBits(weight), DUMMY_ERROR_CODE);
    }

    private ParseResult(@Nullable List<StatisticsItem> statistics, long value, int errorCode) {
        this.statistics = statistics;
        this.value = value;
        this.errorCode = errorCode;
    }

    @NonNull
    private static ParseResult readParseResult(@NonNull Parcel source) {
        Contract.requireNonNull(source);
        var value = source.readLong();
        List<StatisticsItem> statistics;
        int errorCode;
        if (ParcelUtil.readBoolean(source)) {
            statistics = ParcelUtil.readStatistics(source);
            errorCode = DUMMY_ERROR_CODE;
        } else {
            statistics = null;
            errorCode = validateErrorCode(source.readInt());
        }
        return new ParseResult(statistics, value, errorCode);
    }

    private static void writeParseResult(@NonNull Parcel dest, @NonNull ParseResult result) {
        Contract.requireNonNull(dest);
        var succeeded = result.isSucceeded();
        ParcelUtil.writeBoolean(dest, succeeded);
        dest.writeLong(result.value);
        if (succeeded) {
            ParcelUtil.writeStatistics(dest, result.getStatistics());
        } else {
            dest.writeInt(result.errorCode);
        }
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

    private static @ErrorCode int validateErrorCode(@ErrorCode int errorCode) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("errorCode", errorCode),
                    ErrorCode.MINIMUM, ErrorCode.MAXIMUM);
        }
        return errorCode;
    }

    public static boolean hasStartEnd(@ErrorCode int errorCode) {
        final var NO_START_END = (1 << ErrorCode.EMPTY_FORMULA) |
                (1 << ErrorCode.ELEMENT_COUNT_OVERFLOW) |
                (1 << ErrorCode.WEIGHT_OVERFLOW);

        validateErrorCode(errorCode);
        return (NO_START_END & (1 << errorCode)) == 0;
    }

    public static boolean isInvalidBracket(@ErrorCode int errorCode) {
        validateErrorCode(errorCode);
        return errorCode == ErrorCode.MISMATCHED_BRACKET;
    }

    private void requireSucceeded() {
        if (BuildConfig.DEBUG) {
            Contract.require(isSucceeded(), "Not succeeded");
        }
    }

    private void requireFailed() {
        if (BuildConfig.DEBUG) {
            Contract.require(!isSucceeded(), "Succeeded");
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        writeParseResult(dest, this);
    }

    @Override
    public int describeContents() {
        return 0;
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
            var errorString = ERROR_STRINGS[errorCode - ErrorCode.MINIMUM];
            if (isInvalidBracket(errorCode)) {
                errorString = String.format(errorString, ParseState.getBracketString(getEnd()));
            }
            return errorString;
        }
    }

    public boolean isSucceeded() {
        return statistics != null;
    }

    @NonNull
    public List<StatisticsItem> getStatistics() {
        requireSucceeded();
        return Contract.requireNonNull(statistics);
    }

    public double getWeight() {
        requireSucceeded();
        return Double.longBitsToDouble(value);
    }

    @ErrorCode
    public int getErrorCode() {
        requireFailed();
        return errorCode;
    }

    public int getStart() {
        requireFailed();
        return (int) value;
    }

    public int getEnd() {
        requireFailed();
        return (int) (value >>> 32);
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
                    for (var item : statistics) {
                        function.visit(item.getElementId(), item.getCount());
                    }
                }
            });
        } else {
            var isInvalidBracket = isInvalidBracket(errorCode);
            var errorString = ERROR_MESSAGES[errorCode - ErrorCode.MINIMUM];
            if (isInvalidBracket) {
                errorString = String.format(errorString, ParseState.getBracketString(getEnd()));
            }
            sb.append(errorString)
                    .append(", start=")
                    .append(getStart())
                    .append(", end=")
                    .append(isInvalidBracket ? getStart() + 1 : getEnd());
        }
        return sb.append("}").toString();
    }
}
