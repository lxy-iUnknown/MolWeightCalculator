package com.lxy.molweightcalculator.parsing;

import androidx.annotation.NonNull;

import com.lxy.molweightcalculator.contract.Contract;
import com.lxy.molweightcalculator.util.Utility;

import java.util.Arrays;

public class ParseStack {
    @NonNull
    private ParseState[] elements;
    private int top;

    public ParseStack() {
        elements = deepClear();
    }

    @NonNull
    private ParseState[] deepClear() {
        final var SIZE = Utility.INITIAL_CAPACITY;

        MemoryUsage.memoryAllocated(ParseState.STATE_SIZE, SIZE);
        var elements = new ParseState[SIZE];
        for (int i = 0; i < SIZE; i++) {
            elements[i] = new ParseState(ParseState.DEFAULT_BRACKET, ParseState.DEFAULT_START);
        }
        clear();
        return elements;
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
            elements = Arrays.copyOf(elements, Utility.growSize(elements.length));
        }
        var state = elements[s];
        if (state != null) {
            state.reset(bracket, start);
        } else {
            elements[s] = new ParseState(bracket, start);
        }
        top = s;
    }

    private void checkStack() {
        if (isEmpty()) {
            Contract.fail("Stack is empty");
        }
    }

    private ParseState peekUnchecked() {
        return elements[top];
    }

    public ParseState peek() {
        checkStack();
        return peekUnchecked();
    }

    public ParseState pop() {
        checkStack();
        return elements[top--];
    }

    public ParseState peekNoThrow() {
        return isEmpty() ? null : peekUnchecked();
    }

    public void purgeMemory() {
        elements = deepClear();
    }
}
