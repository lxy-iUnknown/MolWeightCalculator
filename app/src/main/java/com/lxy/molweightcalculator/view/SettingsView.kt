package com.lxy.molweightcalculator.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.ui.MainUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsView(
    mainUiState: MainUiState,
    parseResult: ParseResult,
    itemPadding: Dp,
    modifier: Modifier
) {
    FormulaView(
        mainUiState = mainUiState,
        parseResult = parseResult,
        modifier = modifier
    )
    MolecularWeightView(
        parseResult = parseResult,
        precision = { mainUiState.precision }
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(itemPadding),
        verticalArrangement = Arrangement.spacedBy(itemPadding),
        maxItemsInEachRow = 3
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrecisionLabelView(precision = mainUiState.precision)
            PrecisionSliderView(
                precision = mainUiState.precision,
                onPrecisionChange = { mainUiState.precision = it },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .widthIn(min = 200.dp)
            )
        }
        SortOrderView(
            parseResult = parseResult,
            sortMethodState = mainUiState.sortMethodState,
            sortOrderState = mainUiState.sortOrderState,
            modifier = Modifier.weight(1f)
        )
        SortMethodView(
            parseResult = parseResult,
            sortMethodState = mainUiState.sortMethodState,
            sortOrderState = mainUiState.sortOrderState,
            modifier = Modifier.weight(1f)
        )
    }
}