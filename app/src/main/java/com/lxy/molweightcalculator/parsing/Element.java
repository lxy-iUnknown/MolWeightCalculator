package com.lxy.molweightcalculator.parsing;

import android.util.JsonReader;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.BuildConfig;
import com.lxy.molweightcalculator.R;
import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.contract.Value;
import com.lxy.molweightcalculator.util.GlobalContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Element {
    private static final int BASE = 26;
    private static final int ELEMENT_MAX = BASE + BASE * BASE - 1;
    private static final int FIXED_PERCENTAGE_MULTIPLIER = 1_0_0_0_0 * 100;

    @NonNull
    private static final int[] ELEMENT_ORDINALS;
    @NonNull
    private static final double[] ELEMENT_WEIGHTS;

    static {
        final var ARRAY_SIZE = ELEMENT_MAX + 1;

        var elementOrdinals = new int[ARRAY_SIZE];
        var elementWeights = new double[ARRAY_SIZE];
        try {
            var inputStream = GlobalContext.get().getResources().openRawResource(R.raw.element_data);
            // JsonReader.close will also close the underlying Reader
            // InputStreamReader.close will also close the underlying InputStream
            var ordinal = 1;
            try (var jsonReader = new JsonReader(new InputStreamReader(inputStream))) {
                Arrays.fill(elementOrdinals, 0, ARRAY_SIZE, -1);
                Arrays.fill(elementWeights, 0, ARRAY_SIZE, Double.NaN);
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    var elementName = jsonReader.nextName();
                    var elementNameLength = elementName.length();
                    var elementId = switch (elementNameLength) {
                        case 1 -> parseElementId(elementName.charAt(0));
                        case 2 -> parseElementId(elementName.charAt(0), elementName.charAt(1));
                        default -> (char) Contract.fail(
                                "Invalid element name length", elementNameLength);
                    };
                    var weight = jsonReader.nextDouble();
                    if (weight <= 0) {
                        Contract.fail("Non-positive element weight ", weight);
                    }
                    if (!Double.isFinite(weight)) {
                        Contract.fail("Non-finite element weight ", weight);
                    }
                    elementWeights[elementId] = weight;
                    elementOrdinals[elementId] = ordinal++;
                }
            }
        } catch (IOException e) {
            Contract.fail("Open element_data.json failed", e);
        }
        ELEMENT_ORDINALS = elementOrdinals;
        ELEMENT_WEIGHTS = elementWeights;
    }

    private Element() {
    }

    private static char validateElementId(char elementId) {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(new Value<>("elementId", (int) elementId),
                    0, ELEMENT_MAX);
        }
        return elementId;
    }

    public static char parseElementId(char firstChar) {
        return (char) (firstChar - 'A');
    }

    public static char parseElementId(char firstChar, char secondChar) {
        return (char) (firstChar * 26 + secondChar - (('A' - 1) * 26 + 'a'));
    }

    @NonNull
    public static String getElementNameFromId(char elementId) {
        validateElementId(elementId);
        if (elementId < BASE) {
            return Character.toString((char) ('A' + elementId));
        } else {
            var buffer = new char[2];
            var div = elementId / BASE;
            buffer[0] = (char) (('A' - 1) + div);
            buffer[1] = (char) ('a' + elementId - div * BASE);
            return new String(buffer, 0, 2);
        }
    }

    public static int getOrdinalFromId(char elementId) {
        return ELEMENT_ORDINALS[validateElementId(elementId)];
    }

    public static double getWeightFromId(char elementId) {
        return ELEMENT_WEIGHTS[validateElementId(elementId)];
    }

    public static double getScaledWeightFromId(char elementId) {
        return getWeightFromId(elementId) * FIXED_PERCENTAGE_MULTIPLIER;
    }
}
