package com.fm.beebo.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class FilterBy {
    Alles,
    Bücher,
    Kinderbücher,
    Filme,
    CDs;

    companion object {
        fun iterator(): Iterator<FilterBy> = entries.iterator()
    }
}

class SettingsViewModel {
    private val _selectedFilterOption = MutableStateFlow(FilterBy.Alles)
    val selectedFilterOption: StateFlow<FilterBy> = _selectedFilterOption

    val filterOptions: List<String> = FilterBy.entries.map { it.name }

    fun setSelectedFilterOption(option: Int) {
        println("has the option:" + option)
        _selectedFilterOption.value = FilterBy.entries.toTypedArray()[option]
        println(_selectedFilterOption)
    }
}
