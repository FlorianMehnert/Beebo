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


    private val librarySearchService = LibrarySearchService()

    fun searchLibrary(query: String, maxPages: Int = 3, settingsViewModel: SettingsViewModel) {
        if (query.isBlank()) return
        println("max pages is:" + maxPages)

        isLoading = true
        statusMessage = "Suche..."
        results = emptyList()

        viewModelScope.launch {
            try {
                val (searchResults, newCookies) = librarySearchService.search(query, maxPages, settingsViewModel)
                results = searchResults
                SessionRepository.getInstance()
                    .updateCookies(newCookies)
                statusMessage =
                    if (searchResults.isEmpty()) "Keine Ergebnisse gefunden" else "${searchResults.size} Treffer"
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }


    fun fetchItemDetails(itemUrl: String, available: Boolean) {
        isLoading = true
        statusMessage = "Aktualisiere Details..."

        viewModelScope.launch {
            try {
                val itemDetails = librarySearchService.getItemDetails(
                    itemUrl, SessionRepository.getInstance()
                        .cookies, available
                )
                selectedItemDetails = itemDetails
                statusMessage = "Details aktualisiert"
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
