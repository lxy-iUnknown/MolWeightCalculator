package com.lxy.molweightcalculator.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.ui.DeviceKind
import com.lxy.molweightcalculator.ui.MainTheme
import com.lxy.molweightcalculator.ui.MainUiState
import com.lxy.molweightcalculator.ui.MainViewModel
import com.lxy.molweightcalculator.ui.maxFractionHeight

private val viewModel = MainViewModel(SavedStateHandle())


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsView(
    mainUiState: MainUiState,
    parseResult: ParseResult,
    itemPadding: Dp,
    modifier: Modifier
) {
    FormulaView(
        formulaViewState = mainUiState.formulaViewState,
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


@Composable
fun MainView() {
    val mainUiState by rememberSaveable {
        mutableStateOf(MainUiState())
    }
    var deviceKind by remember {
        mutableStateOf(DeviceKind.Phone)
    }
    var firstPaneWeight by remember {
        mutableFloatStateOf(Float.NaN)
    }

    val contentPadding: Dp
    val itemPadding: Dp

    DeviceKind.RegisterChanges(
        onDeviceKindChange = { deviceKind = it },
        onFirstPaneWeightChange = { firstPaneWeight = it },
    )

    when (deviceKind) {
        DeviceKind.Phone,
        DeviceKind.PhoneLandscape,
        -> {
            contentPadding = 16.dp
            itemPadding = 8.dp
        }

        DeviceKind.Medium,
        DeviceKind.FoldingHorizontal,
        DeviceKind.FoldingVertical -> {
            contentPadding = 18.dp
            itemPadding = 10.dp
        }

        DeviceKind.Large -> {
            contentPadding = 20.dp
            itemPadding = 12.dp
        }
    }

    MainTheme(deviceKind) { ->
        Scaffold {
            val parseResult = viewModel.parseResult
            val containerModifier = Modifier
                .padding(paddingValues = it)
                .then(Modifier.padding(contentPadding))
            when (deviceKind) {
                DeviceKind.PhoneLandscape, DeviceKind.FoldingHorizontal -> {
                    val hingePadding =
                        if (deviceKind == DeviceKind.PhoneLandscape)
                            0.dp
                        else
                            16.dp
                    Row(
                        modifier = containerModifier,
                        horizontalArrangement = Arrangement.spacedBy(itemPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxHeight()
                                .weight(firstPaneWeight)
                                .padding(end = hingePadding),
                            verticalArrangement = Arrangement.spacedBy(itemPadding)
                        ) {
                            SettingsView(
                                mainUiState = mainUiState,
                                parseResult = parseResult,
                                itemPadding = itemPadding,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                        StatisticsView(
                            parseResult = parseResult,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1 - firstPaneWeight)
                                .padding(start = hingePadding),
                            spacing = itemPadding
                        )
                    }
                }

                DeviceKind.FoldingVertical -> {
                    Column(
                        modifier = containerModifier,
                        verticalArrangement = Arrangement.spacedBy(itemPadding)
                    ) {
                        StatisticsView(
                            parseResult = parseResult,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(firstPaneWeight)
                                .padding(bottom = 16.dp),
                            spacing = itemPadding
                        )
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .fillMaxWidth()
                                .weight(1 - firstPaneWeight)
                                .padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(itemPadding)
                        ) {
                            SettingsView(
                                mainUiState = mainUiState,
                                parseResult = parseResult,
                                itemPadding = itemPadding,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = containerModifier,
                        verticalArrangement = Arrangement.spacedBy(itemPadding)
                    ) {
                        SettingsView(
                            mainUiState = mainUiState,
                            parseResult = parseResult,
                            itemPadding = itemPadding,
                            modifier = Modifier
                                .fillMaxWidth()
                                .maxFractionHeight(0.45f)
                        )
                        StatisticsView(
                            parseResult = parseResult,
                            modifier = Modifier,
                            spacing = itemPadding
                        )
                    }
                }
            }
        }
    }
}