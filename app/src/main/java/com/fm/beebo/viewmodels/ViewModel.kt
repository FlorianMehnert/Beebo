package com.fm.beebo.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.network.LibrarySearchService
import com.fm.beebo.network.LoginService
import kotlinx.coroutines.launch

class SessionRepository private constructor() {
    private val _cookies = mutableMapOf<String, String>()

    val cookies: Map<String, String>
        get() = _cookies

    fun updateCookies(newCookies: Map<String, String>) {
        _cookies.putAll(newCookies)
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionRepository? = null

        fun getInstance(): SessionRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionRepository().also { INSTANCE = it }
            }
    }
}

class LibrarySearchViewModel : ViewModel() {
    var results by mutableStateOf(listOf<LibraryMedia>())
    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf("")
    var selectedItemDetails by mutableStateOf<LibraryMedia?>(null)


    private val librarySearchService = LibrarySearchService()

    fun searchLibrary(query: String, maxPages: Int = 3) {
        if (query.isBlank()) return

        isLoading = true
        statusMessage = "Suche..."
        results = emptyList()

        viewModelScope.launch {
            try {
                val (searchResults, newCookies) = librarySearchService.search(query, maxPages)
                results = searchResults
                SessionRepository.getInstance()
                    .updateCookies(newCookies)
                statusMessage = if (searchResults.isEmpty()) "Keine Ergebnisse gefunden" else "${searchResults.size} Treffer"
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
                val itemDetails = librarySearchService.getItemDetails(itemUrl, SessionRepository.getInstance()
                    .cookies, available)
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


class LoginViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val loginService = LoginService()

    fun login() {
        viewModelScope.launch { // will be canceled if the ViewModel is cleared

            // Assume the server returns session cookies after login

            // Update repository with new session cookies
            var newCookies = loginService.login(username, password)
            if (newCookies != null){
                SessionRepository.getInstance()
                    .updateCookies(newCookies)
                isLoggedIn = true
            }
        }
    }

}


