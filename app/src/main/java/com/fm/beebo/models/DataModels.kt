package com.fm.beebo.models

data class LibraryMedia(
    var url: String ="",
    val title: String ="",
    val isAvailable: Boolean =false,
    val year: String = "kein Jahr",
    val dueDates: List<String> = emptyList(),
    val kindOfMedium: String = "",
    val author: String = "",
    val language: String = "",
    val publisher: String = "", // verlag
    val direction: String = "", // regie
    val actors: List<String> = emptyList(),
    val isbn: String = "",
    val availableLibraries: List<String> = emptyList(),
    val unavailableLibraries: MutableList<Pair<String, String>> = mutableListOf<Pair<String, String>>(),
    val orderableLibraries: List<String> = emptyList()
) {
    override fun toString(): String {
        val mediumIcon = when (kindOfMedium) {
            "DVD" -> "📀"
            "Blu-ray Disc" -> "🔵"
            "CD" -> "💿"
            "Buch" -> "📖"
            "Kinderbuch" -> "\uD83E\uDDF8"
            "Videostreaming über Filmfriend" -> "\uD83D\uDCFA"
            "Einzelband einer Serie, siehe auch übergeordnete Titel" -> "📕"
            "Noten" -> "♬"
            "eAudio" -> "\uD83D\uDD09"
            "eBook" -> "\uD83D\uDCD7"
            else -> "❓"
        }

        val availabilityText = if (isAvailable) " ausleihbar" else " nicht_ausleihbar "
        val dueDate = if (!isAvailable && dueDates.isNotEmpty()) dueDates[0] else ""

        return "$year $mediumIcon $title $availabilityText$dueDate"
    }
}
