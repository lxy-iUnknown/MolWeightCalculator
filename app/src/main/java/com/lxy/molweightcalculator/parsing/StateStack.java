package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.util.Utility;

import java.util.Arrays;
import java.util.EmptyStackException;

public class StateStack {
    private static final int STATE_SIZE =
            8 + // Object
                    4 + // statistics
                    4 + // bracket
                    4 + // start
                    8;  // weight
    @NonNull
    private static final State[] EMPTY_ELEMENTS = new State[0];
    @NonNull
    private State[] elements;
    private int top;

    public StateStack(int initialCapacity) {
        MemoryUsage.allocate(STATE_SIZE, initialCapacity);
        elements = new State[initialCapacity];
        top = -1;
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
            MemoryUsage.allocate(STATE_SIZE, newSize);
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
            throw new EmptyStackException();
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
        clear();
        // Let the garbage collector clean up memory
        elements = EMPTY_ELEMENTS;
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

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean updateStatistics(char elementId, long count) {
            return statistics.addValueOrPut(elementId, count);
        }
    }
}
