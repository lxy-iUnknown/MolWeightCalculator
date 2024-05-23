package com.lxy.molweightcalculator.ui

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lxy.molweightcalculator.util.Utility
import com.lxy.molweightcalculator.view.FormulaViewState

@Stable
class MainUiState() : Parcelable {
    var formulaViewState by mutableStateOf(FormulaViewState())
    var precision by mutableFloatStateOf(Utility.DEFAULT_PRECISION.toFloat())
    var sortOrderState by mutableStateOf(DropDownMenuState())
    var sortMethodState by mutableStateOf(DropDownMenuState())

    constructor(parcel: Parcel) : this() {
        formulaViewState = FormulaViewState(parcel)
        precision = parcel.readFloat()
        sortOrderState = DropDownMenuState(parcel)
        sortMethodState = DropDownMenuState(parcel)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        formulaViewState.writeToParcel(parcel, flags)
        parcel.writeFloat(precision)
        sortOrderState.writeToParcel(parcel, flags)
        sortMethodState.writeToParcel(parcel, flags)
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