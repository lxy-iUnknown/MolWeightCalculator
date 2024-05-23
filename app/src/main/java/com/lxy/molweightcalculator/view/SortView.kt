package com.lxy.molweightcalculator.view

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.ui.DropDownMenuState
import com.lxy.molweightcalculator.ui.DropDownView
import com.lxy.molweightcalculator.util.SortUtil

@Composable
fun SortOrderView(
    parseResult: ParseResult,
    sortMethodState: DropDownMenuState,
    sortOrderState: DropDownMenuState,
    modifier: Modifier
) {
    DropDownView(
        label = stringResource(id = R.string.sort_order_label),
        options = stringArrayResource(id = R.array.sort_orders),
        state = sortOrderState,
        onItemSelected = {
            if (parseResult.succeeded) {
                SortUtil.sortStatistics(
                    parseResult = parseResult,
                    sortOrder = sortOrderState.selectedIndex,
                    sortMethod = sortMethodState.selectedIndex
                )
            }
        },
        modifier = modifier.widthIn(max = 300.dp)
    )
}

@Composable
fun SortMethodView(
    parseResult: ParseResult,
    sortMethodState: DropDownMenuState,
    sortOrderState: DropDownMenuState,
    modifier: Modifier
) {
    DropDownView(
        label = stringResource(id = R.string.sort_method_label),
        options = stringArrayResource(id = R.array.sort_methods),
        state = sortMethodState,
        onItemSelected = {
            if (parseResult.succeeded) {
                SortUtil.sortStatistics(
                    parseResult = parseResult,
                    sortOrder = sortOrderState.selectedIndex,
                    sortMethod = sortMethodState.selectedIndex
                )
            }
        },
        modifier = modifier.widthIn(max = 300.dp)
    )
}