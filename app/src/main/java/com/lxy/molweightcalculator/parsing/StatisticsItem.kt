package com.lxy.molweightcalculator.parsing

import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.contract.Contract
import com.lxy.molweightcalculator.contract.Operator
import com.lxy.molweightcalculator.contract.Value
import com.lxy.molweightcalculator.util.buildEquals
import com.lxy.molweightcalculator.util.mix

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

    override fun hashCode(): Int {
        return elementId.hashCode().mix(count)
    }

    override fun equals(other: Any?): Boolean {
        return buildEquals(other, ::simpleEquals)
    }
}
