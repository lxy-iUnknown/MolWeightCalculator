package com.lxy.molweightcalculator.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.lxy.molweightcalculator.ui.DeviceKind
import com.lxy.molweightcalculator.ui.MainTheme
import com.lxy.molweightcalculator.ui.MainUiState
import com.lxy.molweightcalculator.ui.MainViewModel
import com.lxy.molweightcalculator.ui.OnDeviceChange
import com.lxy.molweightcalculator.ui.PaneWeight
import com.lxy.molweightcalculator.ui.maxFractionHeight

private val viewModel = MainViewModel(SavedStateHandle())


@Composable
fun MainView() {
    val mainUiState by rememberSaveable {
        mutableStateOf(MainUiState())
    }
    var deviceKind by remember {
        mutableStateOf(DeviceKind.Phone)
    }
    var paneWeight by remember {
        mutableStateOf(PaneWeight.INVALID)
    }

    OnDeviceChange(
        onDeviceKindChange = { deviceKind = it },
        onPaneWeightChange = { paneWeight = it },
    ) {
        val contentPadding: Dp
        val itemPadding: Dp

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
                                    .weight(paneWeight.first)
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
                                    .weight(paneWeight.second)
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
                                    .weight(paneWeight.first)
                                    .padding(bottom = 16.dp),
                                spacing = itemPadding
                            )
                            Column(
                                modifier = Modifier
                                    .width(IntrinsicSize.Min)
                                    .fillMaxWidth()
                                    .weight(1 - paneWeight.second)
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
}