package com.lxy.molweightcalculator.util

private const val PRIME = 31

private fun Int.internalMix(hashCode: Int) = PRIME * this + hashCode

fun Int.mix(value: Boolean) = internalMix(value.hashCode())

fun Int.mix(value: Byte) = internalMix(value.hashCode())

fun Int.mix(value: Char) = internalMix(value.hashCode())

fun Int.mix(value: Short) = internalMix(value.hashCode())

fun Int.mix(value: Int) = internalMix(value.hashCode())

fun Int.mix(value: Long) = internalMix(value.hashCode())

fun Int.mix(value: Float) = internalMix(value.hashCode())

fun Int.mix(value: Double) = internalMix(value.hashCode())

fun <T> Int.mix(value: T) = internalMix(value.hashCode())

inline fun <reified T> T.buildEquals(
    other: Any?,
    crossinline fieldComparer: (T) -> Boolean
): Boolean {
    if (this === other) {
        return true
    }
    if (other is T) {
        return fieldComparer(other)
    }
    return false
}