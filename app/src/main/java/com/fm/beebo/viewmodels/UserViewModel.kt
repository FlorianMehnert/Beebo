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

                                val availabilityUrl = availabilityLink.attr("href")
                                val curPosMatch = "curPos=(\\d+)".toRegex().find(availabilityUrl)
                                val curPos = curPosMatch?.groupValues?.get(1) ?: "1"

                                val identifierInput = row.select("input[name='identifier']").firstOrNull()
                                val identifier = identifierInput?.attr("value") ?: run {
                                    val checkboxInput = row.select("input[type='checkbox']").firstOrNull()
                                    checkboxInput?.attr("value") ?: "unknown_${index}"
                                }

                                val detailUrl = "${NetworkConfig.BASE_LOGGED_IN_URL}$availabilityUrl"

                                val libraryMedia = LibraryMedia(
                                    title = title,
                                    url = detailUrl,
                                    year = year,
                                    author = author,
                                    kindOfMedium = com.fm.beebo.ui.settings.mediaFromString(mediaType),
                                    isAvailable = true,
                                    dueDates = emptyList(),
                                    isInMemorizeList = true,
                                    memorizeActionUrl = "${NetworkConfig.BASE_LOGGED_IN_URL}$availabilityUrl"
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


    fun initialize() {
        // Fetch wishlist if logged in
        if (isLoggedIn) {
            fetchWishlist()
        }
    }
}
