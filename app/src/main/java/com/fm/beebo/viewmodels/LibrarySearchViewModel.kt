package com.fm.beebo.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.network.LibrarySearchService
import com.fm.beebo.ui.settings.Media
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LibrarySearchViewModel() : ViewModel() {
    var results by mutableStateOf(listOf<LibraryMedia>())
    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf("")
    var itemDetailsMap by mutableStateOf(mapOf<String, LibraryMedia>())
    var progress by mutableStateOf(0f)
    var totalPages by mutableStateOf(0)
    
    private val librarySearchService = LibrarySearchService()
    private val _bulkFetchEnabled = MutableStateFlow(false)
    private var searchJob: Job? = null
    fun searchLibrary(query: String, maxPages: Int = 3, settingsViewModel: SettingsViewModel, settingsDataStore: SettingsDataStore) {
        if (query.isBlank()) return
        isLoading = true
        results = emptyList()
        itemDetailsMap = emptyMap()
        viewModelScope.launch {
            settingsDataStore.bulkFetchEnabledFlow.collectLatest { enabled ->
                _bulkFetchEnabled.value = enabled
            }
        }
        searchJob = viewModelScope.launch {
            try {
                librarySearchService.searchWithFlow(query, maxPages, settingsViewModel)
                    .flowOn(Dispatchers.IO)
                    .collect { searchResult ->
                        withContext(Dispatchers.Main) {
                            results = searchResult.results
                            statusMessage = searchResult.message
                            progress = searchResult.progress
                            totalPages = searchResult.totalPages
                            if (_bulkFetchEnabled.value){
                                println("bulkfetch is enabled")
                                searchResult.results.forEach { item ->
                                    viewModelScope.launch {
                                        fetchItemDetails(item.url, item.isAvailable)
                                    }
                                }
                            }

                            if (!searchResult.success ||
                                searchResult.results.size >= searchResult.totalPages * 10 ||
                                searchResult.totalPages <= 1 || searchResult.isComplete) {
                                isLoading = false
                            }
                        }
                    }
                progress = 0f
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusMessage = "Error: ${e.message ?: "Unknown error"}"
                    isLoading = false
                }
            }
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        isLoading = false
    }

    fun fetchItemDetails(itemUrl: String, available: Boolean) {
        viewModelScope.launch {
            try {
                val itemDetails = librarySearchService.getItemDetails(itemUrl, available)
                // Update the map with the fetched item details
                itemDetailsMap = itemDetailsMap + (itemUrl to itemDetails)

                // Update the results list with the new item details
                results = results.map { item ->
                    if (item.url == itemUrl) {
                        item.copy(
                            // Update the fields you want to refresh
                            title = itemDetails.title,
                            author = itemDetails.author,
                            year = itemDetails.year,
                            isbn = itemDetails.isbn,
                            language = itemDetails.language,
                            isAvailable = itemDetails.isAvailable,
                            dueDates = itemDetails.dueDates,
                            availableLibraries = itemDetails.availableLibraries,
                            unavailableLibraries = itemDetails.unavailableLibraries,
                            orderableLibraries = itemDetails.orderableLibraries
                        )
                    } else {
                        item
                    }
                }
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
            }
        }
    }

    fun getCountOfMedium(kindOfMedium: Media): Int {
        var count = 0
        results.forEach {
            if (it.kindOfMedium == kindOfMedium) {
                count++
            }
        }
        return count
    }
}

