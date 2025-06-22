package com.fm.beebo.ui.search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.fm.beebo.datastore.SearchHistoryItem
import com.fm.beebo.datastore.SearchHistoryManager
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    searchViewModel: LibrarySearchViewModel,
    searchHistoryManager: SearchHistoryManager
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val searchHistory by searchHistoryManager.searchHistory.collectAsState(emptyList())
    val focusManager = LocalFocusManager.current

    val filtered by remember(query, searchHistory) {
        derivedStateOf {
            if (query.isBlank()) searchHistory.take(10)
            else searchHistory
                .filter { it.query.contains(query, ignoreCase = true) && it.query != query }
                .take(5)
        }
    }


    /* ----------  Focus tracking ---------- */
    var fieldHasFocus by remember { mutableStateOf(false) }


    /* ----------  Menu visibility = focus && listNotEmpty ---------- */
    val showMenu by remember(fieldHasFocus, filtered) {
        derivedStateOf { fieldHasFocus && filtered.isNotEmpty() }
    }


    ExposedDropdownMenuBox(
        expanded = showMenu,
        onExpandedChange = {  }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Suchbegriff") },
            singleLine = true,
            leadingIcon  = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty())
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, "Löschen")
                    }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    performSearch(keyboard, searchViewModel, onSearch) { /* keep focus */ }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { fieldHasFocus = it.isFocused }
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
        )


        ExposedDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { focusManager.clearFocus() },
            modifier = Modifier
                .widthIn(min = 280.dp, max = 400.dp)
                .animateContentSize()
        ) {
            filtered.forEachIndexed { i, item ->
                DropdownMenuItem(
                    text = { SearchHistoryRow(item) },
                    onClick = {
                        onQueryChange(item.query)
                        performSearch(keyboard, searchViewModel, onSearch) { /* keep focus */ }
                    }
                )
                if (i < filtered.lastIndex) Divider()
            }
        }
    }
}

@Composable
private fun SearchHistoryRow(item: SearchHistoryItem) {
    val date = remember(item.timestamp) {
        SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(Date(item.timestamp))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.History, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp))
        Column {
            Text(item.query, style = MaterialTheme.typography.bodyLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(date, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}



// Extract search logic to reduce duplication
private inline fun performSearch(
    keyboard: SoftwareKeyboardController?,
    vm: LibrarySearchViewModel,
    onSearch: () -> Unit,
    after: () -> Unit
) {
    keyboard?.hide()
    vm.statusMessage = "Warte auf Antwort vom Katalog..."
    onSearch()
    after()
}


@Composable
private fun SearchHistorySuggestion(
    historyItem: SearchHistoryItem,
    onSuggestionClick: (String) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN) }
    val formattedDate = remember(historyItem.timestamp) {
        dateFormatter.format(Date(historyItem.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onSuggestionClick(historyItem.query) }
            )
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
