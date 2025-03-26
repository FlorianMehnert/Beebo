package com.fm.beebo.network

import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.network.NetworkConfig.BASE_LOGGED_IN_URL
import com.fm.beebo.network.NetworkConfig.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

object NetworkConfig {
    const val BASE_URL = "https://katalog.bibo-dresden.de/webOPACClient/start.do?Login=webopac&BaseURL=this"
    const val BASE_LOGGED_IN_URL = "https://katalog.bibo-dresden.de"
}


class LibrarySearchService {
    companion object {
        private const val BASE_URL = NetworkConfig.BASE_URL
        private const val BASE_LOGGED_IN_URL = NetworkConfig.BASE_LOGGED_IN_URL
    }

    suspend fun search(
        searchTerm: String,
        maxPages: Int = 3
    ): Pair<List<LibraryMedia>, Map<String, String>> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<LibraryMedia>()
            val cookies = HashMap<String, String>()

            try {
                // Initialize the session and get the CSId
                val initialResponse = Jsoup.connect(BASE_URL)
                    .timeout(30000)
                    .execute()

                // Store cookies
                cookies.putAll(initialResponse.cookies())

                val initialDoc = initialResponse.parse()
                val csidInput = initialDoc.select("input[name=CSId]").first()
                    ?: return@withContext Pair(results, cookies)

                val csid = csidInput.attr("value")

                // Prepare search URL
                val searchUrl =
                    "$BASE_LOGGED_IN_URL/webOPACClient/search.do?methodToCall=submit&CSId=$csid&methodToCallParameter=submitSearch"

                // Execute the search
                val searchResponse = Jsoup.connect(searchUrl)
                    .data("searchCategories[0]", "-1")
                    .data("searchString[0]", searchTerm)
                    .data("callingPage", "searchParameters")
                    .data("selectedViewBranchlib", "0")
                    .data("selectedSearchBranchlib", "")
                    .data("searchRestrictionID[0]", "8")
                    .data("searchRestrictionValue1[0]", "")
                    .data("searchRestrictionID[1]", "6")
                    .data("searchRestrictionValue1[1]", "")
                    .data("searchRestrictionID[2]", "3")
                    .data("searchRestrictionValue1[2]", "")
                    .data("searchRestrictionValue2[2]", "")
                    .cookies(cookies)
                    .timeout(30000)
                    .execute()

                // Update cookies
                cookies.putAll(searchResponse.cookies())

                val searchDoc = searchResponse.parse()

                // Check if we have results
                if (searchDoc.text().contains("keine Treffer")) {
                    return@withContext Pair(results, cookies)
                }

                val currentTab = getCurrentTab(searchDoc)
                if (currentTab == "Detailanzeige"){
                    val result = parseDetails(doc = searchDoc)
                    if (result != null){
                        changeDetailsTab(searchDoc)
                        result.url = searchResponse.url().toString()
                        println(result.toString())
                        val details : LibraryMedia? = getItemDetails2(searchDoc, result.url, cookies)
                        if (details != null) {
                            results.add(details)
                        }
                    }
                }else if (currentTab == "Suchergebnis"){
                    // Process first page results
                    val firstPageResults = extractMetadata(searchDoc)
                    println("fpr: " + firstPageResults.count())
                    results.addAll(firstPageResults)
                    val totalPages = getMaxPages(searchDoc)
                    val pagesToFetch = minOf(totalPages, maxPages)
                    var currentPage = 1
                    var nextUrl = getNextPageLink(searchDoc)

                    while (nextUrl != null && currentPage < pagesToFetch) {
                        currentPage++

                        val nextPageResponse = Jsoup.connect(nextUrl)
                            .cookies(cookies)
                            .timeout(30000)
                            .execute()

                        cookies.putAll(nextPageResponse.cookies())

                        val nextPageDoc = nextPageResponse.parse()
                        val pageResults = extractMetadata(nextPageDoc)
                        results.addAll(pageResults)

                        // Get next page URL
                        nextUrl = getNextPageLink(nextPageDoc)
                        if (currentPage >= totalPages) break
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            println(results.count())

            return@withContext Pair(results, cookies)
        }
    }




    suspend fun getItemDetails(itemUrl: String, cookies: Map<String, String>, isAvailable: Boolean): LibraryMedia? {
        return withContext(Dispatchers.IO) {
            val response = Jsoup.connect(itemUrl)
                .cookies(cookies)
                .timeout(30000)
                .execute()

            val doc = response.parse()

            // Check for session expiry
            if (doc.select("div.error").text().contains("Diese Sitzung ist nicht mehr gültig!")) {
                // Handle session expiry
                println("Session expired. Please log in again.")
                return@withContext null
            }


            val extraDetailsTabUrl = changeDetailsTab(doc)
            var extendedMedia: LibraryMedia? = LibraryMedia()
            if (extraDetailsTabUrl.isNotEmpty()) {
                extendedMedia = parseDetailsTab(BASE_LOGGED_IN_URL + extraDetailsTabUrl, cookies)
            }

            // Extract title
            val title = doc.select("h1").text()
            var language = ""
            var publisher = ""
            var direction = ""
            var actors: List<String> = emptyList()
            var author = ""
            var isbn = ""
            var year = ""

            if (extendedMedia != null) {
                if (extendedMedia.language.isNotEmpty()){language = extendedMedia.language}
                if (extendedMedia.publisher.isNotEmpty()){publisher = extendedMedia.publisher}
                if (extendedMedia.direction.isNotEmpty()) { direction = extendedMedia.direction }
                if (extendedMedia.actors.isNotEmpty()) { actors = extendedMedia.actors }
                if (extendedMedia.author.isNotEmpty()) { author = extendedMedia.author }
                if (extendedMedia.isbn.isNotEmpty()) { isbn = extendedMedia.isbn }
                if (extendedMedia.year.isNotEmpty()) { year = extendedMedia.year.replace("[", "").replace("]", "") }
            }

            // Extract availability and due dates

            val dueDates = doc.select("td:contains(entliehen bis)").eachText()
                .map { it.replace("entliehen bis ", "") }


            // Extract kind of medium
            val kindOfMedium = doc.select("div.teaser").text().let {
                if (it.contains("DVD")) "DVD"
                else if (it.contains("Blu-ray")) "Blu-ray Disc"
                else if (it.contains("CD")) "CD"
                else if (it.contains("Buch")) "Buch"
                else ""
            }

            LibraryMedia(
                url = itemUrl,
                isAvailable = isAvailable,
                year = year,
                title = title,
                dueDates = dueDates,
                kindOfMedium = kindOfMedium,
                author = author,
                actors = actors,
                language = language,
                isbn = isbn,
                publisher = publisher,
                direction = direction
            )
        }
    }

    suspend fun getItemDetails2(doc: Document, url: String, cookies: Map<String, String>): LibraryMedia? {
        return withContext(Dispatchers.IO) {

            // Check for session expiry
            if (doc.select("div.error").text().contains("Diese Sitzung ist nicht mehr gültig!")) {
                // Handle session expiry
                println("Session expired. Please log in again.")
                return@withContext null
            }

            val extraDetailsTabUrl = changeDetailsTab(doc)
            var extendedMedia: LibraryMedia? = LibraryMedia()
            if (extraDetailsTabUrl.isNotEmpty()) {
                extendedMedia = parseDetailsTab(BASE_LOGGED_IN_URL + extraDetailsTabUrl, cookies)
            }

            // Extract title
            val title = doc.select("h1").text()
            var language = ""
            var publisher = ""
            var direction = ""
            var actors: List<String> = emptyList()
            var author = ""
            var isAvailable: Boolean? = null
            var isbn = ""
            var year = ""
            var kindOfMedium = ""

            if (extendedMedia != null) {
                if (extendedMedia.language.isNotEmpty()){language = extendedMedia.language}
                if (extendedMedia.publisher.isNotEmpty()){publisher = extendedMedia.publisher}
                if (extendedMedia.direction.isNotEmpty()) { direction = extendedMedia.direction }
                if (extendedMedia.actors.isNotEmpty()) { actors = extendedMedia.actors }
                if (extendedMedia.author.isNotEmpty()) { author = extendedMedia.author }
                if (extendedMedia.isbn.isNotEmpty()) { isbn = extendedMedia.isbn }
                isAvailable = extendedMedia.isAvailable
                if (extendedMedia.year.isNotEmpty()) { year = extendedMedia.year.replace("[", "").replace("]", "") }
                if (extendedMedia.kindOfMedium.isNotEmpty()) {kindOfMedium = extendedMedia.kindOfMedium}
            }

            // Extract availability and due dates

            val dueDates = doc.select("td:contains(entliehen bis)").eachText()
                .map { it.replace("entliehen bis ", "") }
            LibraryMedia(
                url = url,
                isAvailable = false,
                year = year,
                title = title,
                dueDates = dueDates,
                kindOfMedium = kindOfMedium,
                author = author,
                actors = actors,
                language = language,
                isbn = isbn,
                publisher = publisher,
                direction = direction
            )
        }
    }

    suspend fun parseDetailsTab(
        detailsTabUrl: String,
        cookies: Map<String, String>
    ): LibraryMedia? {
        return withContext(Dispatchers.IO) {
            val response = Jsoup.connect(detailsTabUrl)
                .cookies(cookies)
                .timeout(30000)
                .execute()

            val doc = response.parse()

            parseDetails(doc)
        }
    }

    private fun parseDetails(doc: Document) : LibraryMedia?{
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

        // Construct LibraryMedia object with mapped values
        return LibraryMedia(
            url = attributes["url"] ?: "",
            title = attributes["title"] ?: "Unbekannter Titel",
            year = attributes["year"] ?: "kein Jahr",
            isbn = attributes["isbn"] ?: "keine ISBN gefunden",
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
        println("table exists")
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
        println(results.count())
        return results
    }

    private fun changeDetailsTab(doc: Document): String {
        return doc.select("#labelTitle a").first()?.attr("href") ?: ""
    }

    private fun getCurrentTab(doc: Document): String {
        return doc.select("#current2").text()
    }
}

class LoginService{

    /**
     * Get CSID to allow login if never logged in before then login using credentials
     * and return cookies
     */
    suspend fun login(username: String, password: String): Map<String, String>? {
        return withContext(Dispatchers.IO) {
            val cookies = mutableMapOf<String, String>()

            try {
                val initialResponse = Jsoup.connect(BASE_URL)
                    .timeout(30000)
                    .execute()

                // Store initial cookies
                cookies.putAll(initialResponse.cookies())

                val initialDoc = initialResponse.parse()
                val csidInput = initialDoc.select("input[name=CSId]").first()
                    ?: return@withContext null

                val csid = csidInput.attr("value")

                // Step 2: Send login request
                val loginResponse = Jsoup.connect("$BASE_LOGGED_IN_URL/webOPACClient/login.do")
                    .data("username", username)
                    .data("password", password)
                    .data("CSId", csid)
                    .cookies(cookies)
                    .timeout(30000)
                    .method(Connection.Method.POST)
                    .execute()

                // Step 3: Update cookies after login
                cookies.putAll(loginResponse.cookies())

                // Step 4: Verify if login was successful
                val loginDoc = loginResponse.parse()
                val loginError = loginDoc.select(".loginError") // Adjust the selector if necessary

                return@withContext if (loginError.isNotEmpty()) {
                    println("Login failed: ${loginError.text()}")
                    null
                } else {
                    println("Login successful")
                    cookies
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }
}
