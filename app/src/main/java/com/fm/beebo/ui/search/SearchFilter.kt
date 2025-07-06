package com.fm.beebo.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fm.beebo.ui.components.HoldableFilterButton
import com.fm.beebo.ui.settings.FilterOptions
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import com.fm.beebo.viewmodels.SettingsViewModel

@Composable
fun SearchFilter(
    viewModel: SettingsViewModel,
    searchViewModel: LibrarySearchViewModel,
    filterExpanded: Boolean,
    onFilterExpandedChange: (Boolean) -> Unit,
    isHolding: Boolean,
    onHoldingChanged: (Boolean) -> Unit,
    progress: Float,
    onSearch: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedFilterOptions by viewModel.sortBy.collectAsState(
        initial = Pair(
            FilterOptions.YEAR,
            true
        )
    )
    val minYear by viewModel.minYear.collectAsState()
    val maxYear by viewModel.maxYear.collectAsState()
    val dueDateFilter by viewModel.dueDateFilter.collectAsState()
    val filterByTimeSpan by viewModel.filterByTimeSpan.collectAsState()
    val selectedMediaTypes by viewModel.selectedMediaTypes.collectAsState(initial = emptyList())

    Surface(
        modifier = Modifier
            .height(50.dp)
            .offset(y = 4.dp)
            .clip(RoundedCornerShape(8)),
        shape = RoundedCornerShape(8),
    ) {
        Surface(
            shape = RoundedCornerShape(8),
            color = if (viewModel.hasFilters.collectAsState().value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
            border = BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        ) {
            HoldableFilterButton(
                onClick = { onFilterExpandedChange(true) },
                isHolding = isHolding,
                onHoldingChanged = onHoldingChanged,
                progress = progress
            )
            DropdownMenu(
                expanded = filterExpanded,
                onDismissRequest = { onFilterExpandedChange(false) },
            ) {
                FilterOptionsSection(
                    viewModel = viewModel,
                    selectedFilterOptions = selectedFilterOptions
                )
                when (selectedFilterOptions.first) {
                    FilterOptions.YEAR -> FilterYear(
                        viewModel,
                        minYear,
                        maxYear,
                        filterByTimeSpan,
                        onFilterExpandedChange,
                        onSearch
                    )
                    FilterOptions.KIND_OF_MEDIUM -> FilterKindOfMedium(
                        viewModel,
                        searchViewModel,
                        selectedMediaTypes,
                        onFilterExpandedChange,
                        onSearch
                    )
                    FilterOptions.DUE_DATE -> FilterDueDate(
                        viewModel,
                        dueDateFilter,
                        showDatePicker,
                        onShowDatePickerChange = { showDatePicker = it },
                        onFilterExpandedChange,
                        onSearch
                    )
                }
            }
        }
    }
}
