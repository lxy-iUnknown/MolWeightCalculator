package com.lxy.molweightcalculator.contract

import com.lxy.molweightcalculator.BuildConfig
import timber.log.Timber

object Contract {
    private val EMPTY_ARGS = arrayOfNulls<Any>(0)

    private fun <T> failInternal(message: String, cause: Throwable?): T {
        if (BuildConfig.DEBUG) {
            Timber.wtf(cause, message, *EMPTY_ARGS)
        }
        throw RuntimeException(message, cause)
    }

    fun <T : Comparable<T>> requireOperation(left: Value<T>, right: Value<T>, op: Operator) {
        if (!op.test(left.value, right.value)) {
            fail<Any>("$left ${op.negatedOpName} $right")
        }
    }

    fun <T> fail(format: String, cause: Throwable?, vararg args: Any?): T {
        return failInternal(String.format(format, *args), cause)
    }

    fun <T> fail(message: String): T {
        return fail(message, null as Throwable?)
    }

    fun <T> fail(message: String, cause: Throwable?): T {
        return failInternal(message, cause)
    }

    fun <T> fail(message: String, vararg args: Any?): T {
        return fail(message, null, *args)
    }

    fun require(value: Boolean, message: String?) {
        if (!value) {
            fail<Any>("Contract failed, %s", message)
        }
    }

    fun <T : Comparable<T>> requireInRangeInclusive(value: Value<T>, min: T, max: T) {
        val minValue = Value(min)
        val maxValue = Value(max)
        requireOperation(minValue, maxValue, Operator.LE)
        requireOperation(value, minValue, Operator.GE)
        requireOperation(value, maxValue, Operator.LE)
    }

    inline fun <reified T : Comparable<T>> requireValidIndex(index: T, length: T) {
        requireValidFromToIndex(Value.zero(), index, length)
    }

    inline fun <reified T : Comparable<T>> requireValidFromIndexSize(
        fromIndex: T,
        size: T,
        length: T
    ) {
        requireValidFromToIndex(fromIndex, Value.add(fromIndex, size), length)
    }

    inline fun <reified T : Comparable<T>> requireValidFromToIndex(
        fromIndex: T, toIndex: T, length: T
    ) {
        val fromIndexValue = Value("fromIndex", fromIndex)
        val toIndexValue = Value("toIndex", toIndex)
        requireOperation(fromIndexValue, Value.zeroValue(), Operator.GE)
        requireOperation(fromIndexValue, toIndexValue, Operator.LE)
        requireOperation(toIndexValue, Value("length", length), Operator.LT)
    }
}