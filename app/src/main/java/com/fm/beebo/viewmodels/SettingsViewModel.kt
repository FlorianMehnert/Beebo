package com.fm.beebo.viewmodels

import com.fm.beebo.ui.settings.FilterOptions
import com.fm.beebo.ui.settings.Media
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class SettingsViewModel {
    // Private MutableStateFlows
    private val _appStart = MutableStateFlow(false)
    private val _sortBy = MutableStateFlow(Pair(FilterOptions.YEAR, true))
    private val _selectedMediaTypes = MutableStateFlow<List<Media>>(emptyList())
    private val _dueDateFilter = MutableStateFlow<LocalDate?>(null)
    private val _filterByTimeSpan = MutableStateFlow(false)
    private val _minYear = MutableStateFlow(2000)
    private val _maxYear = MutableStateFlow(2025)
    private val _hasFilters = MutableStateFlow(false)
    private val _activeFiltersCount = MutableStateFlow(0)
    private val _isHoldingReset = MutableStateFlow(false)

    // Public StateFlows
    val appStart: StateFlow<Boolean> = _appStart
    val sortBy: StateFlow<Pair<FilterOptions, Boolean>> = _sortBy
    val selectedMediaTypes: StateFlow<List<Media>> = _selectedMediaTypes
    val dueDateFilter: StateFlow<LocalDate?> = _dueDateFilter
    val filterByTimeSpan: StateFlow<Boolean> = _filterByTimeSpan
    val minYear: StateFlow<Int> = _minYear
    val maxYear: StateFlow<Int> = _maxYear
    val hasFilters: StateFlow<Boolean> = _hasFilters


    // Default values - make these configurable if needed
    private val defaultMinYear = 2000
    private val defaultMaxYear = 2025
    private val defaultSortOption = FilterOptions.YEAR
    private val defaultSortDirection = true


    fun setSortBy(option: FilterOptions, value: Boolean) {
        _sortBy.value = Pair(option, value)
        // Only consider sorting a filter if it's not the default sort
        updateFilterState()
    }

    fun toggleMediaType(mediaType: Media) {
        val currentList = _selectedMediaTypes.value.toMutableList()
        if (currentList.contains(mediaType)) {
            currentList.remove(mediaType)
        } else {
            currentList.add(mediaType)
        }
        _selectedMediaTypes.value = currentList
        updateFilterState()
    }

    fun setDueDateFilter(date: LocalDate?) {
        _dueDateFilter.value = date
        updateFilterState()
    }

    fun setMinYear(year: Int) {
        _minYear.value = year
        updateFilterState()
    }

    fun setMaxYear(year: Int) {
        _maxYear.value = year
        updateFilterState()
    }

    fun setAppStart(option: Boolean) {
        _appStart.value = option
    }

    fun setFilterByTimeSpan(option: Boolean) {
        _filterByTimeSpan.value = option
        updateFilterState()
    }

    fun resetFilters() {
        _dueDateFilter.value = null
        _minYear.value = defaultMinYear
        _maxYear.value = defaultMaxYear
        _filterByTimeSpan.value = false
        _selectedMediaTypes.value = emptyList()
        _sortBy.value = Pair(defaultSortOption, defaultSortDirection)
        _hasFilters.value = false
        _activeFiltersCount.value = 0
    }

    fun onAppStart() {
        _appStart.value = true
    }

    /**
     * Centralized method to determine if any filters are active and update
     * the filter state accordingly
     */
    private fun updateFilterState() {
        var count = 0

        // Check media type filters
        if (_selectedMediaTypes.value.isNotEmpty()) {
            count++
        }

        // Check date filter
        if (_dueDateFilter.value != null) {
            count++
        }

        // Check year range filters - only count if they differ from defaults
        if (_minYear.value != defaultMinYear) {
            count++
        }

        if (_maxYear.value != defaultMaxYear) {
            count++
        }

        // Check timespan filter
        if (_filterByTimeSpan.value) {
            count++
        }

        // Update the filter states
        _activeFiltersCount.value = count
        _hasFilters.value = count > 0
    }
}