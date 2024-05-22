package com.lxy.molweightcalculator.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.lxy.molweightcalculator.parsing.ParseResult


@OptIn(SavedStateHandleSaveableApi::class)
class MainViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    var parseResult by savedStateHandle.saveable {
        mutableStateOf(ParseResult.EMPTY_FORMULA)
    }
}