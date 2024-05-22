package com.lxy.molweightcalculator.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.lxy.molweightcalculator.R
import com.lxy.molweightcalculator.parsing.MassRatio
import com.lxy.molweightcalculator.parsing.ParseResult
import com.lxy.molweightcalculator.util.Utility


class Symbol(private val description: String) {
    override fun toString(): String {
        return "Symbol($description)"
    }
}


private val ElementNameType = Symbol("ElementName")
private val ElementCountType = Symbol("ElementCount")
private val MassRatioType = Symbol("MassRatio")


@Composable
fun StatisticsView(
    parseResult: ParseResult,
    modifier: Modifier = Modifier,
    spacing: Dp,
) {
    @Composable
    fun GridItem(text: String, modifier: Modifier = Modifier) {
        Text(text = text, textAlign = TextAlign.Center, modifier = modifier)
    }

    Column(modifier = modifier) {
        GridItem(
            text = stringResource(id = R.string.statistics_table_title),
            modifier = Modifier.fillMaxWidth()
        )
        Row {
            GridItem(
                text = stringResource(id = R.string.element_name),
                modifier = Modifier.weight(1f)
            )
            GridItem(
                text = stringResource(id = R.string.element_count),
                modifier = Modifier.weight(1f)
            )
            GridItem(
                text = stringResource(id = R.string.mass_ratio),
                modifier = Modifier.weight(1f)
            )
        }
        LazyVerticalGrid(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            columns = GridCells.Fixed(Utility.COLUMN_COUNT),
        ) {
            parseResult.statistics.forEach {
                val grid = this@LazyVerticalGrid
                grid.item(contentType = ElementNameType) {
                    GridItem(text = it.elementId.elementName)
                }
                grid.item(contentType = ElementCountType) {
                    GridItem(text = it.count.toString())
                }
                grid.item(contentType = MassRatioType) {
                    GridItem(text = run {
                        val massRatio = parseResult.calculateMassRatio(it)
                        MassRatio.massRatioString(massRatio)
                    })
                }
            }
        }
    }
}