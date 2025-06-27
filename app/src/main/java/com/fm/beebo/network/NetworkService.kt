package com.fm.beebo.network

import android.webkit.CookieManager
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.ui.settings.Media
import com.fm.beebo.ui.settings.mediaFromString
import com.fm.beebo.viewmodels.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext


object NetworkConfig {
    const val BASE_URL =
        "https://katalog.bibo-dresden.de/webOPACClient/start.do?Login=webopac&BaseURL=this"
    const val BASE_LOGGED_IN_URL = "https://katalog.bibo-dresden.de"
}

class LibrarySearchService {
    companion object {
        private const val BASE_LOGGED_IN_URL = NetworkConfig.BASE_LOGGED_IN_URL
    }


    fun searchWithFlow(
        searchTerm: String,
        maxPages: Int = 3,
        viewModel: SettingsViewModel
    ): Flow<SearchResult> = flow {
        var currentPage = 1
        val results = mutableListOf<LibraryMedia>()
        var totalPages = 0
        var pagesToFetch = maxPages
        viewModel.setAppStart(false)


        try {
            val cookieManager = CookieManager.getInstance()
            var existingCookies = cookieManager.getCookies()
            var csid = cookieManager.getStoredCSId()

            // ✅ If no session exists, initialize a new anonymous session
            if (existingCookies.isEmpty() || csid.isEmpty()) {
                println("🔄 No session found, initializing anonymous session...")

                // Initialize session by connecting to BASE_URL
                val initResponse = Jsoup.connect(NetworkConfig.BASE_URL)
                    .timeout(30000)
                    .execute()

                val initDoc = initResponse.parse()
                val csidInput = initDoc.select("input[name=CSId]").first()

                if (csidInput == null) {
                    emit(SearchResult(emptyList(), 0, false, "Fehler beim Initialisieren der Sitzung"))
                    return@flow
                }

                csid = csidInput.attr("value")
                existingCookies = initResponse.cookies()

                // Store the new session cookies and CSId
                cookieManager.syncFromHttpClient(existingCookies, BASE_LOGGED_IN_URL)
                cookieManager.storeCSId(csid)

                println("✅ Anonymous session initialized with CSId: $csid")
            } else {
                println("🔍 Using existing session with CSId: $csid")
            }


            // Now proceed with search using the session (either existing or newly created)
            val searchUrl = "$BASE_LOGGED_IN_URL/webOPACClient/search.do?methodToCall=submit&CSId=$csid&methodToCallParameter=submitSearch"
            val searchConnection = Jsoup.connect(searchUrl)
                .data("searchCategories[0]", "-1")
                .data("searchString[0]", searchTerm)
                .data("searchHistoryCombinationOperator", "AND")
                .data("searchHistory", "")
                .data("callingPage", "searchParameters")
                .data("selectedViewBranchlib", "0")
                .data("selectedSearchBranchlib", "")
                .data("searchRestrictionID[0]", "8")
                .data(
                    "searchRestrictionValue1[0]",
                    if (viewModel.selectedMediaTypes.value.isNotEmpty()) viewModel.selectedMediaTypes.value[0].asGetParameter() else ""
                )
                .data("searchRestrictionID[1]", "6")
                .data("searchRestrictionValue1[1]", "")
                .data("searchRestrictionID[2]", "3")
                .data(
                    "searchRestrictionValue1[2]",
                    if (viewModel.filterByTimeSpan.value) viewModel.minYear.value.toString() else ""
                )
                .data(
                    "searchRestrictionValue2[2]",
                    if (viewModel.filterByTimeSpan.value) viewModel.maxYear.value.toString() else ""
                )
                .cookies(existingCookies)
                .timeout(30000)


            val searchResponse = searchConnection.execute()


            // Check if session expired
            val searchDoc = searchResponse.parse()
            if (searchDoc.select("div.error").text().contains("Diese Sitzung ist nicht mehr gültig!")) {
                // Clear expired session and try to initialize a new one
                cookieManager.clearCSId()
                cookieManager.removeAllCookies(null)
                cookieManager.flush()

                println("🔄 Session expired, trying to initialize new session...")

                // Try to initialize a fresh session
                val freshInitResponse = Jsoup.connect(NetworkConfig.BASE_URL)
                    .timeout(30000)
                    .execute()

                val freshInitDoc = freshInitResponse.parse()
                val freshCsidInput = freshInitDoc.select("input[name=CSId]").first()

                if (freshCsidInput == null) {
                    emit(SearchResult(emptyList(), 0, false, "Sitzung abgelaufen und konnte nicht erneuert werden"))
                    return@flow
                }

                val freshCsid = freshCsidInput.attr("value")
                val freshCookies = freshInitResponse.cookies()

                cookieManager.syncFromHttpClient(freshCookies, BASE_LOGGED_IN_URL)
                cookieManager.storeCSId(freshCsid)

                // Retry search with fresh session
                val retrySearchUrl = "$BASE_LOGGED_IN_URL/webOPACClient/search.do?methodToCall=submit&CSId=$freshCsid&methodToCallParameter=submitSearch"
                val retrySearchResponse = Jsoup.connect(retrySearchUrl)
                    .data("searchCategories[0]", "-1")
                    .data("searchString[0]", searchTerm)
                    .data("searchHistoryCombinationOperator", "AND")
                    .data("searchHistory", "")
                    .data("callingPage", "searchParameters")
                    .data("selectedViewBranchlib", "0")
                    .data("selectedSearchBranchlib", "")
                    .data("searchRestrictionID[0]", "8")
                    .data(
                        "searchRestrictionValue1[0]",
                        if (viewModel.selectedMediaTypes.value.isNotEmpty()) viewModel.selectedMediaTypes.value[0].asGetParameter() else ""
                    )
                    .data("searchRestrictionID[1]", "6")
                    .data("searchRestrictionValue1[1]", "")
                    .data("searchRestrictionID[2]", "3")
                    .data(
                        "searchRestrictionValue1[2]",
                        if (viewModel.filterByTimeSpan.value) viewModel.minYear.value.toString() else ""
                    )
                    .data(
                        "searchRestrictionValue2[2]",
                        if (viewModel.filterByTimeSpan.value) viewModel.maxYear.value.toString() else ""
                    )
                    .cookies(freshCookies)
                    .timeout(30000)
                    .execute()

                // Update search response and doc with retry results
                val retrySearchDoc = retrySearchResponse.parse()
                if (retrySearchDoc.select("div.error").text().contains("Diese Sitzung ist nicht mehr gültig!")) {
                    emit(SearchResult(emptyList(), 0, false, "Sitzung konnte nicht erneuert werden"))
                    return@flow
                }

                // Use the retry results
                searchDoc.empty()
                searchDoc.appendElement("body").html(retrySearchDoc.html())

                println("✅ Session renewed and search retried")
            }


            // Only update cookies if server sends new ones AND they're different
            val newCookies = searchResponse.cookies()
            if (newCookies.isNotEmpty() && cookiesChanged(existingCookies, newCookies)) {
                cookieManager.setCookies(BASE_LOGGED_IN_URL, newCookies)
                println("🍪 Session cookies updated due to server changes")
            }


            // Check for results
            if (searchDoc.text().contains("keine Treffer")) {
                emit(SearchResult(emptyList(), 0, true, "Keine Treffer gefunden"))
                return@flow
            }


            val currentTab = getCurrentTab(searchDoc)


            // Handle only one result
            if (currentTab == "Detailanzeige") {
                val result = parseDetails(doc = searchDoc)
                if (result != null) {
                    result.url = searchResponse.url().toString()
                    val details: LibraryMedia? = getItemDetails(result.url, false)
                    if (details != null) {
                        results.add(details)
                        emit(SearchResult(results.toList(), 1, true, "1 Treffer"))
                    } else {
                        emit(SearchResult(emptyList(), 0, false, "Konnte Details nicht abrufen"))
                    }
                } else {
                    emit(SearchResult(emptyList(), 0, false, "Konnte Details nicht parsen"))
                }
            } else if (currentTab == "Suchergebnis") {
                // Process first page results
                val firstPageResults = extractMetadata(searchDoc)
                results.addAll(firstPageResults)
                totalPages = getMaxPages(searchDoc)
                pagesToFetch = minOf(totalPages, maxPages)


                // Emit first page results immediately
                emit(
                    SearchResult(
                        results = results.toList(),
                        totalPages = totalPages,
                        success = true,
                        message = if (results.isEmpty()) "Keine Treffer gefunden" else
                            "Erste Seite: ${results.size} Treffer von ungefähr ${totalPages * 10} Treffern",
                        progress = currentPage.toFloat() / totalPages
                    )
                )


                var nextUrl = getNextPageLink(searchDoc)
                while (nextUrl != null && currentPage < pagesToFetch && coroutineContext.isActive) {
                    currentPage++
                    try {
                        val nextPageResponse = Jsoup.connect(nextUrl)
                            .cookies(cookieManager.getCookies())
                            .timeout(30000)
                            .execute()
                        cookieManager.setCookies(BASE_LOGGED_IN_URL, nextPageResponse.cookies())
                        val nextPageDoc = nextPageResponse.parse()
                        val pageResults = extractMetadata(nextPageDoc)
                        results.addAll(pageResults)


                        // Emit updated results after each page
                        emit(
                            SearchResult(
                                results = results.toList(),
                                totalPages = totalPages,
                                success = true,
                                message = "Seite $currentPage: ${results.size} Treffer von ungefähr ${totalPages * 10} Treffern",
                                progress = currentPage.toFloat() / pagesToFetch
                            )
                        )


                        // Get next page URL
                        nextUrl = getNextPageLink(nextPageDoc)
                        if (currentPage >= totalPages) break
                    } catch (e: Exception) {
                        // Handle page-specific errors but continue with results we have
                        println("Error loading page $currentPage: ${e.message ?: "Unknown error"}")
                        emit(
                            SearchResult(
                                results = results.toList(),
                                totalPages = totalPages,
                                success = true,
                                message = "Teilweise Ergebnisse (${results.size}): Fehler bei Seite $currentPage",
                                isComplete = true,
                                progress = currentPage.toFloat() / pagesToFetch
                            )
                        )
                        break
                    }
                }


                // Final results summary if we have results
                if (results.isNotEmpty() && coroutineContext.isActive) {
                    emit(
                        SearchResult(
                            results = results.toList(),
                            totalPages = totalPages,
                            success = true,
                            message = if (totalPages > 1 && (totalPages * 10 - results.size) >= 10)
                                "${results.size} Treffer von ungefähr ${totalPages * 10} Treffern"
                            else
                                "${results.size} Treffer",
                            isComplete = true,
                            progress = currentPage.toFloat() / pagesToFetch
                        )
                    )
                }
            }
        } catch (e: Exception) {
            if (coroutineContext.isActive) {
                e.printStackTrace()
                val errorMessage = e.message ?: "Unbekannter Fehler bei der Suche"
                emit(
                    SearchResult(
                        results = results.toList(),
                        totalPages = totalPages,
                        success = false,
                        message = "Error: $errorMessage",
                        progress = currentPage.toFloat() / pagesToFetch
                    )
                )
            }
        }
    }

    private fun cookiesChanged(
        oldCookies: Map<String, String>,
        newCookies: Map<String, String>
    ): Boolean {
        val importantKeys = setOf("JSESSIONID", "USERSESSIONID", "BaseURL")
        return importantKeys.any { key ->
            oldCookies[key] != newCookies[key]
        }
    }

    data class SearchResult(
        val results: List<LibraryMedia>,
        val totalPages: Int,
        val success: Boolean,
        val message: String,
        val isComplete: Boolean = false,
        val progress: Float = 0f
    )

    /**
     * Connects to the item URL and invokes the DOM parser for media details.
     */
    suspend fun getItemDetails(
        itemUrl: String,
        available: Boolean
    ): LibraryMedia {
        val cookieManager = CookieManager.getInstance()
        return withContext(Dispatchers.IO) {
            val response = Jsoup.connect(itemUrl)
                .cookies(cookieManager.getCookies())
                .timeout(30000)
                .execute()

            val doc = response.parse()
            parseItemDetails(doc, itemUrl, available)
        }
    }

    suspend fun parseItemDetails(
        doc: Document,
        url: String,
        available: Boolean
    ): LibraryMedia {
        CookieManager.getInstance()
        return withContext(Dispatchers.IO) {
            // Check for session expiry
            if (doc.select("div.error").text().contains("Diese Sitzung ist nicht mehr gültig!")) {
                println("Session expired. Please log in again.")
                return@withContext LibraryMedia()
            }

            val extraDetailsTabUrl = changeDetailsTab(doc)
            val extendedMedia: LibraryMedia? = if (extraDetailsTabUrl.isNotEmpty()) {
                parseDetailsTab(BASE_LOGGED_IN_URL + extraDetailsTabUrl)
            } else {
                null
            }

            val title = doc.select("h1").text()
            val dueDates = doc.select("td:contains(entliehen bis)").eachText()
                .map { it.replace("entliehen bis ", "").replace("(gesamte Vormerkungen: 0)", "") }
            val kindOfMedium = doc.select("div.teaser").text().let {
                when {
                    it.contains("DVD") -> Media.Filme
                    it.contains("Blu-ray") -> Media.Bluray
                    it.contains("CD") -> Media.CDs
                    it.contains("Buch") -> Media.Bücher
                    else -> Media.Other
                }
            }

            val librariesAvailable = mutableListOf<String>()
            val librariesUnavailable =
                mutableListOf<Pair<String, String>>() // Library name and due date
            val librariesOrderable = mutableListOf<String>()

            doc.select("table.data tbody tr").forEach { row ->
                val library = row.select("td").getOrNull(2)?.text()?.trim() ?: ""
                val statusText = row.select("td").getOrNull(3)?.text()?.trim() ?: ""

                when {
                    statusText.contains("ausleihbar") -> librariesAvailable.add(library)
                    statusText.contains("entliehen bis") -> {
                        val dueDate =
                            statusText.substringAfter("entliehen bis ").substringBefore(" (")
                        librariesUnavailable.add(library to dueDate)
                    }

                    statusText.contains("bestellbar") -> librariesOrderable.add(library)
                }
            }

            LibraryMedia(
                url = url,
                isAvailable = available,
                year = extendedMedia?.year?.replace("[", "")?.replace("]", "") ?: "",
                title = title,
                dueDates = dueDates,
                kindOfMedium = extendedMedia?.kindOfMedium ?: kindOfMedium,
                author = extendedMedia?.author ?: "",
                actors = extendedMedia?.actors ?: emptyList(),
                language = extendedMedia?.language ?: "",
                isbn = extendedMedia?.isbn ?: "",
                publisher = extendedMedia?.publisher ?: "",
                direction = extendedMedia?.direction ?: "",
                availableLibraries = librariesAvailable,
                unavailableLibraries = librariesUnavailable,
                orderableLibraries = librariesOrderable
            )
        }
    }

    suspend fun parseDetailsTab(
        detailsTabUrl: String
    ): LibraryMedia? {
        val cookieManager = CookieManager.getInstance()
        return withContext(Dispatchers.IO) {
            val response = Jsoup.connect(detailsTabUrl)
                .cookies(cookieManager.getCookies())
                .timeout(30000)
                .execute()

            val doc = response.parse()

            parseDetails(doc)
        }
    }

    private fun parseDetails(doc: Document): LibraryMedia? {
        if (doc.select("div.error").text().contains("Diese Sitzung ist nicht mehr gültig!")) {
            println("Session expired. Please log in again.")
            return null
        }

        doc.getElementById("tab-content")?.select("table.data")?.select("tbody")?.select("tr")
            ?.select("td")

        val attributes = mutableMapOf<String, String>()
        val actorsList = mutableListOf<String>()

        // Define the mapping of HTML field names to data class properties
        val fieldMappings = mapOf(
            "Titel" to "title",
            "Medienart" to "kindOfMedium",
            "Erscheinungsdatum" to "year",
            "Verf.Vorlage" to "author",
            "Sprache(n)" to "language",
            "Verlagsname" to "publisher",
            "Regie" to "direction",
            "ISBN" to "isbn",
            "Cover_URL" to "url",
            "Beteiligt" to "actors"
        )

        doc.select("strong.c2").forEach { strongElement ->
            val key = strongElement.text().removeSuffix(":").trim()
            val valueElement: Element? = strongElement.nextElementSibling()
            val value = valueElement?.text()?.trim() ?: ""

            if (key == "Beteiligt") {
                actorsList.add(value)
            } else if (fieldMappings.containsKey(key)) {
                attributes[fieldMappings[key]!!] = value
            }
        }
        return LibraryMedia(
            url = attributes["url"] ?: "",
            title = attributes["title"] ?: "Unbekannter Titel",
            year = attributes["year"] ?: "",
            isbn = attributes["isbn"] ?: "",
            kindOfMedium = mediaFromString(attributes["kindOfMedium"] ?: ""),
            author = attributes["author"] ?: "",
            language = attributes["language"] ?: "",
            publisher = attributes["publisher"] ?: "",
            direction = attributes["direction"] ?: "",
            actors = actorsList,
            dueDates = emptyList()
        )
    }

    private fun getMaxPages(doc: Document): Int {
        val lastPageLink = doc.select("a[title='Letzte Seite']").firstOrNull()

        if (lastPageLink != null) {
            val lastPageUrl = lastPageLink.attr("href")

            // Extract curPos from URL
            val pattern = Pattern.compile("curPos=(\\d+)")
            val matcher = pattern.matcher(lastPageUrl)

            if (matcher.find()) {
                val lastPosition = matcher.group(1)?.toInt()
                val resultsPerPage = 10  // Adjust based on site pagination
                if (lastPosition != null) {
                    return (lastPosition / resultsPerPage) + 1
                }
            }
        }
        return 1  // If we can't determine, assume at least 1 page
    }

    private fun getNextPageLink(doc: Document): String? {
        val nextPageLink = doc.select("a[title='Nächste Seite']").firstOrNull()
        return if (nextPageLink != null) {
            val nextPageUrl = nextPageLink.attr("href")
            "${BASE_LOGGED_IN_URL}${nextPageUrl}"  // Ensure full URL
        } else {
            null
        }
    }

    /**
     * Is invoked on the search results list
     * @return List of search results as DataClass LibraryMedia
     */
    private fun extractMetadata(doc: Document): List<LibraryMedia> {
        val results = mutableListOf<LibraryMedia>()
        val table = doc.select("table").firstOrNull() ?: return results
        for (row in table.select("tr").filter { row -> row.select("th").isNotEmpty() }) {
            try {
                // title
                val titleTag = row.select("a[href][title=null]").firstOrNull()
                    ?: row.select("a[href]:not([title='in die Merkliste'])").firstOrNull()
                    ?: row.select("a[href]:not([title='vormerken/bestellen'])").firstOrNull()
                val title = titleTag?.text()?.trim() ?: continue

                // Extract the year
                val text = row.select("td").text()
                var year = ""

                // Look for [YYYY] pattern first
                val bracketPattern = "\\[(\\d{4})]".toRegex()
                val bracketMatch = bracketPattern.find(text)
                if (bracketMatch != null) {
                    year = bracketMatch.groupValues[1]
                } else {
                    // If not found in brackets, look for standalone year
                    val yearPattern = "\\b(20\\d{2})\\b".toRegex()  // Matches years from 2000-2099
                    val yearMatch = yearPattern.find(text)
                    if (yearMatch != null) {
                        year = yearMatch.groupValues[1]
                    }
                }

                val isAvailable = row.select("span.textgruen").isNotEmpty()

                // Extract the link
                val itemLink = titleTag.attr("href")
                val kindOfMediumRaw = row.select("img").firstOrNull()
                val kindOfMedium = kindOfMediumRaw?.attr("title") ?: ""

                val media = LibraryMedia(
                    url = "$BASE_LOGGED_IN_URL$itemLink",
                    isAvailable = isAvailable,
                    year = year,
                    title = title,
                    dueDates = emptyList(),
                    kindOfMedium = mediaFromString(kindOfMedium)
                )

                results.add(media)
            } catch (e: Exception) {
                println("Error processing item: ${e.message}")
                continue
            }
        }
        return results
    }

    private fun changeDetailsTab(doc: Document): String {
        return doc.select("#labelTitle a").first()?.attr("href") ?: ""
    }

    private fun getCurrentTab(doc: Document): String {
        return doc.select("#current2").text()
    }
}
