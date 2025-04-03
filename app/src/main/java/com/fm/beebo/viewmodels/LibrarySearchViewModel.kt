package com.fm.beebo.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.network.LibrarySearchService
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LibrarySearchViewModel : ViewModel() {
    var results by mutableStateOf(listOf<LibraryMedia>())
    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf("")
    var selectedItemDetails by mutableStateOf<LibraryMedia?>(null)
    var progress by mutableStateOf(0f)
    var totalPages by mutableStateOf(0)


    private val librarySearchService = LibrarySearchService()

    fun searchLibrary(query: String, maxPages: Int = 3, settingsViewModel: SettingsViewModel) {
        if (query.isBlank()) return
        println("max pages is:$maxPages")

        isLoading = true
        results = emptyList()

        viewModelScope.launch {
            try {
                // The flow must collect on a background dispatcher to avoid NetworkOnMainThreadException
                librarySearchService.searchWithFlow(query, maxPages, settingsViewModel)
                    .flowOn(Dispatchers.IO) // This ensures all upstream flow operations run on the IO dispatcher
                    .collect { searchResult ->
                        withContext(Dispatchers.Main) {
                            // Update UI with each emission from the Flow
                            results = searchResult.results

                            statusMessage = searchResult.message
                            progress = searchResult.progress
                            totalPages = searchResult.totalPages

                            // Only set isLoading to false after we've received the final page
                            // or if there was an error
                            if (!searchResult.success ||
                                searchResult.results.size >= searchResult.totalPages * 10 ||
                                searchResult.totalPages <= 1 || searchResult.isComplete) {
                                isLoading = false
                            }
                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusMessage = "Error: ${e.message ?: "Unknown error"}"
                    isLoading = false
                }
            }
        }
    }


    fun fetchItemDetails(itemUrl: String, available: Boolean) {
        isLoading = true
        viewModelScope.launch {
            try {
                val itemDetails = librarySearchService.getItemDetails(
                    itemUrl,
                    available
                )
                selectedItemDetails = itemDetails
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
