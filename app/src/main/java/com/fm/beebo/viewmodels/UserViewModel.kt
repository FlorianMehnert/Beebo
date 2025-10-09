package com.fm.beebo.viewmodels

import android.content.Context
import android.webkit.CookieManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.network.LoginService
import com.fm.beebo.network.NetworkConfig
import com.fm.beebo.network.getCookies
import com.fm.beebo.network.setCookies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class UserViewModel : ViewModel() {
    private val _wishList = MutableStateFlow<Set<String>>(emptySet())

    var isLoggedIn by mutableStateOf(false)
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var accountFees by mutableStateOf<String?>(null)
    val wishList: StateFlow<Set<String>> = _wishList

    private val loginService = LoginService()

    fun login() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val loginSuccess = loginService.login(username, password)
                if (loginSuccess) {
                    isLoggedIn = true
                    fetchAccountDetails()
                    // Fetch wishlist after login
                    fetchWishlist()
                } else {
                    errorMessage = "Login fehlgeschlagen"
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Login: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchAccountDetails() {
        viewModelScope.launch {
            try {
                val fees = loginService.fetchAccountFees()
                accountFees = fees
            } catch (e: Exception) {
                errorMessage = "Fehler beim Abrufen der Kontodaten: ${e.message}"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            loginService.logout()
            isLoggedIn = false
            accountFees = null
            _wishList.value = emptySet()
        }
    }

    fun toggleWishlistItem(item: LibraryMedia) {
        viewModelScope.launch {
            print("toggle wishlist item")
            try {
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookies()

                errorMessage = "in toggle wishlist item"

                // Extract the item position from the URL
                val urlPattern = "curPos=(\\d+)".toRegex()
                val posMatch = urlPattern.find(item.url)
                val curPos = posMatch?.groupValues?.get(1) ?: return@launch

                // Extract identifier from URL
                val identifierPattern = "identifier=([^&]+)".toRegex()
                val identifierMatch = identifierPattern.find(item.url)
                val identifier = identifierMatch?.groupValues?.get(1) ?: return@launch

                // Build the wishlist URL
                val wishlistUrl =
                    "${NetworkConfig.BASE_LOGGED_IN_URL}/webOPACClient/memorizeHitList.do?" +
                            "methodToCall=addToMemorizeList&curPos=$curPos&forward=hitlist&identifier=$identifier"
                print(wishlistUrl)

                // Make the request to add/remove from wishlist
                val response = Jsoup.connect(wishlistUrl)
                    .cookies(cookies)
                    .timeout(30000)
                    .execute()

                // Update local wishlist state
                val itemId = "${item.title}|${item.year}|${item.kindOfMedium.name}"
                if (_wishList.value.contains(itemId)) {
                    _wishList.value = _wishList.value - itemId
                } else {
                    _wishList.value = _wishList.value + itemId
                }

            } catch (e: Exception) {
                errorMessage = "Fehler beim Aktualisieren der Merkliste: ${e.message}"
            }
        }
    }

    fun toggleWishlistUsingServerLink(item: LibraryMedia) {
        viewModelScope.launch {
            try {
                val link = item.memorizeActionUrl ?: return@launch
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookies()

                // Execute on IO dispatcher like fetchItemDetails does
                val response = withContext(Dispatchers.IO) {
                    Jsoup.connect(link)
                        .cookies(cookies)
                        .timeout(30000)
                        .followRedirects(true)
                        .execute()
                }

                // Update cookies if needed
                val newCookies = response.cookies()
                if (newCookies.isNotEmpty()) {
                    cookieManager.setCookies(NetworkConfig.BASE_LOGGED_IN_URL, newCookies)
                }

                // Toggle local state
                val nowInList = !item.isInMemorizeList
                val nextLink = when {
                    link.contains("addToMemorizeList") ->
                        link.replace("addToMemorizeList", "removeFromMemorizeList")
                    link.contains("removeFromMemorizeList") ->
                        link.replace("removeFromMemorizeList", "addToMemorizeList")
                    link.contains("deleteFromMemorizeList") ->
                        link.replace("deleteFromMemorizeList", "addToMemorizeList")
                    else -> link
                }

                item.isInMemorizeList = nowInList
                item.memorizeActionUrl = nextLink

                // Update wishlist state
                val itemId = "${item.title}|${item.year}|${item.kindOfMedium.name}"
                _wishList.value = if (nowInList) {
                    _wishList.value + itemId
                } else {
                    _wishList.value - itemId
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Aktualisieren der Merkliste: ${e.message}"
            }
        }
    }


    fun syncWishlistFromSearchResult(items: List<LibraryMedia>) {
        val parsed = items.filter { it.isInMemorizeList }
        val ids = parsed.map {
            "${it.title}|${it.year}|${it.kindOfMedium.name}"
        }.toSet()
    }

    // Fetch the current wishlist from the website
    fun fetchWishlist() {
        viewModelScope.launch {
            try {
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookies()

                val wishlistUrl =
                    "${NetworkConfig.BASE_LOGGED_IN_URL}/webOPACClient/memorizelist.do?methodToCall=show"
                val response = Jsoup.connect(wishlistUrl)
                    .cookies(cookies)
                    .timeout(30000)
                    .execute()

                val doc = response.parse()

                // Parse wishlist items from the page
                val wishlistItems = mutableSetOf<String>()
                doc.select("table tr").forEach { row ->
                    val titleElement = row.select("a[href*='singleHit.do']").firstOrNull()
                    if (titleElement != null) {
                        val title = titleElement.text().trim()
                        val yearText = row.text()
                        val yearPattern = "\\[(\\d{4})]".toRegex()
                        val year = yearPattern.find(yearText)?.groupValues?.get(1) ?: ""

                        // Extract media type from image
                        val mediaTypeImg = row.select("img[title]").firstOrNull()
                        val mediaType = mediaTypeImg?.attr("title") ?: "Other"

                        val itemId = "$title|$year|$mediaType"
                        wishlistItems.add(itemId)
                    }
                }

                _wishList.value = wishlistItems

            } catch (e: Exception) {
                // Silently fail if not logged in or wishlist not accessible
            }
        }
    }

    fun isInWishlist(item: LibraryMedia): Boolean {
        val uniqueId = "${item.title}|${item.year}|${item.kindOfMedium.name}"
        return _wishList.value.contains(uniqueId)
    }

    suspend fun getWishlistItems(): List<LibraryMedia> {
        val wishlistItems = mutableListOf<LibraryMedia>()

        try {
            val cookieManager = CookieManager.getInstance()
            val cookies = cookieManager.getCookies()

            val wishlistUrl =
                "${NetworkConfig.BASE_LOGGED_IN_URL}/webOPACClient/memorizelist.do?methodToCall=show"
            val response = Jsoup.connect(wishlistUrl)
                .cookies(cookies)
                .timeout(30000)
                .execute()

            val doc = response.parse()

            // Parse each wishlist item
            doc.select("table tr").forEach { row ->
                val titleElement = row.select("a[href*='singleHit.do']").firstOrNull()
                if (titleElement != null) {
                    val title = titleElement.text().trim().replace("¬", "")
                    val url = NetworkConfig.BASE_LOGGED_IN_URL + titleElement.attr("href")

                    val yearText = row.text()
                    val yearPattern = "\\[(\\d{4})]".toRegex()
                    val year = yearPattern.find(yearText)?.groupValues?.get(1) ?: ""

                    val mediaTypeImg = row.select("img[title]").firstOrNull()
                    val mediaType = mediaTypeImg?.attr("title") ?: "Other"

                    val isAvailable = row.select("span.textgruen").isNotEmpty()

                    wishlistItems.add(
                        LibraryMedia(
                            title = title,
                            url = url,
                            year = year,
                            kindOfMedium = com.fm.beebo.ui.settings.mediaFromString(mediaType),
                            isAvailable = isAvailable,
                            author = "", // Will be filled when details are loaded
                            dueDates = emptyList()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Return empty list if error
        }

        return wishlistItems
    }

    // Initialize is no longer needed since we fetch from website
    fun initialize(context: Context) {
        // Fetch wishlist if logged in
        if (isLoggedIn) {
            fetchWishlist()
        }
    }
}
