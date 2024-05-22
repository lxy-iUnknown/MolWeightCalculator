package com.lxy.molweightcalculator.util

import com.lxy.molweightcalculator.parsing.ElementId

interface IStatistics {
    interface TraverseFunction {
        fun visit(key: ElementId, value: Long)
    }

    fun size(): Int

    fun forEach(function: TraverseFunction)

    companion object {
        fun appendStatistics(
            sb: StringBuilder,
            statistics: IStatistics
        ) {
            val size = statistics.size()
            sb.append('{')
            statistics.forEach(object : TraverseFunction {
                private var count = size

                override fun visit(key: ElementId, value: Long) {
                    sb.append(key.elementName)
                    sb.append('=')
                    sb.append(value)
                    if (--count > 0) {
                        sb.append(", ")
                    }
                }
            })
            sb.append('}')
        }
    }
}
