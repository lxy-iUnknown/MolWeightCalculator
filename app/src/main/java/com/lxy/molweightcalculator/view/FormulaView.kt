package com.lxy.molweightcalculator.view

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.parsing.Parser

@Composable
fun FormulaView(
    parseResult: ParseResult,
    onParseResultChange: (ParseResult) -> Unit,
    precision: () -> Float,
    modifier: Modifier
) {
    var formula by rememberSaveable {
        mutableStateOf("")
    }
    var isFocused by rememberSaveable {
        mutableStateOf(false)
    }

    val errorColor = MaterialTheme.colorScheme.error

    OutlinedTextField(
        label = { Text(text = stringResource(id = R.string.formula_label)) },
        value = formula,
        isError = !parseResult.succeeded,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused },
        onValueChange = {
            formula = it
            onParseResultChange(Parser.parse(it))
        },
        trailingIcon = if (!parseResult.succeeded) {
            { Icon(imageVector = Icons.Outlined.Info, contentDescription = "error") }
        } else if (isFocused) {
            {
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "clear",
                    modifier = Modifier.clickable {
                        formula = ""
                        onParseResultChange(ParseResult.EMPTY_FORMULA)
                    }
                )
            }
        } else {
            null
        },
        visualTransformation = {
            var text = it
            if (!parseResult.succeeded) {
                if (parseResult.hasStartEnd) {
                    val start = parseResult.start
                    val end = if (parseResult.isInvalidBracket)
                        start + 1
                    else
                        parseResult.end
                    text = AnnotatedString(
                        text = text.text,
                        spanStyles = text.spanStyles + AnnotatedString.Range(
                            item = SpanStyle(color = errorColor),
                            start = start,
                            end = end
                        ),
                        paragraphStyles = text.paragraphStyles
                    )
                }
            }
            TransformedText(text, OffsetMapping.Identity)
        }
    )
    MolecularWeightView(parseResult, precision, errorColor)
}