package com.lxy.molweightcalculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.window.core.ExperimentalWindowCoreApi
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import com.lxy.molweightcalculator.ui.DeviceKind.FoldingHorizontal
import com.lxy.molweightcalculator.ui.DeviceKind.FoldingVertical
import com.lxy.molweightcalculator.ui.DeviceKind.Large
import com.lxy.molweightcalculator.ui.DeviceKind.Medium
import com.lxy.molweightcalculator.ui.DeviceKind.Phone
import com.lxy.molweightcalculator.ui.DeviceKind.PhoneLandscape
import kotlinx.coroutines.launch

private val windowMetricsCalculator = WindowMetricsCalculator.getOrCreate()

// 0.6f -> 0x3f19999a, 0.4f -> 0x3ecccccd
private const val PHONE_LANDSCAPE_PANE_WEIGHTS = 0x3ecccccd_3f19999a

@Composable
fun OnDeviceChange(
    onDeviceKindChange: (DeviceKind) -> Unit,
    onPaneWeightChange: (PaneWeight) -> Unit,
    content: @Composable () -> Unit
) {
    if (LocalInspectionMode.current) {
        // https://issuetracker.google.com/issues/319957681
        // https://stackoverflow.com/questions/77515248/how-to-calculate-windowsizeclass-in-jetpack-compose-preview
        val configuration = LocalConfiguration.current
        // Foldable device is not supported in preview mode
        registerChangesNonFoldable(
            windowSizeClass = WindowSizeClass.compute(
                configuration.screenWidthDp.toFloat(),
                configuration.screenHeightDp.toFloat()
            ),
            onDeviceKindChange = onDeviceKindChange,
            onPaneWeightChange = onPaneWeightChange
        )
    } else {
        val context = LocalContext.current
        val windowInfoTracker = WindowInfoTracker.getOrCreate(context)
        val coroutineScope = rememberCoroutineScope()

        val density = LocalDensity.current.density
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            coroutineScope.launch {
                windowInfoTracker.windowLayoutInfo(context).collect {
                    val bounds = windowMetricsCalculator
                        .computeCurrentWindowMetrics(context).bounds
                    registerChangesInternal(
                        windowSize = IntSize(bounds.width(), bounds.height()),
                        density = density,
                        displayFeatures = it.displayFeatures,
                        onDeviceKindChange = onDeviceKindChange,
                        onPaneWeightChange = onPaneWeightChange
                    )
                }
            }
        }
    }
    content()
}

private fun registerChangesInternal(
    windowSize: IntSize,
    density: Float,
    displayFeatures: List<DisplayFeature>,
    onDeviceKindChange: (DeviceKind) -> Unit,
    onPaneWeightChange: (PaneWeight) -> Unit,
) {
    val displayFeature = displayFeatures.firstOrNull()
    if (displayFeature == null) {
        // Not foldable
        registerChangesNonFoldable(
            windowSize = windowSize,
            density = density,
            onDeviceKindChange = onDeviceKindChange,
            onPaneWeightChange = onPaneWeightChange
        )
    } else if (displayFeature is FoldingFeature) {
        val bounds = displayFeature.bounds
        if (displayFeature.orientation
            == FoldingFeature.Orientation.VERTICAL
        ) {
            val windowWidth = windowSize.width
            onDeviceKindChange(FoldingHorizontal)
            onPaneWeightChange(
                PaneWeight(
                    bounds.left.toFloat() / windowWidth,
                    (windowWidth - bounds.right.toFloat()) / windowWidth
                )
            )
        } else {
            val windowHeight = windowSize.height
            onDeviceKindChange(FoldingVertical)
            onPaneWeightChange(
                PaneWeight(
                    bounds.top.toFloat() / windowHeight,
                    (windowHeight - bounds.bottom.toFloat()) / windowHeight
                )
            )
        }
    } else {
        // Unsupported feature
        registerChangesNonFoldable(
            windowSize = windowSize,
            density = density,
            onDeviceKindChange = onDeviceKindChange,
            onPaneWeightChange = onPaneWeightChange
        )
    }
}

@OptIn(ExperimentalWindowCoreApi::class)
private fun registerChangesNonFoldable(
    windowSize: IntSize,
    density: Float,
    onDeviceKindChange: (DeviceKind) -> Unit,
    onPaneWeightChange: (PaneWeight) -> Unit,
) {
    registerChangesNonFoldable(
        windowSizeClass = WindowSizeClass.compute(
            windowSize.width,
            windowSize.height,
            density
        ),
        onDeviceKindChange = onDeviceKindChange,
        onPaneWeightChange = onPaneWeightChange
    )
}

private fun registerChangesNonFoldable(
    windowSizeClass: WindowSizeClass,
    onDeviceKindChange: (DeviceKind) -> Unit,
    onPaneWeightChange: (PaneWeight) -> Unit,
) {
    val deviceKind = computeNonFoldableDeviceKind(windowSizeClass)
    onDeviceKindChange(deviceKind)
    if (deviceKind == PhoneLandscape) {
        onPaneWeightChange(PaneWeight(PHONE_LANDSCAPE_PANE_WEIGHTS))
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