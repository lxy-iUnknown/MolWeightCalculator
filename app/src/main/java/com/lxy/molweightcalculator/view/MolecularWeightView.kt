package com.lxy.molweightcalculator.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.BaselineShift
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.parsing.ParseState
import com.lxy.molweightcalculator.ui.WeightString
import kotlin.math.roundToInt

@Composable
fun MolecularWeightView(
    parseResult: ParseResult,
    precision: () -> Float
) {
    val colorScheme = MaterialTheme.colorScheme
    val smallTextStyle = MaterialTheme.typography.bodySmall
    val errorStrings = stringArrayResource(id = R.array.error_stings)

    val textColor: Color
    val text: AnnotatedString
    if (parseResult.succeeded) {
        val titleString = stringResource(id = R.string.molecular_weight)
        val weightString = WeightString.valueOf(parseResult, precision().roundToInt())
        val beginIndex = weightString.exponentBeginIndex
        val spanStyles = if (beginIndex >= 0) {
            val offset = titleString.length
            listOf(
                AnnotatedString.Range(
                    item = SpanStyle(
                        fontStyle = smallTextStyle.fontStyle,
                        baselineShift = BaselineShift.Superscript
                    ),
                    start = beginIndex + offset,
                    end = weightString.exponentEndIndex + offset
                )
            )
        } else {
            emptyList()
        }
        text = AnnotatedString(
            titleString + weightString.value,
            spanStyles = spanStyles
        )
        textColor = colorScheme.onSurfaceVariant
    } else {
        textColor = colorScheme.error
        text = AnnotatedString(run {
            val errorCode = parseResult.errorCode
            var errorString = errorStrings[errorCode.ordinal]
            if (errorCode.isInvalidBracket) {
                errorString = String.format(
                    errorString, ParseState.getBracketString(parseResult.end)
                )
            }
            errorString
        })
    }
    Text(
        text = text,
        style = smallTextStyle,
        color = textColor
    )
}