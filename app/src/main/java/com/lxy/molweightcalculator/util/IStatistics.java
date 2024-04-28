package com.lxy.molweightcalculator.util;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.parsing.Element;

public interface IStatistics {
    static void appendStatistics(@NonNull StringBuilder sb,
                                 @NonNull IStatistics statistics) {
        Contract.requireNonNull(sb);
        Contract.requireNonNull(statistics);
        var size = statistics.size();
        sb.append('{');
        statistics.forEach(new TraverseFunction() {
            private int count = size;

            @Override
            public void visit(char key, long value) {
                sb.append(Element.getElementNameFromId(key));
                sb.append('=');
                sb.append(value);
                if (--count > 0) {
                    sb.append(", ");
                }
            }
        });
        sb.append('}');
    }

    int size();

    void forEach(@NonNull TraverseFunction function);
}
