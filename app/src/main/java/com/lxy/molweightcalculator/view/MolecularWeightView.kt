package com.lxy.molweightcalculator.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.parsing.ParseState
import com.lxy.molweightcalculator.util.WeightUtil
import kotlin.math.roundToInt


@Composable
fun MolecularWeightView(
    parseResult: ParseResult,
    precision: () -> Float
) {
    val errorColor = MaterialTheme.colorScheme.error
    val smallTextStyle = MaterialTheme.typography.bodySmall
    val errorStrings = stringArrayResource(id = R.array.error_stings)

    if (parseResult.succeeded) {
        Text(
            text = stringResource(id = R.string.molecular_weight) +
                    WeightUtil.getWeightString(
                        parseResult, precision().roundToInt()
                    ),
            style = smallTextStyle
        )
    } else {
        Text(
            text = run {
                val errorCode = parseResult.errorCode
                var errorString = errorStrings[errorCode.ordinal]
                if (errorCode.isInvalidBracket) {
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