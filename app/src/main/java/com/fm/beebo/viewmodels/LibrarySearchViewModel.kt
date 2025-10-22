package com.fm.beebo.viewmodels

import android.webkit.CookieManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.network.LibrarySearchService
import com.fm.beebo.network.getCookies
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

    private var lastSearchQuery: String = ""
    private var lastSearchMaxPages: Int = 3
    private var lastSettingsViewModel: SettingsViewModel? = null
    private var lastSettingsDataStore: SettingsDataStore? = null
    private var currentSessionCookies: Map<String, String> = emptyMap()

    private var previouslyLoadedItems: Set<String> = emptySet()
    private var isReSearchDueToSessionChange = false

    private var pendingItemFetch: Pair<String, Boolean>? = null
    var currentlyRefetching = false
    fun searchLibrary(query: String, maxPages: Int = 3, settingsViewModel: SettingsViewModel, settingsDataStore: SettingsDataStore) {
        if (query.isBlank()) return


        val cookieManager = CookieManager.getInstance()

        lastSearchQuery = query
        lastSearchMaxPages = maxPages
        lastSettingsViewModel = settingsViewModel
        lastSettingsDataStore = settingsDataStore


        if (!isReSearchDueToSessionChange) {
            previouslyLoadedItems = itemDetailsMap.keys.toSet()
        }


        isLoading = true
        results = emptyList()
        itemDetailsMap = emptyMap()


        viewModelScope.launch {
            settingsDataStore.bulkFetchEnabledFlow.collectLatest { enabled ->
                _bulkFetchEnabled.value = enabled
            }
        }


        if (!isReSearchDueToSessionChange) {
            currentSessionCookies = cookieManager.getCookies()
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


                            // ✅ Re-fetch details for previously loaded items after session change
                            if (isReSearchDueToSessionChange && previouslyLoadedItems.isNotEmpty()) {

                                searchResult.results.forEach { item ->
                                    if (previouslyLoadedItems.contains(item.url)) {
                                        viewModelScope.launch {
                                            fetchItemDetailsInternal(item.url, item.isAvailable)
                                        }
                                    }
                                }

                                previouslyLoadedItems = emptySet()
                            }


                            // ✅ Handle pending item fetch after re-search
                            if (isReSearchDueToSessionChange && pendingItemFetch != null) {
                                val (url, available) = pendingItemFetch!!
                                pendingItemFetch = null

                                viewModelScope.launch {
                                    fetchItemDetailsInternal(url, available)
                                }
                            }


                            // ✅ Reset re-search flag
                            if (isReSearchDueToSessionChange) {
                                isReSearchDueToSessionChange = false
                            }


                            // Normal bulk fetch for new searches
                            if (_bulkFetchEnabled.value && !isReSearchDueToSessionChange) {
                                searchResult.results.forEach { item ->
                                    viewModelScope.launch {
                                        fetchItemDetailsInternal(item.url, item.isAvailable)
                                    }
                                }
                            }


                            if (!searchResult.success ||
                                searchResult.results.size >= searchResult.totalPages * 10 ||
                                searchResult.totalPages <= 1 || searchResult.isComplete) {
                                isLoading = false
                            }
                            if (searchResult.message.contains("under construction", ignoreCase = true)) {
                                isLoading = false
                                return@withContext
                            }
                        }
                    }
                progress = 0f
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusMessage = "Error: ${e.message ?: "Unknown error"}"
                    isLoading = false

                    // ✅ Reset flags on error
                    isReSearchDueToSessionChange = false
                    pendingItemFetch = null
                }
            }
        }
    }

    fun fetchItemDetails(itemUrl: String, available: Boolean) {
        val cookieManager = CookieManager.getInstance()
        val newSessionCookies = cookieManager.getCookies()

        // ✅ Check if session changed
        if (sessionHasChanged(currentSessionCookies, newSessionCookies)) {
            isLoading = true
            // ✅ Store the item fetch request for after re-search
            pendingItemFetch = itemUrl to available

            // Trigger re-search
            statusMessage = "Session geändert - Suchergebnisse werden aktualisiert..."
            isReSearchDueToSessionChange = true
            currentSessionCookies = newSessionCookies


            lastSettingsViewModel?.let { settingsVM ->
                lastSettingsDataStore?.let { dataStore ->
                    searchLibrary(lastSearchQuery, lastSearchMaxPages, settingsVM, dataStore)
                }
            }
            isLoading = false
        } else {
            fetchItemDetailsInternal(itemUrl, available)
        }
    }


    private fun fetchItemDetailsInternal(itemUrl: String, available: Boolean) {
        viewModelScope.launch {
            isLoading = true
            try {
                val itemDetails = librarySearchService.getItemDetails(itemUrl, available)

                itemDetailsMap = itemDetailsMap + (itemUrl to itemDetails)


                results = results.map { item ->
                    if (item.url == itemUrl) {
                        item.copy(
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
                isLoading = false
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
            }
        }
    }

    fun sessionHasChanged(oldCookies: Map<String, String>, newCookies: Map<String, String>): Boolean {
        val importantKeys = setOf("JSESSIONID", "USERSESSIONID", "BaseURL", "APP_CSID")

        return importantKeys.any { key ->
            oldCookies[key] != newCookies[key]
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        isLoading = false
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

    fun fetchItemDetailsForWishlistItem(url: String) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val libraryMedia = librarySearchService.getWishlistItemDetails(url)
                withContext(Dispatchers.Main) {
                    itemDetailsMap = itemDetailsMap + (url to libraryMedia)
                    isLoading = false  // Reset ViewModel's loading state
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }
}

