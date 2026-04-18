package com.mosait.ems.core.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> EmsChipGroup(
    items: List<T>,
    selectedItems: List<T>,
    onSelectionChanged: (List<T>) -> Unit,
    labelSelector: (T) -> String,
    modifier: Modifier = Modifier,
    singleSelection: Boolean = false
) {
    FlowRow(modifier = modifier) {
        items.forEach { item ->
            val isSelected = item in selectedItems
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = if (singleSelection) {
                        if (isSelected) emptyList() else listOf(item)
                    } else {
                        if (isSelected) {
                            selectedItems - item
                        } else {
                            selectedItems + item
                        }
                    }
                    onSelectionChanged(newSelection)
                },
                label = { Text(labelSelector(item)) },
                modifier = Modifier.padding(end = 8.dp, bottom = 4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmsStringChipGroup(
    items: List<String>,
    selectedItems: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    singleSelection: Boolean = false
) {
    EmsChipGroup(
        items = items,
        selectedItems = selectedItems,
        onSelectionChanged = onSelectionChanged,
        labelSelector = { it },
        modifier = modifier,
        singleSelection = singleSelection
    )
}
