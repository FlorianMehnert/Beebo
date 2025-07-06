package com.fm.beebo.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.ui.components.AppBottomNavigation
import com.fm.beebo.ui.search.details.ItemDetailsScreen
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import com.fm.beebo.viewmodels.SettingsViewModel
import com.fm.beebo.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSettingsClick: () -> Unit,
    viewModel: LibrarySearchViewModel = viewModel(),
    settingsViewModel: SettingsViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {
    val settingsDataStore = SettingsDataStore(LocalContext.current)
    val defaultSearchTerm by settingsDataStore.defaultSearchTermFlow.collectAsState(initial = "")
    val enableDefaultSearchTerm by settingsDataStore.enableDefaultSearchTermFlow.collectAsState(
        initial = true
    )
    val maxPagesSetting by settingsDataStore.maxPagesFlow.collectAsState(initial = "")
    var query by remember { mutableStateOf("") }
    val switchToBottomNavigationFlow by settingsDataStore.switchToBottomNavigationFlow.collectAsState(initial=false)


    if (enableDefaultSearchTerm) {
        LaunchedEffect(defaultSearchTerm) {
            query = defaultSearchTerm
        }
    }

    var selectedItem by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var selectedItemUrl by remember { mutableStateOf("") }

    // Handle the ItemDetails screen as a completely separate UI state
    if (selectedItem != null) {
        ItemDetailsScreen(
            viewModel = viewModel,
            onBack = { selectedItem = null },
            selectedItemUrl = selectedItemUrl
        )
        return
    }

    // Main search screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Bibo Dresden") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (!switchToBottomNavigationFlow) {
                        Row {
                            IconButton(onClick = { navController.navigate("profile") }) {
                                Icon(
                                    if (userViewModel.isLoggedIn) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle,
                                    contentDescription = "Account",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            IconButton(onClick = onSettingsClick) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "Einstellungen",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                })
        },
        bottomBar = {
            if (switchToBottomNavigationFlow){
            AppBottomNavigation(
                navController = navController,
                userViewModel = userViewModel,
                currentRoute = "librarySearch"
            )}
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    viewModel.searchLibrary(
                        query,
                        maxPagesSetting.toString().toIntOrNull() ?: 3,
                        settingsViewModel,
                        settingsDataStore
                    )
                },
                viewModel = settingsViewModel,
                searchViewModel = viewModel
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status text
            SearchStatus(
                isLoading = viewModel.isLoading,
                totalResults = viewModel.totalPages * 10,
                progress = viewModel.progress,
                resultCount = viewModel.results.size,
                message = viewModel.statusMessage,
                onCancelSearch = {
                    // Implement the logic to cancel the search
                    viewModel.cancelSearch()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List showing search results
            Box(modifier = Modifier.fillMaxSize()) {
                SearchResultsList(
                    results = viewModel.results,
                    onItemClick = { item ->
                        selectedItem = Pair(item.title, item.isAvailable)
                        selectedItemUrl = item.url
                        viewModel.fetchItemDetails(item.url, item.isAvailable)
                    },
                    searchQuery = query,
                    firstTimeStart = settingsViewModel.appStart.collectAsState().value,
                    dueDateFilter = settingsViewModel.dueDateFilter,
                    doYearRangeFiltering = settingsViewModel.filterByTimeSpan.collectAsState().value,
                    selectedYearRange = Pair(settingsViewModel.minYear.collectAsState(), settingsViewModel.maxYear.collectAsState()),
                    selectedMediaTypes = settingsViewModel.selectedMediaTypes
                )
            }
        }
    }
}