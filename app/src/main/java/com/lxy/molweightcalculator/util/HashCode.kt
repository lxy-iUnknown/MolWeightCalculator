package com.lxy.molweightcalculator.util

@Suppress("unused")
@JvmInline
value class HashCode(private val value: Int) {
    companion object {
        private const val PRIME = 31
    }

    private fun internalMix(hash: Int) = HashCode(PRIME * value + hash)

    constructor(value: Byte) : this(value.hashCode())

    constructor(value: Char) : this(value.hashCode())

    constructor(value: Short) : this(value.hashCode())

    constructor(value: Long) : this(value.hashCode())

    constructor(value: Float) : this(value.hashCode())

    constructor(value: Double) : this(value.hashCode())

    constructor(value: Any) : this(value.hashCode())

    fun mix(value: Boolean) = internalMix(value.hashCode())

    fun mix(value: Byte) = internalMix(value.hashCode())

    fun mix(value: Char) = internalMix(value.hashCode())

    fun mix(value: Short) = internalMix(value.hashCode())

    fun mix(value: Long) = internalMix(value.hashCode())

    fun mix(value: Float) = internalMix(value.hashCode())

    fun mix(value: Double) = internalMix(value.hashCode())

    fun <T> mix(value: T) = internalMix(value.hashCode())

    fun build(): Int = value
}