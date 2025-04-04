package com.fm.beebo.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.ui.settings.FilterBy
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


// Function to calculate Levenshtein distance between two strings
// Lower distance means strings are more similar
fun levenshteinDistance(s1: String, s2: String): Int {
    val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

    for (i in s1.indices) dp[i][0] = i
    for (j in s2.indices) dp[0][j] = j

    for (i in 1..s1.length) {
        for (j in 1..s2.length) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
        }
    }

    return dp[s1.length][s2.length]
}

@Composable
fun SearchResultsList(
    results: List<LibraryMedia>,
    searchQuery: String,
    filter: StateFlow<FilterBy>,
    onItemClick: (LibraryMedia) -> Unit,
    firstTimeStart: Boolean
) {
    // Apply filter
    val filteredResults = if (filter.collectAsState().value == FilterBy.Alles) {
        results
    } else {
        results.filter { filter.value.getKindOfMedium().contains(it.kindOfMedium) }
    }

    // Sort results by similarity to the search query
    val sortedResults = if (searchQuery.isNotEmpty()) {
        filteredResults.sortedBy { item ->
            // Calculate similarity - lower value means closer match
            levenshteinDistance(item.title.lowercase(), searchQuery.lowercase())
        }
    } else {
        filteredResults
    }

    val listState = rememberLazyListState()
    val showButton = remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Box(modifier = Modifier.fillMaxSize()) {
        if (sortedResults.isEmpty() && !firstTimeStart) {
            EmptyResults()
        } else if (sortedResults.isEmpty()) {
            WelcomeScreen()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedResults) { item ->
                    SearchResultItem(
                        text = item.toString(),
                        isAvailable = item.isAvailable,
                        onClick = { onItemClick(item) },
                        kindOfMedium = item.kindOfMedium
                    )
                }
            }
        }
        val coroutineScope = rememberCoroutineScope()
        // FloatingActionButton to scroll to the top
        if (showButton.value) {
            FloatingActionButton(
                onClick = {
                    // Scroll to the top
                    coroutineScope.launch {
                    listState.scrollToItem(0)
                        }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.ArrowUpward, contentDescription = "Scroll to top")
            }
        }
    }
}


@Composable
fun EmptyResults() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.TwoTone.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Keine Suchergebnisse",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Probiere es mit einem anderen Suchbegriff",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun WelcomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.TwoTone.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gib einen Suchbegriff ein und starte die Suche um Suchergebnisse zu sehen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp).width(250.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
