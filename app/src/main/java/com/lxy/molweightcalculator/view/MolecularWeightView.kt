package com.lxy.molweightcalculator.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.parsing.ParseState
import kotlin.math.roundToInt

@Composable
fun MolecularWeightView(
    parseResult: ParseResult,
    precision: () -> Float,
    errorColor: Color
) {
    val smallTextStyle = MaterialTheme.typography.bodySmall
    val errorStrings = stringArrayResource(id = R.array.error_stings)

    if (parseResult.succeeded) {
        Text(
            text = stringResource(id = R.string.molecular_weight) +
                    parseResult.getWeightString(precision().roundToInt()),
            style = smallTextStyle
        )
    } else {
        Text(
            text = run {
                var errorString = errorStrings[parseResult.errorCode.ordinal]
                if (parseResult.isInvalidBracket) {
                    errorString = String.format(
                        errorString, ParseState.getBracketString(parseResult.end)
                    )
                }
                errorString
            },
            style = smallTextStyle,
            color = errorColor
        )
    }
}