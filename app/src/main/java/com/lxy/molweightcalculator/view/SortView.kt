package com.lxy.molweightcalculator.view

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.ui.DropDownOptions
import com.lxy.molweightcalculator.ui.DropDownView
import com.lxy.molweightcalculator.util.SortUtil

@Composable
fun SortOrderView(
    parseResult: ParseResult,
    sortMethodProvider: () -> Int,
    sortOrder: Int,
    onSortOrderChange: (Int) -> Unit,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    DropDownView(
        label = stringResource(id = R.string.sort_order_label),
        options = DropDownOptions(stringArrayResource(id = R.array.sort_orders)),
        selectedIndex = sortOrder,
        onItemSelected = {
            onSortOrderChange(it)
            if (parseResult.succeeded) {
                SortUtil.sortStatistics(
                    parseResult = parseResult,
                    sortOrder = it,
                    sortMethod = sortMethodProvider(),
                    coroutineScope = coroutineScope
                )
            }
        },
        modifier = modifier.widthIn(max = 300.dp)
    )
}

@Composable
fun SortMethodView(
    parseResult: ParseResult,
    sortMethod: Int,
    sortOrderProvider: () -> Int,
    onSortMethodChange: (Int) -> Unit,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    DropDownView(
        label = stringResource(id = R.string.sort_method_label),
        options = DropDownOptions(stringArrayResource(id = R.array.sort_methods)),
        selectedIndex = sortMethod,
        onItemSelected = {
            onSortMethodChange(it)
            if (parseResult.succeeded) {
                SortUtil.sortStatistics(
                    parseResult = parseResult,
                    sortOrder = sortOrderProvider(),
                    sortMethod = it,
                    coroutineScope = coroutineScope
                )
            }
        },
        modifier = modifier.widthIn(max = 300.dp)
    )
}