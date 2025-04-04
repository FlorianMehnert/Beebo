package com.fm.beebo.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
    val kindOfMediumList = viewModel.kindOfMediumList
    val selectedYear by viewModel.selectedYear.collectAsState(2025)
    val sortBy =
        FilterOptions.entries.toTypedArray() // Define the range of years you want to filter by

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
                    Text(
                        text = "Medienart",
                        fontWeight = FontWeight(900),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                    kindOfMediumList.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                viewModel.setSelectedFilterOption(kindOfMediumList.indexOf(option))
                                filterExpanded = false
                            },
                            modifier = Modifier.conditional(selectedFilterOption.toString(), option) {
                                background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            }
                        )
                    }

                    Divider()

                    Text(
                        text = "Filterart",
                        fontWeight = FontWeight(900),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    )
                    sortBy.forEach { option: FilterOptions ->
                        DropdownMenuItem(
                            text = { Text(text = option.toString()) },
                            onClick = {
                                viewModel.setSortBy(option, true)
                                filterExpanded = false
                            },
                            modifier = Modifier.conditional(selectedYear.toString(), option.toString()) {
                                background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun Modifier.conditional(selectedOption: String, filterItem: String, modifier: @Composable Modifier.() -> Modifier): Modifier {
    return if (selectedOption == filterItem) {
        this.then(modifier())
    } else {
        this
    }
}

