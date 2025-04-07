package com.fm.beebo.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fm.beebo.ui.settings.FilterBy
import com.fm.beebo.ui.settings.FilterOptions
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import com.fm.beebo.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    viewModel: SettingsViewModel,
    searchViewModel: LibrarySearchViewModel,
) {
    var filterExpanded by remember { mutableStateOf(false) }
    val selectedFilterOption by viewModel.selectedFilterOption.collectAsState()
    val selectedFilterOptions by viewModel.sortBy.collectAsState(
        initial = Pair(
            FilterOptions.YEAR,
            true
        )
    )
    val selectedYear by viewModel.selectedYear.collectAsState(2025)
    val selectedYearRange by viewModel.selectedYearRange.collectAsState()
    val minYear by viewModel.minYear.collectAsState()
    val maxYear by viewModel.maxYear.collectAsState()
    val dueDateFilter by viewModel.dueDateFilter.collectAsState()

    // Track selected media types
    val selectedMediaTypes by viewModel.selectedMediaTypes.collectAsState(initial = emptyList())

    // Date picker state
    var showStartYearPicker by remember { mutableStateOf(false) }
    var showEndYearPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(60.dp)
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Suchbegriff") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Suchen"
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Löschen"
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        searchViewModel.statusMessage = "Warte auf Antwort vom Katalog..."
                        onSearch()
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8),
                modifier = Modifier
                    .height(50.dp)
                    .offset(y = 4.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                IconButton(
                    onClick = { filterExpanded = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                        .width(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false },
                ) {
                    // Filter Options Section (YEAR, KIND_OF_MEDIUM, etc.)
                    Text(
                        text = "Filterart",
                        fontWeight = FontWeight(900),
                        modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 16.dp)
                    )

                    FilterOptions.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option.toString()) },
                            onClick = {
                                viewModel.setSortBy(option, true)
                            },
                            modifier = Modifier.conditional(selectedFilterOptions.first == option) {
                                val background =
                                    background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                background
                            }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )

                    // Dynamic section based on selected filter option
                    when (selectedFilterOptions.first) {
                        FilterOptions.YEAR -> {
                            Text(
                                text = "Jahr auswählen",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )

                            var showMinYearDialog by remember { mutableStateOf(false) }
                            var showMaxYearDialog by remember { mutableStateOf(false) }
                            var showRangeSelector by remember { mutableStateOf(false) }

                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { showMinYearDialog = true }) {
                                        Text(text = "Min: $minYear")
                                    }
                                    TextButton(onClick = { showMaxYearDialog = true }) {
                                        Text(text = "Max: $maxYear")
                                    }
                                }

                                if (maxYear > 2025) {
                                    Text(
                                        "Endjahr darf nicht größer als das aktuelle Jahr sein.",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                val debouncedSelectedYearRange by rememberUpdatedState(selectedYearRange)

                                LaunchedEffect(debouncedSelectedYearRange) {
                                    delay(300) // Wait 300ms before updating
                                    viewModel.setSelectedYearRange(debouncedSelectedYearRange)
                                }

                                RangeSlider(
                                    value = selectedYearRange.first.toFloat()..selectedYearRange.second.toFloat(),
                                    onValueChange = { range ->
                                        viewModel.setSelectedYearRange(
                                            Pair(range.start.toInt(), range.endInclusive.toInt())
                                        )
                                    },
                                    valueRange = minYear.toFloat()..maxYear.toFloat()
                                )


                                Text(
                                    "${selectedYearRange.first} - ${selectedYearRange.second}",
                                    Modifier.align(Alignment.CenterHorizontally).clickable{
                                        showRangeSelector = true
                                    }
                                )

                                Button(
                                    onClick = {
                                        filterExpanded = false
                                        onSearch()
                                    },
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.End)
                                ) {
                                    Text("Anwenden")
                                }
                            }

                            // Min Year Input Dialog
                            if (showMinYearDialog) {
                                var newMinYear by remember { mutableStateOf(minYear.toString()) }
                                AlertDialog(
                                    onDismissRequest = { showMinYearDialog = false },
                                    title = { Text("Min Jahr eingeben") },
                                    text = {
                                        TextField(
                                            value = newMinYear,
                                            onValueChange = { newMinYear = it },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            if (newMinYear.toIntOrNull() in minYear..maxYear) {
                                                viewModel.setMinYear(newMinYear.toIntOrNull()?.coerceAtLeast(minYear) ?: minYear)
                                            }
                                            showMinYearDialog = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showMinYearDialog = false }) {
                                            Text("Abbrechen")
                                        }
                                    }
                                )
                            }

                            // Max Year Input Dialog
                            if (showMaxYearDialog) {
                                var inputMaxYear by remember { mutableStateOf(maxYear.toString()) }
                                AlertDialog(
                                    onDismissRequest = { showMaxYearDialog = false },
                                    title = { Text("Max Jahr eingeben") },
                                    text = {
                                        TextField(
                                            value = inputMaxYear,
                                            onValueChange = { inputMaxYear = it },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            if (inputMaxYear.toIntOrNull() != null && inputMaxYear.toIntOrNull() in minYear..maxYear) {
                                                viewModel.setMaxYear(inputMaxYear.toIntOrNull()?.coerceAtMost(maxYear) ?: maxYear)
                                            }
                                            showMaxYearDialog = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showMaxYearDialog = false }) {
                                            Text("Abbrechen")
                                        }
                                    }
                                )
                            }

                            if (showRangeSelector) {
                                // Initialize with current values
                                var inputMinYear by remember { mutableStateOf(viewModel.selectedYearRange.value.first.toString()) }
                                var inputMaxYear by remember { mutableStateOf(viewModel.selectedYearRange.value.second.toString()) }

                                AlertDialog(
                                    onDismissRequest = { showRangeSelector = false },
                                    title = { Text("Zeitspanne definieren") },
                                    text = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Von:")
                                                Spacer(modifier = Modifier.width(8.dp))
                                                TextField(
                                                    value = inputMinYear,
                                                    onValueChange = { inputMinYear = it },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(0.4f),
                                                    singleLine = true
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Bis:")
                                                Spacer(modifier = Modifier.width(8.dp))
                                                TextField(
                                                    value = inputMaxYear,
                                                    onValueChange = { inputMaxYear = it },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(0.4f),
                                                    singleLine = true
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            val newMinYear = inputMinYear.toIntOrNull()?.coerceIn(minYear, maxYear) ?: minYear
                                            val newMaxYear = inputMaxYear.toIntOrNull()?.coerceIn(minYear, maxYear) ?: maxYear

                                            // Ensure min year is less than or equal to max year
                                            val finalMinYear = minOf(newMinYear, newMaxYear)
                                            val finalMaxYear = maxOf(newMinYear, newMaxYear)

                                            // Update both values
                                            viewModel.setMinYear(finalMinYear)
                                            viewModel.setMaxYear(finalMaxYear)
                                            viewModel.setSelectedYearRange(Pair(finalMinYear, finalMaxYear))

                                            showRangeSelector = false
                                        }) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showRangeSelector = false }) {
                                            Text("Abbrechen")
                                        }
                                    }
                                )
                            }
                        }


                        FilterOptions.KIND_OF_MEDIUM -> {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally // Centers content
                            ) {
                                Text(
                                    text = "Medienart",
                                    fontWeight = FontWeight(900),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // Get all unique media types
                                val allMediaTypes =
                                    FilterBy.entries.flatMap { it.getKindOfMedium() }.distinct()
                                        .filter { it.isNotEmpty() }

                                // **Wrap the FlowRow inside a Box to restrict width**
                                Box(
                                    modifier = Modifier.widthIn(min = 100.dp, max = 200.dp) // Set min/max width
                                ) {
                                    FlowRow(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        allMediaTypes.forEach { mediaType ->
                                            FilterChip(
                                                selected = selectedMediaTypes.contains(mediaType),
                                                onClick = { viewModel.toggleMediaType(mediaType) },
                                                label = { Text(mediaType) },
                                            )
                                        }
                                    }
                                }

                                // Apply Button
                                Button(
                                    onClick = {
                                        filterExpanded = false
                                        onSearch()
                                    },
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("Anwenden")
                                }
                            }
                        }

                        FilterOptions.DUE_DATE -> {
                            // Due date filter options
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Verfügbar bis",
                                    fontWeight = FontWeight(900),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Display selected date or prompt
                                val formattedDate = dueDateFilter?.let {
                                    "${it.dayOfMonth}.${it.monthValue}.${it.year}"
                                } ?: "Kein Datum ausgewählt"

                                // Date display with picker button
                                OutlinedButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(formattedDate)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Datum auswählen"
                                        )
                                    }
                                }

                                // Clear date button (only show if a date is selected)
                                if (dueDateFilter != null) {
                                    TextButton(
                                        onClick = { viewModel.setDueDateFilter(null) }
                                    ) {
                                        Text("Datum zurücksetzen")
                                    }
                                }

                                // Apply Button
                                Button(
                                    onClick = {
                                        filterExpanded = false
                                        onSearch()
                                    },
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("Anwenden")
                                }
                            }

                            // Date picker dialog
                            if (showDatePicker) {
                                val datePickerState = rememberDatePickerState(
                                    initialSelectedDateMillis = dueDateFilter?.toEpochDay()?.times(24 * 60 * 60 * 1000)
                                )

                                DatePickerDialog(
                                    onDismissRequest = { showDatePicker = false },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    val localDate = Instant.ofEpochMilli(millis)
                                                        .atZone(ZoneId.systemDefault())
                                                        .toLocalDate()
                                                    viewModel.setDueDateFilter(localDate)
                                                }
                                                showDatePicker = false
                                            }
                                        ) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showDatePicker = false }
                                        ) {
                                            Text("Abbrechen")
                                        }
                                    }
                                ) {
                                    DatePicker(
                                        state = datePickerState,
                                        headline = { Text(text="Datum auswählen", Modifier.padding(16.dp)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Modifier.conditional(
    isConditionMet: Boolean,
    modifier: @Composable Modifier.() -> Modifier
): Modifier {
    return if (isConditionMet) {
        this.then(modifier())
    } else {
        this
    }
}