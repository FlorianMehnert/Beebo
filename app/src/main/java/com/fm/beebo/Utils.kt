package com.fm.beebo

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileWriter
import java.io.IOException


/**
 * Returns (title, year, author) for one wishlist row.
 * Works for CDs, eVideos, books, DVDs, … – anything that the SBD catalogue puts
 * into its “Merkliste”.
 */
fun parseTitleBlock(cell: Element, fallbackIndex: Int): Triple<String, String, String> {
    // Get all text nodes and br-separated content
    val lines = cell.html()
        .split(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE))  // Split on <br> or <br/>
        .map { fragment ->
            val parsed = Jsoup.parse(fragment).text().trim()
            parsed
        }
        .filter { it.isNotBlank() }
        .filterNot { it.contains("Verfügbarkeit", ignoreCase = true) }

    val yearRegex = "^\\[?(\\d{4})]?$".toRegex()
    val authorRegex = "(.+?)\\s*Â¬?\\[Verfasser]".toRegex()

    var title = "Unknown Title $fallbackIndex"
    var year = ""
    var author = ""
    var titleFound = false
    for ((_, line) in lines.withIndex()) {
        when {
            // Check for year pattern
            yearRegex.matches(line) -> {
                year = yearRegex.find(line)!!.groupValues[1]
            }
            // Check for author pattern
            authorRegex.containsMatchIn(line) -> {
                author = authorRegex.find(line)?.groupValues?.get(1)?.trim() ?: ""
            }
            // First non-special line is the title
            !titleFound && line.isNotEmpty() -> {
                // Remove leading special characters
                title = line.removePrefix("Â¬").removePrefix("¬").trim()
                titleFound = true
            }
        }
    }
    val result = Triple(title, year, author)
    return result
}

fun logToFile(message: String) {
    try {
        val logFile = File(MainActivity.appContext.filesDir, "wishlist_log.txt")
        FileWriter(logFile, true).use { writer ->
            writer.appendLine("${System.currentTimeMillis()}: $message")
        }
    } catch (e: IOException) {
    }
}