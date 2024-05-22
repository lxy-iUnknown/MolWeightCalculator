package com.lxy.molweightcalculator.parsing

import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.util.Utility

class ParseStack {
    private var elements: Array<ParseState?>
    private var top = 0

    companion object {
        const val INITIAL_SIZE = Utility.INITIAL_CAPACITY
    }

    init {
        elements = deepClear()
    }

    private fun deepClear(): Array<ParseState?> {
        MemoryUsage.memoryAllocated(ParseState.STATE_SIZE.toLong(), INITIAL_SIZE)
        val elements = arrayOfNulls<ParseState>(INITIAL_SIZE)
        for (i in 0 until INITIAL_SIZE) {
            elements[i] = ParseState(ParseState.DEFAULT_BRACKET, ParseState.DEFAULT_START)
        }
        clear()
        return elements
    }

    val isEmpty get() = size() < 0

    private fun size(): Int {
        return top
    }

    fun clear() {
        top = -1
    }

    fun push(bracket: Bracket, start: Int) {
        val s = size() + 1
        if (s == elements.size) {
            elements = elements.copyOf(Utility.growSize(elements.size))
        }
        val state = elements[s]
        if (state != null) {
            state.reset(bracket, start)
        } else {
            elements[s] = ParseState(bracket, start)
        }
        top = s
    }

    private fun checkStack() {
        if (isEmpty) {
            Contract.fail<Any>("Stack is empty")
        }
    }

    private fun getItem(index: Int): ParseState {
        return elements[index]!!
    }

    private fun peekUnchecked(): ParseState {
        return getItem(top)
    }

    fun peek(): ParseState {
        checkStack()
        return peekUnchecked()
    }

    fun pop(): ParseState {
        checkStack()
        return getItem(top--)
    }

    fun peekNoThrow(): ParseState? {
        return if (isEmpty) null else peekUnchecked()
    }

    fun purgeMemory() {
        elements = deepClear()
    }
}
