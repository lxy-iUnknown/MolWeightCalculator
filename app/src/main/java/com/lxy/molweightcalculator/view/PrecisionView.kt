package com.lxy.molweightcalculator.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.util.Utility
import kotlin.math.roundToInt

@Composable
fun PrecisionView(
    precision: Float,
    onPrecisionChange: (Float) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.precision) + precision.roundToInt(),
        )
        Slider(
            value = precision,
            valueRange = Utility.VALUE_RANGE,
            steps = Utility.PRECISION_COUNT - 1,
            onValueChange = onPrecisionChange,
            modifier = Modifier
                .padding(start = 8.dp)
                .widthIn(min = 200.dp),
        )
    }
}