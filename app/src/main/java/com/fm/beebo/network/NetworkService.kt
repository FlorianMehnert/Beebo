package com.fm.beebo.network

import android.webkit.CookieManager
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.viewmodels.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


object NetworkConfig {
    const val BASE_URL =
        "https://katalog.bibo-dresden.de/webOPACClient/start.do?Login=webopac&BaseURL=this"
    const val BASE_LOGGED_IN_URL = "https://katalog.bibo-dresden.de"
}

class LibrarySearchService {
    companion object {
        private const val BASE_URL = NetworkConfig.BASE_URL
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
        val cookieManager = CookieManager.getInstance()

        try {
            // Initialize the session and get the CSId
            val initialResponse = Jsoup.connect(BASE_URL)
                .timeout(30000)
                .execute()

            // Store cookies
            cookieManager.setCookies(BASE_LOGGED_IN_URL, initialResponse.cookies())

            val initialDoc = initialResponse.parse()
            val csidInput = initialDoc.select("input[name=CSId]").first()
                ?: run {
                    emit(SearchResult(emptyList(), 0, false, "Session initialization failed"))
                    return@flow
                }

            val csid = csidInput.attr("value")

            // Prepare search URL
            val searchUrl = "$BASE_LOGGED_IN_URL/webOPACClient/search.do?methodToCall=submit&CSId=$csid&methodToCallParameter=submitSearch"

            val searchResponse = Jsoup.connect(searchUrl)
                .data("searchCategories[0]", "-1")
                .data("searchString[0]", searchTerm)
                .data("callingPage", "searchParameters")
                .data("selectedViewBranchlib", "0")
                .data("selectedSearchBranchlib", "")
                .data("searchRestrictionID[0]", "8")
                .data("searchRestrictionValue1[0]", viewModel.selectedFilterOption.value.getSearchRestrictionValue1())
                .data("searchRestrictionID[1]", "6")
                .data("searchRestrictionValue1[1]", "")
                .data("searchRestrictionID[2]", "3")
                .data("searchRestrictionValue1[2]", "")
                .data("searchRestrictionValue2[2]", "")
                .cookies(cookieManager.getCookies())
                .timeout(30000)
                .execute()

            // Update cookies
            cookieManager.setCookies(BASE_LOGGED_IN_URL, searchResponse.cookies())

            val searchDoc = searchResponse.parse()

            // Check if we have results
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

                // Emit first page results immediately
                emit(SearchResult(
                    results = results.toList(),
                    totalPages = totalPages,
                    success = true,
                    message = if (results.isEmpty()) "Keine Treffer gefunden" else
                        "Erste Seite: ${results.size} Treffer von ungefähr ${totalPages*10} Treffern",
                    progress = currentPage.toFloat()*10 / totalPages
                ))

                val pagesToFetch = minOf(totalPages, maxPages)

                if (currentPage >= pagesToFetch || currentPage == 3){

                }
                var nextUrl = getNextPageLink(searchDoc)

                while (nextUrl != null && currentPage < pagesToFetch) {
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
                        emit(SearchResult(
                            results = results.toList(),
                            totalPages = totalPages,
                            success = true,
                            message = "Seite $currentPage: ${results.size} Treffer von ungefähr ${totalPages*10} Treffern",
                            progress = currentPage.toFloat()*10 / totalPages
                        ))

                        // Get next page URL
                        nextUrl = getNextPageLink(nextPageDoc)
                        if (currentPage >= totalPages) break
                    } catch (e: Exception) {
                        // Handle page-specific errors but continue with results we have
                        println("Error loading page $currentPage: ${e.message ?: "Unknown error"}")
                        emit(SearchResult(
                            results = results.toList(),
                            totalPages = totalPages,
                            success = true,
                            message = "Teilweise Ergebnisse (${results.size}): Fehler bei Seite $currentPage",
                            isComplete = true,
                            progress = currentPage.toFloat() / totalPages
                        ))
                        break
                    }
                }

                // Final results summary if we have results
                if (results.isNotEmpty()) {
                    emit(SearchResult(
                        results = results.toList(),
                        totalPages = totalPages,
                        success = true,
                        message = if (totalPages > 1 && (totalPages * 10 - results.size) >= 10)
                            "${results.size} Treffer von ungefähr ${totalPages*10} Treffern."
                        else
                            "${results.size} Treffer",
                        isComplete = true,
                        progress = currentPage.toFloat()*10 / totalPages
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = e.message ?: "Unbekannter Fehler bei der Suche"
            emit(SearchResult(
                results = results.toList(),
                totalPages = totalPages,
                success = false,
                message = "Error: $errorMessage",
                progress = currentPage.toFloat()*10 / totalPages
            ))
        }
    }


// Create a data class to hold search results and metadata
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
    ): LibraryMedia? {
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
    ): LibraryMedia? {
        val cookieManager = CookieManager.getInstance()
        return withContext(Dispatchers.IO) {
            // Check for session expiry
            if (doc.select("div.error").text().contains("Diese Sitzung ist nicht mehr gültig!")) {
                println("Session expired. Please log in again.")
                return@withContext null
            }

            val extraDetailsTabUrl = changeDetailsTab(doc)
            val extendedMedia: LibraryMedia? = if (extraDetailsTabUrl.isNotEmpty()) {
                parseDetailsTab(BASE_LOGGED_IN_URL + extraDetailsTabUrl)
            } else {
                null
            }

            val title = doc.select("h1").text()
            val dueDates = doc.select("td:contains(entliehen bis)").eachText()
                .map { it.replace("entliehen bis ", "") }
            val kindOfMedium = doc.select("div.teaser").text().let {
                when {
                    it.contains("DVD") -> "DVD"
                    it.contains("Blu-ray") -> "Blu-ray Disc"
                    it.contains("CD") -> "CD"
                    it.contains("Buch") -> "Buch"
                    else -> ""
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
        println(attributes["kindOfMedium"])
        // Construct LibraryMedia object with mapped values
        return LibraryMedia(
            url = attributes["url"] ?: "",
            title = attributes["title"] ?: "Unbekannter Titel",
            year = attributes["year"] ?: "",
            isbn = attributes["isbn"] ?: "",
            kindOfMedium = attributes["kindOfMedium"] ?: "",
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
                    kindOfMedium = kindOfMedium
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
