package com.lxy.molweightcalculator.parsing

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.util.MathUtil


@JvmInline
value class ElementId(val value: Char) {
    companion object {
        private const val BASE = 26

        const val MAX_VALUE = BASE + BASE * BASE - 1
        const val INVALID_VALUE: Int = -1

        val INVALID: ElementId = ElementId(INVALID_VALUE.toChar())

        fun valueOf(firstChar: Char): ElementId {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(
                    Value("firstChar", firstChar), 'A', 'Z'
                )
            }
            return ElementId((firstChar.code - 'A'.code).toChar())
        }

        fun valueOf(firstChar: Char, secondChar: Char): ElementId {
            if (BuildConfig.DEBUG) {
                Contract.requireInRangeInclusive(
                    Value("firstChar", firstChar), 'A', 'Z'
                )
                Contract.requireInRangeInclusive(
                    Value("secondChar", secondChar), 'a', 'z'
                )
            }
            return ElementId(
                (firstChar.code * 26 + secondChar.code
                        - (('A'.code - 1) * 26 + 'a'.code)).toChar()
            )
        }
    }

    init {
        if (BuildConfig.DEBUG) {
            Contract.requireInRangeInclusive(
                Value("elementId", value.code.toShort().toInt()), INVALID_VALUE, MAX_VALUE
            )
        }
    }

    val isValid: Boolean get() = value.code.toShort() >= 0

    val elementName: String
        get() {
            val code = value.code
            if (code < BASE) {
                return ('A'.code + code).toChar().toString()
            } else {
                val div = MathUtil.div26(code)
                return String(
                    charArrayOf(
                        (('A'.code - 1) + div).toChar(),
                        ('a'.code + code - div * BASE).toChar()
                    ), 0, 2
                )
            }
        }

    val ordinal get() = BuildConfig.ELEMENT_ORDINALS[value.code]

    val weight get() = BuildConfig.ELEMENT_WEIGHTS[value.code]
}