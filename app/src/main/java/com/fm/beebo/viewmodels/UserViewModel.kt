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

import com.fm.beebo.logToFile
import com.fm.beebo.parseTitleBlock

class UserViewModel : ViewModel() {
    private val _wishlistItems = MutableStateFlow<List<LibraryMedia>>(emptyList())
    val wishlistItems: StateFlow<List<LibraryMedia>> = _wishlistItems

    var isWishlistLoading by mutableStateOf(false)
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
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }

            try {
                val loginSuccess = loginService.login(username, password)
                if (loginSuccess) {
                    withContext(Dispatchers.Main) {
                        isLoggedIn = true
                        // Clear temporary wishlist when logging in
                        _wishList.value = emptySet()
                    }

                    fetchAccountDetails()
                    // Fetch server-side wishlist after login
                    fetchWishlist()
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Login fehlgeschlagen"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Fehler beim Login: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun fetchAccountDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fees = loginService.fetchAccountFees()
                withContext(Dispatchers.Main) {
                    accountFees = fees
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Fehler beim Abrufen der Kontodaten: ${e.message}"
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            loginService.logout()
            withContext(Dispatchers.Main) {
                isLoggedIn = false
                accountFees = null
                _wishList.value = emptySet()
            }
        }
    }

    fun toggleWishlistItem(item: LibraryMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookies()

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

                // Make the request to add/remove from wishlist
                val response = Jsoup.connect(wishlistUrl)
                    .cookies(cookies)
                    .timeout(30000)
                    .execute()

                // Update local wishlist state on main thread
                val itemId = "${item.title}|${item.year}|${item.kindOfMedium.name}"
                withContext(Dispatchers.Main) {
                    if (_wishList.value.contains(itemId)) {
                        _wishList.value = _wishList.value - itemId
                    } else {
                        _wishList.value = _wishList.value + itemId
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Fehler beim Aktualisieren der Merkliste: ${e.message}"
                }
            }
        }
    }

    fun toggleWishlistUsingServerLink(item: LibraryMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val link = item.memorizeActionUrl ?: return@launch
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookies()

                val response = Jsoup.connect(link)
                    .cookies(cookies)
                    .timeout(30000)
                    .followRedirects(true)
                    .execute()

                // Update cookies if needed
                val newCookies = response.cookies()
                if (newCookies.isNotEmpty()) {
                    cookieManager.setCookies(NetworkConfig.BASE_LOGGED_IN_URL, newCookies)
                }

                // Toggle local state on main thread
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

                withContext(Dispatchers.Main) {
                    item.isInMemorizeList = nowInList
                    item.memorizeActionUrl = nextLink

                    // Update wishlist state
                    val itemId = "${item.title}|${item.year}|${item.kindOfMedium.name}"
                    _wishList.value = if (nowInList) {
                        _wishList.value + itemId
                    } else {
                        _wishList.value - itemId
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Fehler beim Aktualisieren der Merkliste: ${e.message}"
                }
            }
        }
    }

    fun syncWishlistFromSearchResult(items: List<LibraryMedia>) {
        val parsed = items.filter { it.isInMemorizeList }
        val ids = parsed.map {
            "${it.title}|${it.year}|${it.kindOfMedium.name}"
        }.toSet()
        _wishList.value = ids
    }



    fun fetchWishlist() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isWishlistLoading = true
            }

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
                if (doc.select("div.error").text().contains("Diese Sitzung ist nicht mehr gültig!")) {
                    withContext(Dispatchers.Main) {
                        isLoggedIn = false
                        _wishlistItems.value = emptyList()
                        _wishList.value = emptySet()
                        isWishlistLoading = false
                    }
                    return@launch
                }

                val wishlistItems = mutableListOf<LibraryMedia>()
                val wishlistIds = mutableSetOf<String>()

                doc.select("table.data tbody tr").forEachIndexed { index, row ->
                    try {
                        val availabilityLink = row.select("a[href*='availability.do']").firstOrNull()
                        if (availabilityLink != null) {
                            // The structure is: td[0]=checkbox, td[1]=image, td[2]=title, td[3]=cover
                            val titleCell = row.select("td").getOrNull(2)

                            if (titleCell != null) {

                                val (title, year, author) = parseTitleBlock(titleCell, index)

                                val mediaImg   = row.select("img[title]").firstOrNull()
                                val mediaType  = mediaImg?.attr("title") ?: "Other"

                                val detailUrl  = "${NetworkConfig.BASE_LOGGED_IN_URL}${availabilityLink.attr("href")}"

                                val libraryMedia = LibraryMedia(
                                    title          = title,
                                    url            = detailUrl,
                                    year           = year,
                                    author         = author,
                                    kindOfMedium   = com.fm.beebo.ui.settings.mediaFromString(mediaType),
                                    isAvailable    = true,
                                    dueDates       = emptyList(),
                                    isInMemorizeList = true
                                )
                                wishlistItems.add(libraryMedia)
                                wishlistIds.add("$title|$year|${libraryMedia.kindOfMedium.name}|$index")
                            }
                        }
                    } catch (_: Exception) {
                    }
                }

                withContext(Dispatchers.Main) {
                    _wishlistItems.value = wishlistItems
                    _wishList.value = wishlistIds
                    isWishlistLoading = false
                }

            } catch (e: Exception) {
                logToFile("Exception in fetchWishlist: ${e.message}")
                withContext(Dispatchers.Main) {
                    errorMessage = "Fehler beim Laden der Merkliste: ${e.message}"
                    isWishlistLoading = false
                }
            }
        }
    }


    fun isInWishlist(item: LibraryMedia): Boolean {
        val uniqueId = "${item.title}|${item.year}|${item.kindOfMedium.name}"
        return _wishList.value.contains(uniqueId)
    }

    suspend fun getWishlistItems(): List<LibraryMedia> = withContext(Dispatchers.IO) {
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
                    val title = titleElement.text().trim().replace("Â¬", "")
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

        return@withContext wishlistItems
    }

    fun initialize(context: Context) {
        // Fetch wishlist if logged in
        if (isLoggedIn) {
            fetchWishlist()
        }
    }
}
