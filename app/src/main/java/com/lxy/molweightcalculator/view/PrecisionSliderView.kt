package com.lxy.molweightcalculator.view

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lxy.molweightcalculator.util.Utility

@Composable
fun PrecisionSliderView(
    precision: Float,
    onPrecisionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = precision,
        valueRange = Utility.VALUE_RANGE,
        steps = Utility.PRECISION_COUNT - 1,
        onValueChange = onPrecisionChange,
        modifier = modifier,
    )
}