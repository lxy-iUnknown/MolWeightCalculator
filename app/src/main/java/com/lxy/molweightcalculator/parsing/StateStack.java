package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.util.Utility;

import java.util.Arrays;

public class StateStack {
    private static final int STATE_SIZE =
            8 + // Object
                    4 + // statistics
                    4 + // bracket
                    4 + // start
                    8;  // weight

    private State[] elements;
    private int top;

    public StateStack(int initialCapacity) {
        deepReset(initialCapacity);
    }

    private void deepReset(int initialCapacity) {
        Utility.checkInitialCapacity(initialCapacity);
        MemoryUsage.memoryAllocated(STATE_SIZE, initialCapacity);
        elements = new State[initialCapacity];
        clear();
    }

    public boolean isEmpty() {
        return size() < 0;
    }

    public int size() {
        return top;
    }

    public void clear() {
        top = -1;
    }

    public void push(int bracket, int start) {
        var s = size() + 1;
        if (s == elements.length) {
            var newSize = Utility.growSize(elements.length);
            MemoryUsage.memoryAllocated(STATE_SIZE, newSize);
            elements = Arrays.copyOf(elements, newSize);
        }
        var state = elements[s];
        if (state != null) {
            state.statistics.clear();
            state.start = start;
            state.bracket = bracket;
            state.weight = 0;
        } else {
            elements[s] = new State(bracket, start);
        }
        top = s;
    }

    private void checkStack() {
        if (isEmpty()) {
            Contract.fail("Stack is empty");
        }
    }

    private State peekUnchecked() {
        return elements[top];
    }

    public State peek() {
        checkStack();
        return peekUnchecked();
    }

    public State pop() {
        checkStack();
        return elements[top--];
    }

    public State peekNoThrow() {
        return isEmpty() ? null : peekUnchecked();
    }

    public void purgeMemory() {
        deepReset(Utility.INITIAL_CAPACITY);
    }

    public static class State {
        @NonNull
        public final StatisticsMap statistics = new StatisticsMap(Utility.INITIAL_CAPACITY);
        public int bracket;
        public int start;
        public double weight;

        public State(int bracket, int start) {
            this.bracket = bracket;
            this.start = start;
        }
    }
}
