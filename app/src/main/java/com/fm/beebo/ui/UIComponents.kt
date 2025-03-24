package com.fm.beebo.ui


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import com.fm.beebo.viewmodels.LoginViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySearchScreen(
    viewModel: LibrarySearchViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel()
) {
    var query by remember { mutableStateOf("fight club") }
    var pages by remember { mutableStateOf("3") }
    var selectedItem by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var showLoginDialog by remember { mutableStateOf(false) }

    // Handle login dialog
    if (showLoginDialog) {
        LoginDialog(
            loginViewModel = loginViewModel,
            onDismiss = { showLoginDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Bibo Dresden") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Login button in the top bar
                    IconButton(onClick = { showLoginDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Login",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
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
                    ItemDetails(viewModel) {
                        selectedItem = null
                    }
                } else {
                    SearchResultsList(
                        results = viewModel.results,
                        onItemClick = { item ->
                            selectedItem = Pair(item.title, item.isAvailable)
                            viewModel.fetchItemDetails(item.url, item.isAvailable)
                        },
                        searchQuery = query
                    )
                }
            }
        }
    }
}

@Composable
fun LoginDialog(
    loginViewModel: LoginViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxHeight(.4f)
                .padding(16.dp)
                .wrapContentHeight(), // Only take needed height,
            shape = RoundedCornerShape(4.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Anmeldung",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Embed your login screen content here
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        // Close dialog after successful login
                        onDismiss()
                    },
                )
            }
        }
    }
}


@Composable
fun LibraryResultListItem(text: String, isAvailable: Boolean, onClick: () -> Unit) {
    val parts = text.split(" ", limit = 4)
    val year = if (parts.isNotEmpty()) parts.getOrNull(0) ?: "" else ""
    val medium = if (parts.size > 1) parts.getOrNull(1) ?: "" else ""
    var title = if (parts.size > 2) parts.drop(2).joinToString(" ") else ""

    // Split title and availability info
    val availabilityText = if (title.contains(" ausleihbar")) " ausleihbar" else " nicht_ausleihbar "
    title = title.replace(availabilityText, "").replace("¬", "")

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
                    .background(if (isAvailable) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surface),
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
        }
    }
}


@Composable
fun ItemDetails(viewModel: LibrarySearchViewModel, onBack: () -> Unit) {
    val itemDetails = viewModel.selectedItemDetails

    // Handle the system back button press
    BackHandler {
        onBack()
    }

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
            if (itemDetails != null) {
                Text(
                    text = itemDetails.title,
                    style = MaterialTheme.typography.titleLarge
                )
            }else {
                Text(
                    text = "Katalog",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        if (itemDetails != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Year: ${itemDetails.year}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (itemDetails.author.isNotEmpty()){
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Autor: ${itemDetails.author}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Verfügbarkeit: ${if (itemDetails.isAvailable) "Ausleihbar" else "Nicht verfügbar"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ISBN: ${itemDetails.isbn}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sprache: ${itemDetails.language}",
                    style = MaterialTheme.typography.bodyMedium
                )

            if (!itemDetails.isAvailable && itemDetails.dueDates.isNotEmpty()) {
                Text(
                    text = "Due Date: ${itemDetails.dueDates[0]}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Text(
                text = "Keine Details verfügbar"
            )
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
                label = { Text("Suche im Online-Katalog") },
                placeholder = { Text("Gib einen Suchbegriff ein...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Suchen"
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
                    label = { Text("Maximale Seitenanzahl") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Seiten"
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSearch,
                    modifier = Modifier
                        .height(60.dp)
                        .padding(top = 4.dp)
                        .offset(y = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Suchen")
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
fun SearchResultsList(
    results: List<LibraryMedia>,
    searchQuery: String,
    onItemClick: (LibraryMedia) -> Unit
) {
    // Sort results by similarity to the search query
    val sortedResults = if (searchQuery.isNotEmpty()) {
        results.sortedBy { item ->
            // Calculate similarity - lower value means closer match
            levenshteinDistance(item.title.lowercase(), searchQuery.lowercase())
        }
    } else {
        results
    }

    println(sortedResults)

    if (sortedResults.isEmpty()) {
        EmptyResults()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedResults) { item ->
                LibraryResultListItem(
                    text = item.toString(),
                    isAvailable = item.isAvailable,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

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
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                viewModel.username = username
                viewModel.password = password
                viewModel.login()
            },
                shape = RoundedCornerShape(4.dp)) {
                Text("Login")
            }
        }

        viewModel.errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }

        if (viewModel.isLoggedIn) {
            LaunchedEffect(Unit) {
                onLoginSuccess()
            }
        }
    }
}

