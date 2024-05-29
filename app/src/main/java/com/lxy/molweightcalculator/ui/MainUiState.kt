package com.lxy.molweightcalculator.ui

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lxy.molweightcalculator.util.Utility

@Stable
class MainUiState() : Parcelable {
    var formula by mutableStateOf("")
    var precision by mutableFloatStateOf(Utility.DEFAULT_PRECISION.toFloat())
    var sortOrder by mutableIntStateOf(0)
    var sortMethod by mutableIntStateOf(0)

    constructor(parcel: Parcel) : this() {
        formula = parcel.readString() ?: ""
        precision = parcel.readFloat()
        sortOrder = parcel.readInt()
        sortMethod = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(formula)
        parcel.writeFloat(precision)
        parcel.writeInt(sortOrder)
        parcel.writeInt(sortMethod)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainUiState> {
        override fun createFromParcel(parcel: Parcel): MainUiState {
            return MainUiState(parcel)
        }

        override fun newArray(size: Int): Array<MainUiState?> {
            return arrayOfNulls(size)
        }
    }
}