package com.fm.beebo.viewmodels

import com.fm.beebo.ui.settings.FilterBy
import com.fm.beebo.ui.settings.FilterOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate


class SettingsViewModel {
    private val _selectedFilterOption = MutableStateFlow(FilterBy.Alles)
    private val _appStart = MutableStateFlow(false)
    private val _sortBy = MutableStateFlow(Pair(FilterOptions.YEAR, true))
    private val _selectedMediaTypes = MutableStateFlow<List<String>>(emptyList())
    private val _dueDateFilter = MutableStateFlow<LocalDate?>(null)
    private val _filterByTimeSpan = MutableStateFlow<Boolean>(false)
    private var _minYear = MutableStateFlow(2000)
    private var _maxYear = MutableStateFlow(2025)

    val selectedFilterOption: StateFlow<FilterBy> = _selectedFilterOption
    val appStart: StateFlow<Boolean> = _appStart
    val sortBy: StateFlow<Pair<FilterOptions, Boolean>> = _sortBy
    val selectedMediaTypes: StateFlow<List<String>> = _selectedMediaTypes
    val dueDateFilter: StateFlow<LocalDate?> = _dueDateFilter
    val filterByTimeSpan: StateFlow<Boolean> = _filterByTimeSpan
    val minYear: StateFlow<Int> = _minYear
    val maxYear: StateFlow<Int> = _maxYear

    fun setSortBy(option: FilterOptions, value: Boolean) {
        _sortBy.value = Pair(option, value)
    }

    fun toggleMediaType(mediaType: String) {
        val currentList = _selectedMediaTypes.value.toMutableList()
        if (currentList.contains(mediaType)) {
            currentList.remove(mediaType)
        } else {
            currentList.add(mediaType)
        }
        _selectedMediaTypes.value = currentList
    }

    fun setDueDateFilter(date: LocalDate?) {
        _dueDateFilter.value = date
    }

    fun setMinYear(year: Int){
        _minYear.value = year
    }

    fun setMaxYear(year: Int){
        _maxYear.value = year
    }

    fun setAppStart(option: Boolean) {
        _appStart.value = option
    }

    fun setFilterByTimeSpan(option: Boolean) {
        _filterByTimeSpan.value = option
    }

    fun onAppStart() {
        _appStart.value = true
    }
}
