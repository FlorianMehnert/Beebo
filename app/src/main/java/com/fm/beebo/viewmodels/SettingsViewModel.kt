package com.fm.beebo.viewmodels

import com.fm.beebo.ui.settings.FilterBy
import com.fm.beebo.ui.settings.FilterOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class SettingsViewModel {
    private val _selectedFilterOption = MutableStateFlow(FilterBy.Alles)
    private val _appStart = MutableStateFlow(false) // Initially set to false
    val selectedFilterOption: StateFlow<FilterBy> = _selectedFilterOption
    val appStart: StateFlow<Boolean> = _appStart

    val kindOfMediumList: List<String> = FilterBy.entries.map { it.name }
    val selectedYear: Flow<Int> = MutableStateFlow(2025)
    val sortBy: Flow<Pair<FilterOptions, Boolean>> = MutableStateFlow(Pair(FilterOptions.YEAR, true))

    fun setSelectedFilterOption(option: Int) {
        _selectedFilterOption.value = FilterBy.entries.toTypedArray()[option]
    }

    fun setSelectedYear(year: Int) {
        (selectedYear as MutableStateFlow<Int>).value = year
    }

    fun setSortBy(option: FilterOptions, value: Boolean){
        (sortBy as MutableStateFlow<Pair<FilterOptions, Boolean>>).value = Pair(option, value)
    }

    fun setAppStart(option: Boolean) {
        _appStart.value = option
    }

    // Method to set appStart to true
    fun onAppStart() {
        _appStart.value = true
    }
}
