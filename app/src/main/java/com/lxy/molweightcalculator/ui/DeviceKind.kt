package com.lxy.molweightcalculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize
import androidx.window.core.ExperimentalWindowCoreApi
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import kotlinx.coroutines.launch

enum class DeviceKind {
    Phone,
    PhoneLandscape,
    FoldingHorizontal,
    FoldingVertical,
    Medium,
    Large;

    companion object {
        private val KEY = Any()
        private val windowMetricsCalculator = WindowMetricsCalculator.getOrCreate()

        private const val PHONE_LANDSCAPE_FIRST_PANE_WEIGHT = 0.6f

        @Composable
        fun RegisterChanges(
            onDeviceKindChange: (DeviceKind) -> Unit,
            onFirstPaneWeightChange: (Float) -> Unit,
        ) {
            if (LocalInspectionMode.current) {
                // https://issuetracker.google.com/issues/319957681
                // https://stackoverflow.com/questions/77515248/how-to-calculate-windowsizeclass-in-jetpack-compose-preview
                val configuration = LocalConfiguration.current
                val windowSizeClass = WindowSizeClass.compute(
                    configuration.screenWidthDp.toFloat(),
                    configuration.screenHeightDp.toFloat()
                )
                // Foldable device is not supported in preview mode
                val deviceKind = computeNonFoldableDeviceKind(windowSizeClass)
                onDeviceKindChange(deviceKind)
                if (deviceKind == PhoneLandscape) {
                    onFirstPaneWeightChange(PHONE_LANDSCAPE_FIRST_PANE_WEIGHT)
                }
            } else {
                val context = LocalContext.current
                val windowInfoTracker = WindowInfoTracker.getOrCreate(context)
                val coroutineScope = rememberCoroutineScope()

                val density = LocalDensity.current.density
                LaunchedEffect(KEY) {
                    coroutineScope.launch {
                        windowInfoTracker.windowLayoutInfo(context).collect {
                            val bounds = windowMetricsCalculator
                                .computeCurrentWindowMetrics(context).bounds
                            registerChangesInternal(
                                windowSize = IntSize(bounds.width(), bounds.height()),
                                density = density,
                                displayFeatures = it.displayFeatures,
                                onDeviceKindChange = onDeviceKindChange,
                                onFirstPaneWeightChange = onFirstPaneWeightChange
                            )
                        }
                    }
                }
            }
        }

        private fun registerChangesInternal(
            windowSize: IntSize,
            density: Float,
            displayFeatures: List<DisplayFeature>,
            onDeviceKindChange: (DeviceKind) -> Unit,
            onFirstPaneWeightChange: (Float) -> Unit,
        ) {
            val displayFeature = displayFeatures.firstOrNull()
            if (displayFeature == null) {
                // Not foldable
                registerChangesNonFoldable(
                    windowSize = windowSize,
                    density = density,
                    onDeviceKindChange = onDeviceKindChange,
                    onFirstPaneWeightChange = onFirstPaneWeightChange
                )
            } else if (displayFeature is FoldingFeature) {
                val bounds = displayFeature.bounds
                if (displayFeature.orientation
                    == FoldingFeature.Orientation.VERTICAL
                ) {
                    onDeviceKindChange(FoldingHorizontal)
                    onFirstPaneWeightChange(bounds.left.toFloat() / windowSize.width)
                } else {
                    onDeviceKindChange(FoldingVertical)
                    onFirstPaneWeightChange(bounds.top.toFloat() / windowSize.height)
                }
            } else {
                // Unsupported feature
                registerChangesNonFoldable(
                    windowSize = windowSize,
                    density = density,
                    onDeviceKindChange = onDeviceKindChange,
                    onFirstPaneWeightChange = onFirstPaneWeightChange
                )
            }
        }

        @OptIn(ExperimentalWindowCoreApi::class)
        private fun registerChangesNonFoldable(
            windowSize: IntSize,
            density: Float,
            onDeviceKindChange: (DeviceKind) -> Unit,
            onFirstPaneWeightChange: (Float) -> Unit,
        ) {
            val windowSizeClass = WindowSizeClass.compute(
                windowSize.width,
                windowSize.height,
                density
            )
            val deviceKind = computeNonFoldableDeviceKind(windowSizeClass)
            onDeviceKindChange(deviceKind)
            if (deviceKind == PhoneLandscape) {
                onFirstPaneWeightChange(PHONE_LANDSCAPE_FIRST_PANE_WEIGHT)
            }
        }

        private fun computeNonFoldableDeviceKind(windowSizeClass: WindowSizeClass): DeviceKind {
            return when (windowSizeClass.windowHeightSizeClass) {
                WindowHeightSizeClass.MEDIUM,
                WindowHeightSizeClass.EXPANDED -> {
                    when (windowSizeClass.windowWidthSizeClass) {
                        WindowWidthSizeClass.MEDIUM -> Medium
                        WindowWidthSizeClass.EXPANDED -> Large
                        // Compact or unknown
                        else -> Phone
                    }
                }
                // Compact or unknown
                else -> PhoneLandscape
            }
        }
    }
}