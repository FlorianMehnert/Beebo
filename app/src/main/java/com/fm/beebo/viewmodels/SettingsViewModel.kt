package com.fm.beebo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class FilterBy {
    NONE,
    BOOK,
    CHILDREN_BOOK,
    MOVIE,
    CD,
    XBOX;

    companion object {
        fun iterator(): Iterator<FilterBy> = entries.iterator()
    }
}

class SettingsViewModel {
    private val _selectedFilterOption = MutableStateFlow(FilterBy.NONE)
    val selectedFilterOption: StateFlow<FilterBy> = _selectedFilterOption

    val filterOptions: List<String> = FilterBy.values().map { it.name }

    fun setSelectedFilterOption(option: String) {
        _selectedFilterOption.value = FilterBy.valueOf(option)
    }
}
