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

    suspend fun search(searchTerm: String, maxPages: Int = 3): Pair<List<LibraryMedia>, Map<String, String>> {
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
                val searchUrl = "$BASE_LOGGED_IN_URL/webOPACClient/search.do?methodToCall=submit&CSId=$csid&methodToCallParameter=submitSearch"

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

                // Determine total pages
                val totalPages = getMaxPages(searchDoc)

                // Limit to specified max pages
                val pagesToFetch = minOf(totalPages, maxPages)

                // Process first page
                val firstPageResults = extractMetadata(searchDoc)
                results.addAll(firstPageResults)

                // Process remaining pages
                var currentPage = 1
                var nextUrl = getNextPageLink(searchDoc)

                while (nextUrl != null && currentPage < pagesToFetch) {
                    currentPage++

                    val nextPageResponse = Jsoup.connect(nextUrl)
                        .cookies(cookies)
                        .timeout(30000)
                        .execute()

                    // Update cookies
                    cookies.putAll(nextPageResponse.cookies())

                    val nextPageDoc = nextPageResponse.parse()
                    val pageResults = extractMetadata(nextPageDoc)
                    results.addAll(pageResults)

                    // Get next page URL
                    nextUrl = getNextPageLink(nextPageDoc)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext Pair(results, cookies)
        }
    }


    suspend fun getItemDetails(itemUrl: String, cookies: Map<String, String>): LibraryMedia? {
        return withContext(Dispatchers.IO) {
            println(cookies)
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

            // Extract title
            val title = doc.select("h1").text()

            // Extract authors
            val authors = doc.select("div.teaser li").eachText().filter { it.contains("Regie:") || it.contains("Drehb.:") }

            // Extract description
            val description = doc.select("div.teaser").text()

            // Extract additional info
            val additionalInfo = doc.select("ul.teaser").text()

            // Extract availability and due dates
            val isAvailable = doc.select("span.textgruen").isNotEmpty()
            val dueDates = doc.select("td:contains(entliehen bis)").eachText().map { it.replace("entliehen bis ", "") }

            // Extract year
            val year = doc.select("div.teaser").text().let {
                val yearPattern = "\\b(20\\d{2})\\b".toRegex()
                val yearMatch = yearPattern.find(it)
                yearMatch?.value ?: "kein Jahr"
            }

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
                authors = authors,
                description = description,
                additionalInfo = additionalInfo
            )
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

}
