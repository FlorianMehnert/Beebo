package com.fm.beebo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.viewmodels.LibrarySearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySearchScreen(viewModel: LibrarySearchViewModel = viewModel()) {
    var query by remember { mutableStateOf("fight club") }
    var pages by remember { mutableStateOf("3") }
    var selectedItem by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library Search") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                pages = pages,
                onPagesChange = { pages = it },
                onSearch = { viewModel.searchLibrary(query, pages.toIntOrNull() ?: 3) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchStatus(
                isLoading = viewModel.isLoading,
                statusMessage = viewModel.statusMessage,
                resultCount = viewModel.results.size
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (selectedItem != null) {
                    LibraryItemDetailScreen(viewModel) {
                        selectedItem = null
                        println("is not null")
                    }
                } else {
                    println("is null")
                    SearchResults(
                        results = viewModel.results,
                        onItemClick = { item ->
                            selectedItem = Pair(item.title, item.isAvailable)
                            viewModel.fetchItemDetails(item.url) // Pass empty cookies for now
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedLibraryItemCard(text: String, isAvailable: Boolean, onClick: () -> Unit) {
    val parts = text.split(" ", limit = 4)
    val year = if (parts.isNotEmpty()) parts.getOrNull(0) ?: "" else ""
    val medium = if (parts.size > 1) parts.getOrNull(1) ?: "" else ""
    var title = if (parts.size > 2) parts.drop(2).joinToString(" ") else ""

    // Split title and availability info
    val availabilityText = if (title.contains(" ausleihbar")) " ausleihbar" else " nicht_ausleihbar "
    title = title.replace(availabilityText, "")

    // Extract due date if present
    val dueDate = if (!isAvailable && title.contains(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
        val regex = Regex("(\\d{2}\\.\\d{2}\\.\\d{4})")
        val match = regex.find(title)
        match?.value ?: ""
    } else ""

    // Clean up title if it contains due date
    if (dueDate.isNotEmpty()) {
        title = title.replace(dueDate, "").trim()
    }

    // Determine what to display in the medium icon
    val displayMedium = medium.ifEmpty { "?" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medium icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayMedium,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Only show the year if it's not empty
                    if (year.isNotEmpty()) {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No year found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (dueDate.isNotEmpty()) {
                        // Add spacer only if year is shown
                        if (year.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = buildAnnotatedString {
                                append("Due: ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(dueDate)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Availability indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAvailable) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (isAvailable) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isAvailable) "Available" else "Not Available",
                    tint = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


@Composable
fun LibraryItemDetailScreen(viewModel: LibrarySearchViewModel, onBack: () -> Unit) {
    val itemDetails = viewModel.selectedItemDetails

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Item Details",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (itemDetails != null) {
            Text(
                text = itemDetails.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        if (itemDetails != null) {
            Text(
                text = itemDetails.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Year: ${itemDetails.year}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Authors: ${itemDetails.authors.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Description: ${itemDetails.description}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Additional Info: ${itemDetails.additionalInfo}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Availability: ${if (itemDetails.isAvailable) "Available" else "Not Available"}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (!itemDetails.isAvailable && itemDetails.dueDates.isNotEmpty()) {
                Text(
                    text = "Due Date: ${itemDetails.dueDates[0]}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            CircularProgressIndicator()
        }
    }
}




@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    pages: String,
    onPagesChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search Library") },
                placeholder = { Text("Enter book/movie title, author...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = pages,
                    onValueChange = { value ->
                        // Only allow numbers
                        if (value.isEmpty() || value.all { it.isDigit() }) {
                            onPagesChange(value)
                        }
                    },
                    label = { Text("Pages") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Pages"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSearch,
                    modifier = Modifier
                        .height(60.dp)
                        .padding(top=4.dp)
                        .offset(y=2.dp),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Search")
                }
            }
        }
    }
}

@Composable
fun SearchStatus(
    isLoading: Boolean,
    statusMessage: String,
    resultCount: Int
) {
    if (statusMessage.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val icon = when {
                isLoading -> Icons.Default.Refresh
                statusMessage.contains("Error") -> Icons.Default.Warning
                resultCount > 0 -> Icons.Default.CheckCircle
                else -> Icons.Default.Info
            }

            val color = when {
                isLoading -> MaterialTheme.colorScheme.tertiary
                statusMessage.contains("Error") -> MaterialTheme.colorScheme.error
                resultCount > 0 -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.secondary
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = statusMessage,
                color = color
            )
        }
    }
}

@Composable
fun SearchResults(results: List<LibraryMedia>, onItemClick: (LibraryMedia) -> Unit) {
    if (results.isEmpty()) {
        EmptyResults()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { item ->
                EnhancedLibraryItemCard(
                    text = item.toString(),
                    isAvailable = item.isAvailable,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}




@Composable
fun EmptyResults() {
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
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No search results",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try a different search term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
