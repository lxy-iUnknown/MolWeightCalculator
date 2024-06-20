package com.lxy.molweightcalculator.util

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.*
import androidx.compose.runtime.structuralEqualityPolicy
import com.lxy.molweightcalculator.util.SnapshotUtil.Snapshot_getSnapshotInitializer
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import java.lang.invoke.MethodHandle
import kotlin.reflect.KProperty


@Suppress("SpellCheckingInspection")
private object SnapshotUtil {

    val Snapshot_getSnapshotInitializer: MethodHandle
    val Snapshot_overwritableRecord: MethodHandle
    val Snapshot_notifyWrite: MethodHandle

    init {
        val snapshotKt = Class.forName("androidx.compose.runtime.snapshots.SnapshotKt") //kotlinStaticClass(Snapshot::class.java)
        Snapshot_getSnapshotInitializer = snapshotKt.getPrivateMethod(
            "getSnapshotInitializer",
        )
        Snapshot_overwritableRecord = snapshotKt.getPrivateMethod(
            "overwritableRecord",
            StateRecord::class.java, StateObject::class.java,
            Snapshot::class.java, StateRecord::class.java,
        )
        Snapshot_notifyWrite = snapshotKt.getPrivateMethod(
            "notifyWrite", Snapshot::class.java, StateObject::class.java,
        )
    }
}

@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject()

@Suppress(
    "SpellCheckingInspection",
    "VARIABLE_WITH_REDUNDANT_INITIALIZER",
)
private inline fun <T : StateRecord, R> T.overwritable(
    state: StateObject,
    candidate: T,
    block: T.() -> R
): R {
    var snapshot = InvokeUtil.invoke<Snapshot>(Snapshot_getSnapshotInitializer)
    return synchronized(lock) {
        snapshot = Snapshot.current
        InvokeUtil.invoke<T, StateObject, Snapshot, T, T>(
            SnapshotUtil.Snapshot_overwritableRecord,
            this, state, snapshot, candidate
        ).block()
    }.also {
        InvokeUtil.invokeVoid(SnapshotUtil.Snapshot_notifyWrite, snapshot, state)
    }
}

@Stable
interface MutableBooleanState : MutableState<Boolean> {
    @get:AutoboxingStateValueProperty("booleanValue")
    @set:AutoboxingStateValueProperty("booleanValue")
    override var value: Boolean
        @Suppress("AutoBoxing") get() = booleanValue
        set(value) {
            booleanValue = value
        }

    var booleanValue: Boolean
}

private class BooleanStateStateRecord(
    var value: Boolean
) : StateRecord() {
    override fun assign(value: StateRecord) {
        this.value = (value as BooleanStateStateRecord).value
    }

    override fun create(): StateRecord = BooleanStateStateRecord(value)
}

private class ParcelableSnapshotMutableBooleanState(value: Boolean) :
    StateObject,
    MutableBooleanState,
    SnapshotMutableState<Boolean> {
    private var next = BooleanStateStateRecord(value)

    override val firstStateRecord: StateRecord
        get() = next

    override var booleanValue: Boolean
        get() = next.readable(this).value
        set(value) = next.withCurrent {
            if (it.value != value) {
                next.overwritable(this, it) { this.value = value }
            }
        }

    override val policy: SnapshotMutationPolicy<Boolean> =
        structuralEqualityPolicy()

    override fun component1(): Boolean = booleanValue

    override fun component2(): (Boolean) -> Unit = { booleanValue = it }

    override fun prependStateRecord(value: StateRecord) {
        next = value as BooleanStateStateRecord
    }

    override fun mergeRecords(
        previous: StateRecord,
        current: StateRecord,
        applied: StateRecord
    ): StateRecord? {
        val currentRecord = current as BooleanStateStateRecord
        val appliedRecord = applied as BooleanStateStateRecord
        return if (currentRecord.value == appliedRecord.value) {
            current
        } else {
            null
        }
    }
}

fun mutableBooleanStateOf(value: Boolean): MutableBooleanState =
    ParcelableSnapshotMutableBooleanState(value)

@Suppress("NOTHING_TO_INLINE")
inline operator fun MutableBooleanState.getValue(
    thisObj: Any?, property: KProperty<*>
): Boolean = booleanValue


@Suppress("NOTHING_TO_INLINE")
inline operator fun MutableBooleanState.setValue(
    thisObj: Any?, property: KProperty<*>, value: Boolean
) {
    this.booleanValue = value
}