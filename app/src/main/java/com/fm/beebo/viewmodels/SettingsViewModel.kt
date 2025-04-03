package com.fm.beebo.viewmodels

import com.fm.beebo.ui.settings.FilterBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



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
