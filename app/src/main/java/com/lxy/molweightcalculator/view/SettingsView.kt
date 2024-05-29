package com.lxy.molweightcalculator.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.ui.MainUiState
import com.lxy.molweightcalculator.ui.SortInfo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsView(
    mainUiState: MainUiState,
    parseResult: ParseResult,
    itemPadding: Dp,
    modifier: Modifier
) {
    FormulaView(
        formula = mainUiState.formula,
        onFormulaChange = { mainUiState.formula = it },
        sortInfoProvider = { SortInfo(mainUiState.sortOrder, mainUiState.sortMethod) },
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
        PrecisionView(
            precision = mainUiState.precision,
            onPrecisionChange = { mainUiState.precision = it },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
        )
        SortOrderView(
            parseResult = parseResult,
            sortMethodProvider = { mainUiState.sortMethod },
            sortOrder = mainUiState.sortOrder,
            onSortOrderChange = { mainUiState.sortOrder = it },
            modifier = Modifier.weight(1f)
        )
        SortMethodView(
            parseResult = parseResult,
            sortMethod = mainUiState.sortMethod,
            sortOrderProvider = { mainUiState.sortOrder },
            onSortMethodChange = { mainUiState.sortMethod = it },
            modifier = Modifier.weight(1f)
        )
    }
}