package com.lxy.molweightcalculator.ui

import android.os.Parcel
import android.os.Parcelable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lxy.molweightcalculator.util.readBool
import com.lxy.molweightcalculator.util.writeBool
import kotlinx.coroutines.flow.emptyFlow


private val ScrollToSelected = Any()
private val NullInteractionSource = object : MutableInteractionSource {
    override val interactions get() = emptyFlow<Interaction>()

    override suspend fun emit(interaction: Interaction) {}

    override fun tryEmit(interaction: Interaction) = false
}

@Stable
class DropDownMenuState() : Parcelable {
    var expanded by mutableStateOf(false)
    var selectedIndex by mutableIntStateOf(0)

    constructor(parcel: Parcel) : this() {
        expanded = parcel.readBool()
        selectedIndex = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeBool(expanded)
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

// Enhanced dropdown view
// Inspired by https://proandroiddev.com/improving-the-compose-dropdownmenu-88469b1ef34
@Composable
fun DropDownView(
    label: String,
    options: Array<String>,
    state: DropDownMenuState,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.height(IntrinsicSize.Min)) {
        OutlinedTextField(
            value = options[state.selectedIndex],
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
                        .rotate(if (state.expanded) 180f else 0f)
                        .clickable { state.expanded = true }
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
                    onClick = { state.expanded = true },
                )
        )
    }
    if (state.expanded) {
        Dialog(onDismissRequest = { state.expanded = false }) {
            Surface(shape = RoundedCornerShape(12.dp)) {
                val lazyListState = rememberLazyListState()

                if (state.selectedIndex >= 0) {
                    LaunchedEffect(ScrollToSelected) {
                        lazyListState.animateScrollToItem(index = state.selectedIndex)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = lazyListState
                ) {
                    options.forEachIndexed { index, s ->
                        item {
                            Text(
                                text = s,
                                color = run {
                                    val colorScheme = MaterialTheme.colorScheme
                                    if (index == state.selectedIndex)
                                        colorScheme.primary
                                    else
                                        colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .clickable {
                                        onItemSelected(index)
                                        state.expanded = false
                                        state.selectedIndex = index
                                    }
                            )
                            if (index < options.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth()
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