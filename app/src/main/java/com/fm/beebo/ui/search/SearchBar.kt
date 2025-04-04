package com.fm.beebo.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.fm.beebo.ui.settings.FilterBy
import com.fm.beebo.ui.settings.FilterOptions
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import com.fm.beebo.viewmodels.SettingsViewModel


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
    val selectedFilterOptions by viewModel.sortBy.collectAsState(initial = Pair(FilterOptions.YEAR, true))
    val selectedYear by viewModel.selectedYear.collectAsState(2025)
    val selectedYearRange by viewModel.selectedYearRange.collectAsState(2020f..2025f)

    // Track selected media types
    val selectedMediaTypes by viewModel.selectedMediaTypes.collectAsState(initial = emptyList())

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
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
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
                            // Year range selection UI
                            Text(
                                text = "Jahr auswählen",
                                fontWeight = FontWeight(900),
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            )

                            val currentYear = 2025
                            var yearRange by remember { mutableStateOf(2020f..currentYear.toFloat()) }

                            // Display selected range
                            Text(
                                text = "Von ${yearRange.start.toInt()} bis ${yearRange.endInclusive.toInt()}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            RangeSlider(
                                value = yearRange,
                                onValueChange = { range -> yearRange = range },
                                valueRange = 2000f..currentYear.toFloat(),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Button(
                                onClick = {
                                    viewModel.setSelectedYearRange(Pair(yearRange.start.toInt(), yearRange.endInclusive.toInt()))
                                    filterExpanded = false
                                },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Anwenden")
                            }
                        }

                        FilterOptions.KIND_OF_MEDIUM -> {
                            // Media type selection UI with checkboxes for multiple selection
                            Text(
                                text = "Medienart",
                                fontWeight = FontWeight(900),
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            )

                            // Get all unique media types from FilterBy enum
                            val allMediaTypes = FilterBy.entries.flatMap { it.getKindOfMedium() }.distinct()
                                .filter { it.isNotEmpty() }

                            allMediaTypes.forEach { mediaType ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.toggleMediaType(mediaType)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedMediaTypes.contains(mediaType),
                                        onCheckedChange = { viewModel.toggleMediaType(mediaType) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = mediaType)
                                }
                            }

                            // Add Apply button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        filterExpanded = false
                                        onSearch()
                                    }
                                ) {
                                    Text("Anwenden")
                                }
                            }
                        }

                        FilterOptions.AVAILABLE -> {
                            // Availability options
                            Text(
                                text = "Verfügbarkeit",
                                fontWeight = FontWeight(900),
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            )

                            listOf("Alle", "Nur verfügbare").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(text = option) },
                                    onClick = {
                                        viewModel.setAvailabilityFilter(option == "Nur verfügbare")
                                        filterExpanded = false
                                    }
                                )
                            }
                        }

                        FilterOptions.DUE_DATE -> {
                            // Due date options
                            Text(
                                text = "Verfügbar bis",
                                fontWeight = FontWeight(900),
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            )

                            listOf("Alle", "Diese Woche", "Dieser Monat").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(text = option) },
                                    onClick = {
                                        viewModel.setDueDateFilter(option)
                                        filterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Modifier.conditional_filterby(selectedOption: String, filterItem: String, modifier: @Composable Modifier.() -> Modifier): Modifier {
    return if (selectedOption == filterItem) {
        this.then(modifier())
    } else {
        this
    }
}

@Composable
fun Modifier.conditional(isConditionMet: Boolean,modifier: @Composable Modifier.() -> Modifier): Modifier {
    return if (isConditionMet) {
        this.then(modifier())
    } else {
        this
    }
}

