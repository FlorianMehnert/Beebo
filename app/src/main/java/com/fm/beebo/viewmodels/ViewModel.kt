package com.fm.beebo.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.network.LibrarySearchService
import kotlinx.coroutines.launch

class LibrarySearchViewModel : ViewModel() {
    var results by mutableStateOf(listOf<LibraryMedia>())
    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf("")
    var selectedItemDetails by mutableStateOf<LibraryMedia?>(null)
    private var cookies = mutableMapOf<String, String>()

    private val librarySearchService = LibrarySearchService()

    fun searchLibrary(query: String, maxPages: Int = 3) {
        if (query.isBlank()) return

        isLoading = true
        statusMessage = "Searching..."
        results = emptyList()

        viewModelScope.launch {
            try {
                val (searchResults, newCookies) = librarySearchService.search(query, maxPages)
                results = searchResults
                setCookies(newCookies)  // Store cookies
                statusMessage = if (searchResults.isEmpty()) "No results found" else "Found ${searchResults.size} items"
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }


    fun fetchItemDetails(itemUrl: String) {
        isLoading = true
        statusMessage = "Fetching item details..."

        viewModelScope.launch {
            try {
                val itemDetails = librarySearchService.getItemDetails(itemUrl, cookies)
                selectedItemDetails = itemDetails
                statusMessage = "Item details fetched"
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Function to set cookies after initial search
    fun setCookies(newCookies: Map<String, String>) {
        cookies.putAll(newCookies)
    }
}

