package com.fm.beebo.models

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class LibraryMedia(
    val url: String,
    val isAvailable: Boolean,
    val year: String = "kein Jahr",
    val title: String,
    val dueDates: List<String>,
    val kindOfMedium: String = ""
) {
    override fun toString(): String {
        val mediumIcon = when (kindOfMedium) {
            "DVD" -> "📀"
            "Blu-ray Disc" -> "🔵^"
            "CD" -> "💿"
            "Buch" -> "📖"
            else -> "❓"
        }

        val availabilityText = if (isAvailable) " ausleihbar" else " nicht_ausleihbar "
        val dueDate = if (!isAvailable && dueDates.isNotEmpty()) dueDates[0] else ""

        return "$year $mediumIcon $title $availabilityText$dueDate"
    }
}


fun parseLibraryItemDetails(html: String): LibraryItemDetails {
    val document: Document = Jsoup.parse(html)

    val title = document.select("h1").text()
    val director = document.select("td:contains(Regie:)").next().text()
    val releaseYear = document.select("td:contains(Orig.:)").next().text().split(",").last().trim()
    val availability = document.select("td:contains(Status)").next().text()
    val dueDate = if (availability.contains("entliehen bis")) {
        availability.split("entliehen bis")[1].trim()
    } else {
        ""
    }

    return LibraryItemDetails(title, director, releaseYear, availability, dueDate)
}

data class LibraryItemDetails(
    val title: String,
    val director: String,
    val releaseYear: String,
    val availability: String,
    val dueDate: String
)
