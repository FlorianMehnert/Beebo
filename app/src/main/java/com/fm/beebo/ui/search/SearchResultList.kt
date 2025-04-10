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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.ui.settings.Media
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate


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
    dueDateFilter: StateFlow<LocalDate?>,
    doYearRangeFiltering: Boolean,
    selectedYearRange: Pair<State<Int>, State<Int>>,
    selectedMediaTypes: StateFlow<List<Media>>,
    onItemClick: (LibraryMedia) -> Unit,
    firstTimeStart: Boolean
) {
    // Get current filters
    val currentDueDateFilter = dueDateFilter.collectAsState().value
    val currentMediaTypes = selectedMediaTypes.collectAsState().value

    // Step 1: Apply specific media types filtering if available
    val filteredByMediaTypes = if (currentMediaTypes.isNotEmpty()) {
        results.filter { media ->
            currentMediaTypes.contains(media.kindOfMedium)
        }
    } else {
        results
    }

    // Step 2: Apply year range filtering
    val filteredByYear = if (doYearRangeFiltering) {
        filteredByMediaTypes.filter { media ->
            try {
                val year = media.year.toInt()
                year in selectedYearRange.first.value..selectedYearRange.second.value
            } catch (e: NumberFormatException) {
                false
            }
        }
    } else {
        filteredByMediaTypes
    }

    // Step 3: Apply due date filter if present
    val filteredResults = if (currentDueDateFilter != null) {
        filteredByYear.filter { media ->
            // Only consider media with due dates that is available
            if (media.dueDates.isEmpty()) {
                false
            } else {
                // Check if any due date is before our filter date
                media.dueDates.any { dueDateStr ->
                    try {
                        // First clean the string to handle cases like "2025 (gesamte Vormerkungen: 0)"
                        val cleanDateStr = dueDateStr.trim()
                            .split(" ")[0] // Take only the first part before any space
                        println(cleanDateStr)

                        // Parse due date string (assuming format is "DD.MM.YYYY")
                        val parts = cleanDateStr.split(".")
                        if (parts.size == 3) {
                            val dueDate = LocalDate.of(
                                parts[2].toInt(),
                                parts[1].toInt(),
                                parts[0].toInt()
                            )
                            // Check if due date is before our filter date
                            dueDate.isBefore(currentDueDateFilter)
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
            }
        }
    } else {
        filteredByYear
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
    val coroutineScope = rememberCoroutineScope()

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
                        item,
                        onClick = { onItemClick(item) },
                    )
                }
            }
        }

        // FloatingActionButton to scroll to the top
        if (showButton.value) {
            FloatingActionButton(
                onClick = {
                    // Smooth scroll to the top
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = CircleShape
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
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .width(250.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
