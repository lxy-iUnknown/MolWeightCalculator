package com.lxy.molweightcalculator.parsing

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.util.MathUtil.addExact
import com.lxy.molweightcalculator.util.MathUtil.multiplyAddExact
import com.lxy.molweightcalculator.util.MathUtil.multiplyExact
import com.lxy.molweightcalculator.util.Utility
import com.lxy.molweightcalculator.util.Utility.appendStatistics

class ParseState(bracket: Bracket, start: Int) {
    private var keys: CharArray
    private var values: LongArray
    private var size = 0
    var bracket = Bracket.Invalid
        private set
    var start = 0
        private set
    var weight = 0.0

    init {
        this.keys = EMPTY_CHAR
        this.values = EMPTY_LONG
        reset(bracket, start)
    }

    fun reset(bracket: Bracket, start: Int) {
        this.size = 0
        this.bracket = bracket
        this.start = start
        this.weight = 0.0
    }

    private fun insert(index: Int, key: Char, value: Long) {
        val capacity = capacity()
        if (size + 1 <= capacity) {
            // Keys
            System.arraycopy(keys, index, keys, index + 1, size - index)
            keys[index] = key
            // Values
            System.arraycopy(values, index, values, index + 1, size - index)
            values[index] = value
        } else {
            val newSize = Utility.growSize(size)
            MemoryUsage.memoryAllocated(STATE_ITEM_SIZE.toLong(), newSize)
            // Keys
            val newKeys = CharArray(newSize)
            System.arraycopy(keys, 0, newKeys, 0, index)
            newKeys[index] = key
            System.arraycopy(keys, index, newKeys, index + 1, capacity - index)
            keys = newKeys
            // Values
            val newValues = LongArray(newSize)
            System.arraycopy(values, 0, newValues, 0, index)
            newValues[index] = value
            System.arraycopy(values, index, newValues, index + 1, capacity - index)
            values = newValues
        }
    }

    private fun capacity(): Int {
        return keys.size
    }

    private fun indexOfKey(key: Char): Int {
        return keys.binarySearch(key, 0, size)
    }

    @Suppress("unused")
    fun put(key: Char, value: Long) {
        var i = indexOfKey(key)
        if (i >= 0) {
            setValueAt(i, value)
        } else {
            i = i.inv()
            insert(i, key, value)
            size++
        }
    }

    // Simple algorithm for merging two sorted array
    fun merge(other: ParseState, count: Long): Boolean {
        val m = size
        val n = other.size
        val keys1 = keys
        val keys2 = other.keys
        val values1 = values
        val values2 = other.values
        val capacity2 = other.capacity()
        val total = capacity() + capacity2
        val newKeys = CharArray(total)
        val newValues = LongArray(total)
        var i = 0
        var j = 0
        var newSize = 0
        var newValue: Long
        while (i < m && j < n) {
            val key1 = keys1[i]
            val key2 = keys2[j]
            if (key1 < key2) {
                newKeys[newSize] = key1
                newValues[newSize] = values1[i++]
            } else if (key1 > key2) {
                newValue = values2[j++].multiplyExact(count)
                if (newValue < 0) {
                    return false
                }
                newKeys[newSize] = key2
                newValues[newSize] = newValue
            } else {
                newValue = values1[i++].multiplyAddExact(values2[j++], count)
                if (newValue < 0) {
                    return false
                }
                newKeys[newSize] = key1
                newValues[newSize] = newValue
            }
            newSize++
        }
        while (i < m) {
            newKeys[newSize] = keys1[i]
            newValues[newSize++] = values1[i++]
        }
        while (j < n) {
            newValue = values2[j].multiplyExact(count)
            if (newValue < 0) {
                return false
            }
            newKeys[newSize] = keys2[j++]
            newValues[newSize++] = newValue
        }
        MemoryUsage.memoryAllocated(STATE_ITEM_SIZE.toLong(), capacity2)
        keys = newKeys
        values = newValues
        size = newSize
        return true
    }

    fun addValueOrPut(key: Char, delta: Long): Boolean {
        var i = indexOfKey(key)
        if (i >= 0) {
            val newValue = valueAt(i).addExact(delta)
            if (newValue < 0) {
                return false
            }
            setValueAt(i, newValue)
        } else {
            i = i.inv()
            insert(i, key, delta)
            size++
        }
        return true
    }

    fun keyAt(index: Int): Char {
        return keys[index]
    }

    fun valueAt(index: Int): Long {
        return values[index]
    }

    private fun setValueAt(index: Int, value: Long) {
        values[index] = value
    }

    fun size(): Int {
        return size
    }

    override fun toString(): String {
        if (!BuildConfig.DEBUG) {
            return super.toString()
        }
        return StringBuilder("ParseState(statistics=")
            .appendStatistics((0 until this@ParseState.size()).map {
                Pair(ElementId(keyAt(it)), valueAt(it))
            })
            .append(", bracket=\"")
            .append(if (bracket == Bracket.Invalid) "<no bracket>" else getBracketString(bracket))
            .append("\", start=")
            .append(start)
            .append(", weight=")
            .append(weight)
            .append(')').toString()
    }

    companion object {
        const val STATE_SIZE: Int = 8 +  // Object
                4 +  // keys
                4 +  // values
                4 +  // size
                4 +  // bracket
                4 +  // start
                8 // weight

        val DEFAULT_BRACKET = Bracket.Invalid
        const val DEFAULT_START = -1
        private val BRACKET_STRINGS = arrayOf(
            "", "(", ")", "[", "]", "{", "}"
        )
        private const val STATE_ITEM_SIZE = Character.BYTES + java.lang.Long.BYTES
        private val EMPTY_CHAR = CharArray(0)
        private val EMPTY_LONG = LongArray(0)
        fun getBracketString(bracket: Int): String {
            return BRACKET_STRINGS[bracket]
        }

        fun getBracketString(bracket: Bracket): String {
            return getBracketString(bracket.ordinal)
        }
    }
}
