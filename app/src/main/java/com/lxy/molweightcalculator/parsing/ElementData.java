package com.lxy.molweightcalculator.parsing;

import android.util.JsonReader;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.GlobalContext;
import com.lxy.molweightcalculator.R;
import com.lxy.molweightcalculator.util.Contract;
import com.lxy.molweightcalculator.util.TimberUtil;
import com.lxy.molweightcalculator.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ElementData {
    private static class MutableInt {
        private int value;

        public MutableInt(int value) {
            this.value = value;
        }

        public int getValueAndIncrement() {
            return value++;
        }
    }
    @NonNull
    private static final String[] ELEMENT_NAMES = populateElementNames();
    @NonNull
    private static final SparseArray<ElementInfo> ELEMENT_DATA = loadElementData();
    private static final int BASE = 26;
    private static final int ELEMENT_MAX = BASE + BASE * BASE - 1;
    @NonNull
    private static String[] populateElementNames() {
        String[] elementNames = new String[ELEMENT_MAX + 1];
        char[] chars = new char[2];
        for (int i = 0; i < BASE; i++) {
            char ch1 = (char) ('A' + i);
            elementNames[i] = Character.toString(ch1);
            for (int j = 0; j < BASE; j++) {
                char ch2 = (char) ('a' + j);
                chars[0] = ch1;
                chars[1] = ch2;
                elementNames[i * BASE + j + BASE] = new String(chars, 0, 2);
            }
        }
        return elementNames;
    }

    private static <T> T handleIOException(@NonNull IOException e) {
        return TimberUtil.errorAndThrowException(Contract.requireNonNull(e),
                "Load element data failed");
    }

    @NonNull
    private static SparseArray<ElementInfo> loadElementData() {
        final int INITIAL_CAPACITY = 100;

        HashMap<Integer, ElementInfo> map = new HashMap<>(INITIAL_CAPACITY);
        try (InputStream is = GlobalContext.getResource().openRawResource(R.raw.element_data)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                JsonReader reader = new JsonReader(inputStreamReader);
                reader.beginObject();
                MutableInt ordinal = new MutableInt(1);
                while (reader.hasNext()) {
                    int base26Value = parse(reader.nextName());
                    map.compute(base26Value, (key, value) -> {
                        if (value != null) {
                            return TimberUtil.errorAndThrowException("Duplicate key found");
                        } else {
                            try {
                                double weight = reader.nextDouble();
                                if (weight < 0 || weight > Float.MAX_VALUE) {
                                    return TimberUtil.errorAndThrowException("Invalid weight");
                                }
                                int weightFixed = Utility.fixedFromFloat((float) weight);
                                int temp = ordinal.getValueAndIncrement();
                                if (temp == Integer.MAX_VALUE) {
                                    return TimberUtil.errorAndThrowException("Cannot add more entries");
                                }
                                return ElementInfo.of(temp, weightFixed);
                            } catch (IOException e) {
                                return handleIOException(e);
                            }
                        }
                    });
                }
            }
        } catch (NumberFormatException e) {
            return TimberUtil.errorAndThrowException(e, "Parse failed");
        } catch (IllegalStateException e) {
            return TimberUtil.errorAndThrowException(e, "Invalid json token");
        } catch (IOException e) {
            return handleIOException(e);
        }
        SparseArray<ElementInfo> realMap = new SparseArray<>(map.size());
        map.forEach(realMap::put);
        return realMap;
    }

    private static char getBase26Value(char firstChar) {
        Contract.requireLatin1UpperCaseLetter(firstChar);
        return (char) (firstChar - 'A');
    }

    private static char getBase26Value(char firstChar, char secondChar) {
        Contract.requireLatin1UpperCaseLetter(firstChar);
        Contract.requireLatin1LowerCaseLetter(secondChar);
        // (firstChar - 'A' + 1) * 26 + (secondChar - 'a')
        // = (firstChar - ('A' - 1)) * 26 + (secondChar - 'a')
        // = firstChar * 26 - ('A' - 1) * 26 + secondChar - 'a'
        // = firstChar * 26 + secondChar - (('A' - 1) * 26 + 'a')
        return (char) (firstChar * 26 + secondChar - (('A' - 1) * 26 + 'a'));
    }

    public static void validateElement(int element) {
        if (BuildConfig.DEBUG) {
            Contract.require(element >= 0 && element < ELEMENT_MAX, "Invalid element range");
        }
    }

    @NonNull
    public static ElementInfo getValidElementInfo(char element) {
        ElementInfo info = getElementInfo(element);
        if (BuildConfig.DEBUG) {
            Contract.require(info.isValid(), "Invalid element");
        }
        return info;
    }

    public static char parse(@NonNull CharSequence value) {
        return parse(value, 0, value.length());
    }

    @NonNull
    public static String getElementName(char element) {
        validateElement(element);
        return ELEMENT_NAMES[element];
    }

    public static int getOrdinal(char element) {
        validateElement(element);
        return getValidElementInfo(element).getOrdinal();
    }

    public static float getMolecularWeight(char element) {
        validateElement(element);
        return getValidElementInfo(element).getMolecularWeight();
    }

   public static char parse(@NonNull CharSequence value, int startIndex, int endIndex) {
        Contract.validateStartEndIndex(value, startIndex, endIndex);
        int length = endIndex - startIndex;
        if (BuildConfig.DEBUG) {
            Contract.require(length >= 1 && length <= 2, "Invalid element length");
        }
        char firstChar = value.charAt(startIndex);
        if (length == 1) {
            return getBase26Value(firstChar);
        } else {
            return getBase26Value(firstChar, value.charAt(startIndex + 1));
        }
    }

    public static ElementInfo getElementInfo(char element) {
        return ELEMENT_DATA.get(element, ElementInfo.INVALID);
    }

    @NonNull
    public static String debugToString(char element) {
        StringBuilder sb = new StringBuilder();
        sb.append("Element{name=").append(getElementName(element));
        ElementInfo info = getElementInfo(element);
        sb.append(", molecularWeight=");
        if (info.isValid()) {
            sb.append(info.getMolecularWeight());
        } else {
            sb.append("(invalid)");
        }
        return sb.append('}').toString();
    }
}
