package com.fm.beebo.ui.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchHistoryDropdown(
    searchHistory: List<String>,
    onQuerySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .offset(y = 8.dp) // Offset to appear below the text field
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            searchHistory.take(5).forEachIndexed { index, historyItem ->
                ListItem(
                    headlineContent = { Text(historyItem) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History"
                        )
                    },
                    modifier = Modifier.clickable {
                        onQuerySelected(historyItem)
                    }
                )

                if (index < searchHistory.take(5).size - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}