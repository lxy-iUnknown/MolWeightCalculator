package com.lxy.molweightcalculator.contract

import com.lxy.molweightcalculator.BuildConfig
import timber.log.Timber

object Contract {
    private val EMPTY_ARGS = arrayOfNulls<Any>(0)

    private fun failInternal(message: String, cause: Throwable?): Nothing {
        if (BuildConfig.DEBUG) {
            Timber.wtf(cause, message, *EMPTY_ARGS)
        }
        throw RuntimeException(message, cause)
    }

    fun <T : Comparable<T>> requireOperation(left: Value<T>, right: Value<T>, op: Operator) {
        if (!op.test(left.value, right.value)) {
            fail("$left ${op.negatedOpName} $right")
        }
    }

    fun fail(format: String, cause: Throwable?, vararg args: Any?): Nothing {
        failInternal(String.format(format, *args), cause)
    }

    fun fail(message: String): Nothing {
        fail(message, null as Throwable?)
    }

    fun fail(message: String, cause: Throwable?): Nothing {
        failInternal(message, cause)
    }

    fun fail(message: String, vararg args: Any?): Nothing {
        fail(message, null, *args)
    }

    fun require(value: Boolean, message: String?) {
        if (!value) {
            fail("Contract failed, %s", message)
        }
    }

    inline fun <reified T : Comparable<T>> requireInRangeInclusive(
        value: Value<T>,
        min: T,
        max: T
    ) {
        val minValue = Value(min)
        val maxValue = Value(max)
        requireOperation(minValue, maxValue, Operator.LE)
        requireOperation(value, minValue, Operator.GE)
        requireOperation(value, maxValue, Operator.LE)
    }
}