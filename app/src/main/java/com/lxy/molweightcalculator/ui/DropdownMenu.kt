package com.lxy.molweightcalculator.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownView(
    label: String,
    options: Array<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    var selectedOption by rememberSaveable {
        mutableStateOf(options[0])
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
        content = {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                label = { Text(text = label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true),
                onDismissRequest = { expanded = false }, content = {
                    options.forEachIndexed { index, s ->
                        DropdownMenuItem(text = {
                            Text(
                                text = s,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }, onClick = {
                            onItemSelected(index)
                            selectedOption = s
                            expanded = false
                        })
                    }
                })
        }
    )
}