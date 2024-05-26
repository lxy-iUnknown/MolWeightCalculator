package com.lxy.molweightcalculator.view

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.lxy.molweightcalculator.ui.MainUiState
import com.lxy.molweightcalculator.util.SortUtil
import com.lxy.molweightcalculator.util.readBool
import com.lxy.molweightcalculator.util.writeBool
import timber.log.Timber

@Stable
class FormulaViewState() : Parcelable {
    var formula by mutableStateOf("")
    var isFocused by mutableStateOf(false)

    constructor(parcel: Parcel) : this() {
        formula = parcel.readString() ?: ""
        isFocused = parcel.readBool()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(formula)
        parcel.writeBool(isFocused)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FormulaViewState> {
        override fun createFromParcel(parcel: Parcel): FormulaViewState {
            return FormulaViewState(parcel)
        }

        override fun newArray(size: Int): Array<FormulaViewState?> {
            return arrayOfNulls(size)
        }
    }
}

@Composable
fun FormulaView(
    mainUiState: MainUiState,
    parseResult: ParseResult,
    modifier: Modifier
) {
    val errorColor = MaterialTheme.colorScheme.error
    val formulaViewState = mainUiState.formulaViewState

    OutlinedTextField(
        label = { Text(text = stringResource(id = R.string.formula_label)) },
        value = formulaViewState.formula,
        isError = !parseResult.succeeded,
        modifier = modifier.onFocusChanged {
            formulaViewState.isFocused = it.isFocused
        },
        onValueChange = {
            formulaViewState.formula = it
            Parser.parse(it, parseResult)
            SortUtil.sortStatistics(
                parseResult = parseResult,
                sortOrder = mainUiState.sortOrderState.selectedIndex,
                sortMethod = mainUiState.sortMethodState.selectedIndex
            )
            if (BuildConfig.DEBUG) {
                Timber.d("Parse result: %s", parseResult.debugToString())
            }
        },
        trailingIcon = if (!parseResult.succeeded) {
            { Icon(imageVector = Icons.Outlined.Info, contentDescription = "error") }
        } else if (formulaViewState.isFocused) {
            {
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "clear",
                    modifier = Modifier.clickable {
                        formulaViewState.formula = ""
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