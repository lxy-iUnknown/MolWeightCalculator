package com.lxy.molweightcalculator.parsing;

import android.annotation.SuppressLint;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.util.ULocale;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.GlobalContext;
import com.lxy.molweightcalculator.util.Contract;
import com.lxy.molweightcalculator.R;
import com.lxy.molweightcalculator.util.Utility;

import java.math.RoundingMode;
import java.text.FieldPosition;

import timber.log.Timber;

public class MolWeightParseResult implements Parcelable {
    private static final int MIN_SCIENTIFIC_DIGITS = 7;
    private static final float MIN_SCIENTIFIC_THRESHOLD = 1e7f; // (float)Math.pow(10, MIN_SCIENTIFIC_DIGITS)

    @NonNull
    private static final String STRING_EMPTY_FORMULA = GlobalContext.getResourceString(R.string.empty_formula);

    @NonNull
    private static final String STRING_INVALID_FORMULA = GlobalContext.getResourceString(R.string.invalid_formula);

    @NonNull
    private static final String STRING_ELEMENT_COUNT_OVERFLOW = GlobalContext.getResourceString(R.string.element_count_overflow);

    @NonNull
    private static final String STRING_WEIGHT_OVERFLOW = GlobalContext.getResourceString(R.string.weight_overflow);

    @NonNull
    private static final DecimalFormat[] NORMAL_FORMATS;
    @NonNull
    private static final DecimalFormat[] EXPONENTIAL_FORMATS;
    @NonNull
    private static final StringBuffer STRING_BUFFER = new StringBuffer(9); // 3.4028E38
    @NonNull
    private static final FieldPosition DUMMY = new FieldPosition(0);

    @NonNull
    public static final Creator<MolWeightParseResult> CREATOR = new Creator<MolWeightParseResult>() {
        @NonNull
        @Override
        public MolWeightParseResult createFromParcel(Parcel in) {
            return new MolWeightParseResult(in);
        }

        @NonNull
        @Override
        public MolWeightParseResult[] newArray(int size) {
            return new MolWeightParseResult[size];
        }
    };

    @NonNull
    public static final MolWeightParseResult ELEMENT_COUNT_OVERFLOW =
            new MolWeightParseResult(ParseError.ELEMENT_COUNT_OVERFLOW);

    @NonNull
    public static final MolWeightParseResult WEIGHT_OVERFLOW =
            new MolWeightParseResult(ParseError.WEIGHT_OVERFLOW);

    @NonNull
    public static final MolWeightParseResult EMPTY_FORMULA =
            new MolWeightParseResult(ParseError.EMPTY_FORMULA);
    @NonNull
    public static final MolWeightParseResult INVALID_FORMULA =
            new MolWeightParseResult(ParseError.INVALID_FORMULA);

    static {
        final int PRECISION_COUNT = Utility.MAX_PRECISION + 1;

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(ULocale.ROOT);
        symbols.setExponentSeparator("x10^");

        DecimalFormat[] normalFormats = new DecimalFormat[PRECISION_COUNT];
        DecimalFormat[] exponentialFormats = new DecimalFormat[PRECISION_COUNT];
        for (int precision = 0; precision < PRECISION_COUNT; precision++) {
            normalFormats[precision] = getDecimalFormat(precision, false, symbols);
            exponentialFormats[precision] = getDecimalFormat(precision, true, symbols);
        }
        NORMAL_FORMATS = normalFormats;
        EXPONENTIAL_FORMATS = exponentialFormats;
    }

    /**
     * If value >= 0 (succeeded), this value stores bit representation of molecular weight(floating point).<br/>
     * If value < 0 (failed), this value stores a {@link ParseError} value with sign bit set
     */
    private final int value;
    /**
     * If succeeded, this value stores statistics with type {@link SparseIntArray}(non-null).<br/>
     * If failed, this value stores error message with type {@link String}(nullable)
     */
    private final @Nullable Object objectValue;

    protected MolWeightParseResult(@NonNull Parcel in) {
        int value = in.readInt();
        Object objectValue;
        if (value >= 0) {
            objectValue = Contract.requireNonNull(readSparseIntArray(in));
            validateBits(value);
        } else {
            objectValue = validateFormatArgument(value & Integer.MAX_VALUE, in.readString());
        }
        this.value = value;
        this.objectValue = objectValue;
    }

    private MolWeightParseResult(int value, @Nullable Object objectValue) {
        this.value = value;
        this.objectValue = objectValue;
    }

    public MolWeightParseResult(float weight, @NonNull SparseIntArray statistics) {
        this(validateWeight(weight), (Object) Contract.requireNonNull(statistics));
    }

    public MolWeightParseResult(@ParseError int errorCode) {
        this(errorCode, (String)null);
    }

    @SuppressLint("WrongConstant")
    public MolWeightParseResult(@ParseError int errorCode, @Nullable String formatArgument) {
        this(validateErrorCode(errorCode) | Integer.MIN_VALUE,
                (Object) validateFormatArgument(errorCode, formatArgument));
    }

    private static int validateWeight(float weight) {
        int bits = Float.floatToRawIntBits(weight);
        validateBits(bits);
        return bits;
    }

    private static void validateBits(int bits) {
        if (BuildConfig.DEBUG) {
            // 0x7f800000: +Infinity
            // 0x7f800001 - 0x7fffffff or 0xff800001 - 0xffffffff: NaN
            Contract.require(bits >= 0 && bits <= 0x7f800000,
                    "Bits must be must non-negative and finite");
        }
    }

    private static @ParseError int validateErrorCode(@ParseError int errorCode) {
        final @ParseError int MINIMUM = ParseError.EMPTY_FORMULA;
        final @ParseError int MAXIMUM = ParseError.WEIGHT_OVERFLOW;

        if (BuildConfig.DEBUG) {
            Contract.require(errorCode >= MINIMUM && errorCode <= MAXIMUM, "Invalid error code");
        }
        return errorCode;
    }

    @Nullable
    private static String validateFormatArgument(int errorCode, @Nullable String formatArgument) {
        validateErrorCode(errorCode);
        if (BuildConfig.DEBUG && errorCode == ParseError.INVALID_ELEMENT) {
            Contract.require(formatArgument != null, "Format argument is null");
        }
        return formatArgument;
    }

    @NonNull
    private static StringBuffer clearAndGetStringBuffer() {
        StringBuffer sb = STRING_BUFFER;
        sb.setLength(0);
        return sb;
    }

    @NonNull
    private static DecimalFormat getDecimalFormat(int precision,
                                                  boolean showExponential,
                                                  @NonNull DecimalFormatSymbols symbols) {
        DecimalFormat format = new DecimalFormat("", Contract.requireNonNull(symbols));
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

    @Nullable
    private SparseIntArray readSparseIntArray(@NonNull Parcel src) {
        int size = src.readInt();
        if (size < 0) {
            return null;
        }
        SparseIntArray array = new SparseIntArray(size);
        for (int i = 0; i < size; i++) {
            array.append(src.readInt(), src.readInt());
        }
        return array;
    }

    private void writeSparseIntArray(@NonNull Parcel dest, @NonNull SparseIntArray array) {
        int size = array.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            dest.writeInt(array.keyAt(i));
            dest.writeInt(array.valueAt(i));
        }
    }

    private void requireSucceeded() {
        if (BuildConfig.DEBUG) {
            Contract.require(isSucceeded(), "An error occurred");
        }
    }

    @SuppressLint("WrongConstant")
    private @ParseError int getErrorCode() {
        return value & Integer.MAX_VALUE;
    }

    @NonNull
    private String formatErrorMessage(@StringRes int id) {
        return GlobalContext.get().getString(id, Contract.requireNonNull(objectValue));
    }

    @NonNull
    private String getWeightStringUnchecked(int precision) {
        float weight = getWeight();
        DecimalFormat[] formats;
        if (weight < MIN_SCIENTIFIC_THRESHOLD) {
            formats = NORMAL_FORMATS;
        } else {
            formats = EXPONENTIAL_FORMATS;
        }
        return formats[precision].format(weight, clearAndGetStringBuffer(), DUMMY).toString();
    }

    @NonNull
    private String getErrorMessage() {
        switch (validateErrorCode(getErrorCode())) {
            case ParseError.EMPTY_FORMULA:
                return STRING_EMPTY_FORMULA;
            case ParseError.INVALID_ELEMENT:
                return formatErrorMessage(R.string.invalid_element);
            case ParseError.INVALID_FORMULA:
                return STRING_INVALID_FORMULA;
            case ParseError.ELEMENT_COUNT_OVERFLOW:
                return STRING_ELEMENT_COUNT_OVERFLOW;
            case ParseError.WEIGHT_OVERFLOW:
                return STRING_WEIGHT_OVERFLOW;
            default:
                return "";
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(value);
        if (value >= 0) {
            assert objectValue != null;
            writeSparseIntArray(dest, (SparseIntArray) objectValue);
        } else {
            dest.writeString((String) objectValue);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public float getWeight() {
        requireSucceeded();
        return Float.intBitsToFloat(value);
    }

    public SparseIntArray getStatistics() {
        requireSucceeded();
        return (SparseIntArray) objectValue;
    }

    @NonNull
    public String getWeightString(int precision) {
        if (isSucceeded()) {
            if (BuildConfig.DEBUG) {
                Contract.require(precision >= 0 && precision <= Utility.MAX_PRECISION,
                        "Invalid precision");
            }
            return getWeightStringUnchecked(precision);
        } else {
            return getErrorMessage();
        }
    }

    public boolean isSucceeded() {
        return this.value >= 0;
    }

    @NonNull
    private StringBuilder appendStatistics(@NonNull StringBuilder sb) {
        Contract.requireNonNull(sb);
        SparseIntArray statistics = getStatistics();
        int max = statistics.size() - 1;
        if (max == -1) {
            return sb;
        }
        sb.append('[');
        for (int i = 0; ; i++) {
            int id = statistics.keyAt(i);
            int count = statistics.valueAt(i);
            sb.append("StatisticsItem{count=")
                    .append(count)
                    .append(", id=")
                    .append(id)
                    .append('(')
                    .append(ElementData.getElementName((char) id))
                    .append(")}");
            if (i == max) {
                return sb.append(']');
            }
            sb.append(", ");
        }
    }

    @NonNull
    public String debugToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MolWeightParseResult{");
        if (isSucceeded()) {
            appendStatistics(sb.append("weight=")
                    .append(getWeight())
                    .append(", statistics="))
                    .append("}");
        } else {
            sb.append("{errorMessage='")
                    .append(getErrorMessage())
                    .append("'}");
        }
        return sb.toString();
    }
}
