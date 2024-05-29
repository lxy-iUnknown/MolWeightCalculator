package com.lxy.molweightcalculator.contract

import com.lxy.molweightcalculator.contract.Contract.fail
import java.math.BigDecimal
import java.math.BigInteger

class Value<T : Comparable<T>>(private val name: String?, val value: T) {
    constructor(value: T) : this(null, value)

    override fun toString(): String {
        val valueString = value.toString()
        if (name == null) {
            return valueString
        }
        return "$name($valueString)"
    }

    companion object {
        inline fun <reified T> unsupportedType(): Nothing {
            fail("Unsupported type ${T::class.javaClass.simpleName}")
        }

        inline fun <reified T : Comparable<T>> zero(): T {
            val type = T::class
            return (when (type) {
                Byte::class -> 0.toByte()
                UByte::class -> 0.toUByte()
                Char::class -> 0.toChar()
                Short::class -> 0.toShort()
                UShort::class -> 0.toUShort()
                Int::class -> 0
                UInt::class -> 0.toUInt()
                Long::class -> 0.toLong()
                ULong::class -> 0.toULong()
                Float::class -> 0.toFloat()
                Double::class -> 0.toDouble()
                BigInteger::class -> BigInteger.ZERO
                BigDecimal::class -> BigDecimal.ZERO
                else -> unsupportedType<T>()
            } as T)
        }

        inline fun <reified T : Comparable<T>> zeroValue(): Value<T> {
            return Value(zero())
        }
    }
}
