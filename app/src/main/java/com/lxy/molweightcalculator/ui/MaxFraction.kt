package com.lxy.molweightcalculator.ui

import androidx.annotation.FloatRange
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import com.lxy.molweightcalculator.util.HashCode
import kotlin.math.roundToInt


enum class Direction {
    Horizontal, Vertical, Both
}

// Modified from fillMaxHeight/Width/Size
private class MaxFractionNode(
    var direction: Direction,
    var fraction: Float
) : LayoutModifierNode, Modifier.Node() {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val minConstraintWidth = constraints.minWidth
        val minConstraintHeight = constraints.minHeight
        val maxConstraintWidth = constraints.maxWidth
        val maxConstraintHeight = constraints.maxHeight
        val maxWidth = if (constraints.hasBoundedWidth && direction != Direction.Vertical) {
            (maxConstraintWidth * fraction).roundToInt()
                .coerceIn(minConstraintWidth, maxConstraintWidth)
        } else {
            maxConstraintWidth
        }
        val maxHeight = if (constraints.hasBoundedHeight && direction != Direction.Horizontal) {
            (maxConstraintHeight * fraction).roundToInt()
                .coerceIn(minConstraintHeight, maxConstraintHeight)
        } else {
            maxConstraintHeight
        }
        val placeable = measurable.measure(
            Constraints(minConstraintWidth, maxWidth, minConstraintHeight, maxHeight)
        )
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}

private class MaxFractionElement(
    private val direction: Direction,
    private val fraction: Float
) : ModifierNodeElement<MaxFractionNode>() {

    override fun create(): MaxFractionNode {
        return MaxFractionNode(direction, fraction)
    }

    override fun update(node: MaxFractionNode) {
        node.direction = direction
        node.fraction = fraction
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is MaxFractionElement) {
            return direction == other.direction && fraction == other.fraction
        }
        return false
    }

    override fun hashCode(): Int {
        return HashCode(direction)
            .mix(fraction)
            .build()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = when (direction) {
            Direction.Horizontal -> "maxPercentageWidth"
            Direction.Vertical -> "maxPercentageHeight"
            Direction.Both -> "maxPercentageSize"
        }
        properties["fraction"] = fraction
    }
}

fun Modifier.maxFractionWidth(
    @FloatRange(from = 0.0, to = 1.0)
    fraction: Float
) =
    this.then(MaxFractionElement(Direction.Horizontal, fraction))


fun Modifier.maxFractionHeight(
    @FloatRange(from = 0.0, to = 1.0)
    fraction: Float
) =
    this.then(MaxFractionElement(Direction.Vertical, fraction))


fun Modifier.maxFractionSize(
    @FloatRange(from = 0.0, to = 1.0)
    fraction: Float
) =
    this.then(MaxFractionElement(Direction.Both, fraction))