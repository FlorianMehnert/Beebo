package com.fm.beebo.models

import com.fm.beebo.ui.settings.Media

data class LibraryMedia(
    var url: String ="",
    val title: String ="",
    val isAvailable: Boolean =false,
    val year: String = "kein Jahr",
    val dueDates: List<String> = emptyList(),
    val kindOfMedium: Media = Media.Other,
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
    fun getIsAvailable(): String {
        return if (isAvailable) " ausleihbar" else " nicht_ausleihbar "
    }

    fun cleanedTitle(): String {
        return title.replace("¬", "")
    }
}
