package com.lxy.molweightcalculator.ui

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lxy.molweightcalculator.util.getValue
import com.lxy.molweightcalculator.util.mutableBooleanStateOf
import com.lxy.molweightcalculator.util.setValue
import kotlinx.coroutines.flow.emptyFlow


private val ScrollToSelected = Any()
private val NullInteractionSource = object : MutableInteractionSource {
    override val interactions get() = emptyFlow<Interaction>()

    override suspend fun emit(interaction: Interaction) {}

    override fun tryEmit(interaction: Interaction) = false
}

@Stable
class DropDownMenuState() : Parcelable {
    private var selectedIndex by mutableIntStateOf(0)

    constructor(parcel: Parcel) : this() {
        selectedIndex = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(selectedIndex)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DropDownMenuState> {
        override fun createFromParcel(parcel: Parcel): DropDownMenuState {
            return DropDownMenuState(parcel)
        }

        override fun newArray(size: Int): Array<DropDownMenuState?> {
            return arrayOfNulls(size)
        }
    }
}

@Immutable
class DropDownOptions(val value: Array<String>)

// Enhanced dropdown view
// Inspired by https://proandroiddev.com/improving-the-compose-dropdownmenu-88469b1ef34
@Composable
fun DropDownView(
    label: String,
    options: DropDownOptions,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableBooleanStateOf(false) }

    Box(modifier = modifier.height(IntrinsicSize.Min)) {
        OutlinedTextField(
            value = options.value[selectedIndex],
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth(),
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    null,
                    Modifier
                        .rotate(if (expanded) 180f else 0f)
                        .clickable { expanded = true }
                )
            },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clickable(
                    // This box doesn't need to animate when clicked
                    interactionSource = NullInteractionSource,
                    indication = null,
                    enabled = true,
                    onClick = { expanded = true },
                )
        )
    }
    if (expanded) {
        Dialog(onDismissRequest = { expanded = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                val lazyListState = rememberLazyListState()

                if (selectedIndex >= 0) {
                    LaunchedEffect(ScrollToSelected) {
                        lazyListState.animateScrollToItem(index = selectedIndex)
                    }
                }

                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    state = lazyListState
                ) {
                    options.value.forEachIndexed { index, s ->
                        item {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemSelected(index)
                                    expanded = false
                                }) {
                                Text(
                                    text = s,
                                    color = run {
                                        val colorScheme = MaterialTheme.colorScheme
                                        if (index == selectedIndex)
                                            colorScheme.primary
                                        else
                                            colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        if (index < options.value.lastIndex) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.LightGray)
                                        .height(1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}