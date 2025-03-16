package com.fm.beebo.network

import com.fm.beebo.models.LibraryMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.regex.Pattern

class LibrarySearchService {
    companion object {
        private const val BASE_URL = "https://katalog.bibo-dresden.de/webOPACClient/start.do?Login=webopac&BaseURL=this"
        private const val BASE_LOGGED_IN_URL = "https://katalog.bibo-dresden.de"
    }

    suspend fun search(searchTerm: String, maxPages: Int = 3): List<Pair<String, Boolean>> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<Pair<String, Boolean>>()
            val statusUpdates = mutableListOf<String>()

            try {
                // Create a session
                val cookies = HashMap<String, String>()

                // Initialize the session and get the CSId
                statusUpdates.add("Initializing search session...")
                val initialResponse = Jsoup.connect(BASE_URL)
                    .timeout(30000)
                    .execute()

                // Store cookies
                cookies.putAll(initialResponse.cookies())

                val initialDoc = initialResponse.parse()
                val csidInput = initialDoc.select("input[name=CSId]").first()

                if (csidInput == null) {
                    statusUpdates.add("Failed to get CSID token")
                    return@withContext results
                }

                val csid = csidInput.attr("value")
                statusUpdates.add("Got CSID: ${csid.take(5)}...")

                // Prepare search URL
                val searchUrl = "$BASE_LOGGED_IN_URL/webOPACClient/search.do?methodToCall=submit&CSId=$csid&methodToCallParameter=submitSearch"

                // Execute the search
                statusUpdates.add("Searching for: '$searchTerm'...")
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
                    statusUpdates.add("No results found")
                    return@withContext results
                }

                // Determine total pages
                val totalPages = getMaxPages(searchDoc)
                statusUpdates.add("Found $totalPages pages of results")

                // Limit to specified max pages
                val pagesToFetch = minOf(totalPages, maxPages)

                // Process first page
                statusUpdates.add("Processing first page...")
                val firstPageResults = extractMetadata(searchDoc, cookies)
                results.addAll(firstPageResults)

                // Process remaining pages
                var currentPage = 1
                var nextUrl = getNextPageLink(searchDoc)

                while (nextUrl != null && currentPage < pagesToFetch) {
                    currentPage++
                    statusUpdates.add("Fetching page $currentPage of $pagesToFetch...")

                    val nextPageResponse = Jsoup.connect(nextUrl)
                        .cookies(cookies)
                        .timeout(30000)
                        .execute()

                    // Update cookies
                    cookies.putAll(nextPageResponse.cookies())

                    val nextPageDoc = nextPageResponse.parse()
                    val pageResults = extractMetadata(nextPageDoc, cookies)
                    results.addAll(pageResults)

                    // Get next page URL
                    nextUrl = getNextPageLink(nextPageDoc)
                }

                statusUpdates.add("Completed search with ${results.size} items across $currentPage pages")
            } catch (e: Exception) {
                statusUpdates.add("Error: ${e.message}")
                e.printStackTrace()
            }

            return@withContext results
        }
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

    private fun extractMetadata(doc: Document, cookies: Map<String, String>): List<Pair<String, Boolean>> {
        val results = mutableListOf<Pair<String, Boolean>>()
        val table = doc.select("table").firstOrNull()

        if (table == null) {
            return results
        }

        for (row in table.select("tr").filter { row -> row.text().contains("st") }) {
            try {
                // title
                val titleTag = row.select("a[href][title=null]").firstOrNull()
                    ?: row.select("a[href]:not([title])").firstOrNull()
                val title = titleTag?.text()?.trim() ?: continue

                // Extract the year
                val text = row.text().trim()
                var year = ""  // Default to empty string instead of "No year found"
                if (text.contains("[") && text.contains("]")) {
                    val startIndex = text.indexOf("[") + 1
                    val endIndex = text.indexOf("]", startIndex)
                    if (startIndex < endIndex) {
                        year = text.substring(startIndex, endIndex)
                    }
                }

                val isAvailable = row.select("span.textgruen").isNotEmpty()

                // Extract the link
                val itemLink = titleTag.attr("href")
                val kindOfMediumRaw = row.select("img").firstOrNull()
                val kindOfMedium = kindOfMediumRaw?.attr("title") ?: ""

                val media = LibraryMedia(
                    url = itemLink,
                    isAvailable = isAvailable,
                    year = year,  // Use the empty string if no year found
                    title = title,
                    dueDates = emptyList(),
                    kindOfMedium = kindOfMedium
                )

                results.add(Pair(media.toString(), isAvailable))
            } catch (e: Exception) {
                println("Error processing item: ${e.message}")
                continue
            }
        }

        return results
    }

    private fun findDueDates(html: String): List<String> {
        val results = mutableListOf<String>()
        val doc = Jsoup.parse(html)

        for (row in doc.select("tr")) {
            val textContent = row.text()

            if (textContent.contains("entliehen")) {
                val pattern = Pattern.compile("entliehen.*?(\\d{2}\\.\\d{2}\\.\\d{4})")
                val matcher = pattern.matcher(textContent)

                if (matcher.find()) {
                    val dueDate = matcher.group(1)
                    if (dueDate != null) {
                        results.add(dueDate)
                    }
                }
            }
        }

        return results
    }
}