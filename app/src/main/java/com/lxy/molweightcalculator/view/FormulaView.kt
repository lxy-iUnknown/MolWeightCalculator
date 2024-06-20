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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import com.lxy.molweightcalculator.BuildConfig
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.parsing.ErrorCode
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.parsing.Parser
import com.lxy.molweightcalculator.ui.SortInfo
import com.lxy.molweightcalculator.util.SortUtil
import com.lxy.molweightcalculator.util.Utility
import com.lxy.molweightcalculator.util.getValue
import com.lxy.molweightcalculator.util.mutableBooleanStateOf
import com.lxy.molweightcalculator.util.setValue
import kotlinx.coroutines.launch
import timber.log.Timber

fun parseFormula(
    formula: String,
    parseResult: ParseResult,
) {
    Parser(formula).parse(parseResult)
    if (BuildConfig.DEBUG) {
        Timber.d("Parse result: %s", parseResult.debugToString())
    }
}

@Composable
fun FormulaView(
    formula: String,
    onFormulaChange: (String) -> Unit,
    sortInfoProvider: () -> SortInfo,
    parseResult: ParseResult,
    modifier: Modifier
) {
    var isFocused by remember {
        mutableBooleanStateOf(false)
    }
    val errorColor = MaterialTheme.colorScheme.error
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        label = { Text(text = stringResource(id = R.string.formula_label)) },
        value = formula,
        isError = !parseResult.succeeded,
        modifier = modifier.onFocusChanged {
            isFocused = it.isFocused
        },
        onValueChange = {
            val sortInfo = sortInfoProvider()
            onFormulaChange(it)
            if (it.length > Utility.BACKGROUND_THRESHOLD) {
                coroutineScope.launch {
                    parseFormula(it, parseResult)
                }
            } else {
                parseFormula(it, parseResult)
            }
            SortUtil.sortStatistics(
                parseResult = parseResult,
                sortOrder = sortInfo.sortOrder,
                sortMethod = sortInfo.sortMethod,
                coroutineScope = coroutineScope
            )
        },
        trailingIcon = if (!parseResult.succeeded) {
            { Icon(imageVector = Icons.Outlined.Info, contentDescription = "error") }
        } else if (isFocused) {
            {
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "clear",
                    modifier = Modifier.clickable {
                        onFormulaChange("")
                        parseResult.init(ErrorCode.EmptyFormula)
                    }
                )
            }
        } else {
            null
        },
        visualTransformation = {
            var text = it
            if (!parseResult.succeeded) {
                val errorCode = parseResult.errorCode
                if (errorCode.hasStartEnd) {
                    val start = parseResult.start
                    val end = if (errorCode == ErrorCode.MismatchedBracket)
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
}