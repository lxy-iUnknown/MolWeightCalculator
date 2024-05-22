package com.lxy.molweightcalculator.parsing

import androidx.compose.runtime.Immutable
import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Operator
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.util.HashCode

@Immutable
class StatisticsItem(elementId: ElementId, count: Long) {
    val elementId: ElementId
    val count: Long

    init {
        if (BuildConfig.DEBUG) {
            Contract.requireOperation(Value("count", count), Value.zeroValue(), Operator.GE)
        }
        this.elementId = elementId
        this.count = count
    }

    fun simpleEquals(other: StatisticsItem): Boolean {
        return elementId == other.elementId &&
                count == other.count
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is StatisticsItem) {
            return simpleEquals(other)
        }
        return false
    }

    override fun hashCode(): Int {
        return HashCode(elementId)
            .mix(count)
            .build()
    }
}
