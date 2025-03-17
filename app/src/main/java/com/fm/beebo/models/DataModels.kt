package com.fm.beebo.models

data class LibraryMedia(
    val url: String,
    val isAvailable: Boolean,
    val year: String = "kein Jahr",
    val title: String,
    val dueDates: List<String>,
    val kindOfMedium: String = "",
    val authors: List<String> = emptyList(),
    val description: String = "",
    val additionalInfo: String = ""
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
