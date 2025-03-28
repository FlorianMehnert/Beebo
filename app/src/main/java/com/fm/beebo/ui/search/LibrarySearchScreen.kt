package com.fm.beebo.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.ui.profile.LoginDialog
import com.fm.beebo.ui.search.details.ItemDetails
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import com.fm.beebo.viewmodels.LoginViewModel
import com.fm.beebo.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySearchScreen(
    onSettingsClick: () -> Unit,
    viewModel: LibrarySearchViewModel = viewModel(),
    settingsViewModel: SettingsViewModel,
    loginViewModel: LoginViewModel = viewModel()
) {
    val settingsDataStore = SettingsDataStore(LocalContext.current)
    val defaultSearchTerm by settingsDataStore.defaultSearchTermFlow.collectAsState(initial = "")
    val enableDefaultSearchTerm by settingsDataStore.enableDefaultSearchTermFlow.collectAsState(
        initial = true
    )
    val maxPagesSetting by settingsDataStore.maxPagesFlow.collectAsState(initial = "")
    var query by remember { mutableStateOf("") }

    if (enableDefaultSearchTerm) {
        LaunchedEffect(defaultSearchTerm) {
            query = defaultSearchTerm
        }
    }


    OutlinedTextField(
        value = query,
        onValueChange = { newQuery -> query = newQuery },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Standard Suchbegriff") },
        singleLine = true,
    )
    var selectedItem by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var showLoginDialog by remember { mutableStateOf(false) }

    // Handle login dialog
    if (showLoginDialog) {
        LoginDialog(
            loginViewModel = loginViewModel, onDismiss = { showLoginDialog = false })
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
                    Row {
                        IconButton(onClick = { showLoginDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Login",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "Einstellungen",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                })
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    viewModel.searchLibrary(
                        query,
                        maxPagesSetting.toString().toIntOrNull() ?: 3,
                        settingsViewModel
                    )
                },
                viewModel = settingsViewModel,
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
                        results = viewModel.results, onItemClick = { item ->
                            selectedItem = Pair(item.title, item.isAvailable)
                            viewModel.fetchItemDetails(item.url, item.isAvailable)
                        }, searchQuery = query
                    )
                }
            }
        }
    }
}