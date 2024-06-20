package com.lxy.molweightcalculator.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.ui.MassRatio

@Composable
private fun StatisticsRow(
    elementName: String,
    elementCount: String,
    massRatio: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = elementName,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth()
        )
        Text(
            text = elementCount,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth()
        )
        Text(
            text = massRatio,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth()
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatisticsView(
    parseResult: ParseResult,
    modifier: Modifier = Modifier,
    spacing: Dp,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Text(
            text = stringResource(id = R.string.statistics_table_title),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth()
        )
        StatisticsRow(
            elementName = stringResource(id = R.string.element_name),
            elementCount = stringResource(id = R.string.element_count),
            massRatio = stringResource(id = R.string.mass_ratio),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            items(
                items = parseResult.statistics,
                key = { it.elementId.value }
            ) {
                StatisticsRow(
                    elementName = it.elementId.elementName,
                    elementCount = it.count.toString(),
                    massRatio = MassRatio.valueOf(parseResult.weight, it).string,
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}