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
        println("max pages is:$maxPages")

        isLoading = true
        statusMessage = "Suche..."
        results = emptyList()

        viewModelScope.launch {
            try {
                val (searchResultInfo, _) = librarySearchService.search(query, maxPages, settingsViewModel)
                val (searchResults, totalPages) = searchResultInfo
                results = searchResults
                statusMessage =
                    if (searchResults.isEmpty())
                        "Keine Ergebnisse gefunden"
                    else if (totalPages > 1 && (totalPages * 10 - searchResults.size) >= 10)
                        "${searchResults.size} Treffer von ungefähr ${totalPages*10 } Treffern."
                    else
                        "${searchResults.size} Treffer"
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
                    itemUrl,
                    available
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
