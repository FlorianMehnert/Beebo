package com.fm.beebo.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.fm.beebo.datastore.SearchHistoryManager
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SearchBarTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    searchViewModel: LibrarySearchViewModel,
    searchHistoryManager: SearchHistoryManager
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchHistory by searchHistoryManager.searchHistory.collectAsState(initial = emptyList())

    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    // Filter suggestions based on current query
    val filteredSuggestions = remember(query, searchHistory) {
        if (query.isBlank()) {
            searchHistory.take(5)
        } else {
            searchHistory.filter {
                it.query.contains(query, ignoreCase = true) &&
                        !it.query.equals(query, ignoreCase = true)
            }.take(5)
        }
    }

    // Show suggestions when focused and there are suggestions to show
    LaunchedEffect(isFocused, filteredSuggestions, query) {
        showSuggestions = isFocused && (
                (query.isBlank() && searchHistory.isNotEmpty()) ||
                        (query.isNotBlank() && filteredSuggestions.isNotEmpty())
                )
    }

    // Hide suggestions when focus is lost
    LaunchedEffect(isFocused) {
        if (!isFocused) {
            showSuggestions = false
        }
    }

    // Use Box for proper overlay positioning
    Box {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Suchbegriff") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()  // Make sure it takes full width
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
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
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    searchViewModel.statusMessage = "Warte auf Antwort vom Katalog..."
                    onSearch()
                    isFocused = false
                }
            )
        )

        // Dropdown positioned as overlay
        if (showSuggestions) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp) // Position below text field
                    .zIndex(10f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    items(filteredSuggestions) { historyItem ->
                        SearchHistorySuggestion(
                            historyItem = historyItem,
                            onSuggestionClick = { selectedQuery ->
                                onQueryChange(selectedQuery)
                                keyboardController?.hide()
                                searchViewModel.statusMessage = "Warte auf Antwort vom Katalog..."
                                onSearch()
                                isFocused = false
                                showSuggestions = false
                            }
                        )

                        if (historyItem != filteredSuggestions.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun SearchHistorySuggestion(
    historyItem: com.fm.beebo.datastore.SearchHistoryItem,
    onSuggestionClick: (String) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN) }
    val formattedDate = remember(historyItem.timestamp) {
        dateFormatter.format(Date(historyItem.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSuggestionClick(historyItem.query) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Suchverlauf",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = historyItem.query,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}