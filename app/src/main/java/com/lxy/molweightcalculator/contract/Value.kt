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
        inline fun <reified T> unsupportedType(): T {
            return fail("Unsupported type ${T::class.javaClass.simpleName}")
        }

        inline fun <reified T : Comparable<T>> add(left: T, right: T): T {
            val type = T::class
            return (when (type) {
                Byte::class -> (left as Byte) + (right as Byte)
                UByte::class -> (left as UByte) + (right as UByte)
                Char::class -> ((left as Char).code + (right as Char).code).toChar()
                Short::class -> (left as Short) + (right as Short)
                UShort::class -> (left as UShort) + (right as UShort)
                Int::class -> (left as Int) + (right as Int)
                UInt::class -> (left as UInt) + (right as UInt)
                Long::class -> (left as Long) + (right as Long)
                ULong::class -> (left as ULong) + (right as ULong)
                Float::class -> (left as Float) + (right as Float)
                Double::class -> (left as Double) + (right as Double)
                BigInteger::class -> (left as BigInteger) + (right as BigInteger)
                BigDecimal::class -> (left as BigDecimal) + (right as BigDecimal)
                else -> unsupportedType()
            } as T)
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
                else -> unsupportedType()
            } as T)
        }

        inline fun <reified T : Comparable<T>> zeroValue(): Value<T> {
            return Value(zero())
        }
    }
}
