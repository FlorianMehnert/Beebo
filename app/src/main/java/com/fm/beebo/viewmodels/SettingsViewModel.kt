package com.fm.beebo.viewmodels

import com.fm.beebo.ui.settings.FilterBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class SettingsViewModel {
    private val _selectedFilterOption = MutableStateFlow(FilterBy.Alles)
    private val _appStart = MutableStateFlow(false) // Initially set to false
    val selectedFilterOption: StateFlow<FilterBy> = _selectedFilterOption
    val appStart: StateFlow<Boolean> = _appStart

    val filterOptions: List<String> = FilterBy.entries.map { it.name }

    fun setSelectedFilterOption(option: Int) {
        _selectedFilterOption.value = FilterBy.entries.toTypedArray()[option]
    }

    fun setAppStart(option: Boolean) {
        _appStart.value = option
    }

    // Method to set appStart to true
    fun onAppStart() {
        _appStart.value = true
    }
}
