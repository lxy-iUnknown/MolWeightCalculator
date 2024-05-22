package com.lxy.molweightcalculator.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lxy.molweightcalculator.R
import kotlin.math.roundToInt

@Composable
fun PrecisionLabelView(
    precision: Float,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.precision) + precision.roundToInt(),
        modifier = modifier,
    )
}