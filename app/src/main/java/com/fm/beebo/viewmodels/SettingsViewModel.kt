package com.fm.beebo.viewmodels

import com.fm.beebo.ui.settings.FilterBy
import com.fm.beebo.ui.settings.FilterOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Year


class SettingsViewModel {
    private val _selectedFilterOption = MutableStateFlow(FilterBy.Alles)
    private val _appStart = MutableStateFlow(false)
    private val _selectedYear = MutableStateFlow(2025)
    private val _sortBy = MutableStateFlow(Pair(FilterOptions.YEAR, true))
    private val _selectedMediaTypes = MutableStateFlow<List<String>>(emptyList())
    private val _availabilityFilter = MutableStateFlow(false) // false = show all, true = only available
    private val _dueDateFilter = MutableStateFlow("Alle")
    private var _selectedYearRange = MutableStateFlow(Pair<Int, Int>(2020, 2025))
    private var _minYear = MutableStateFlow(1800)
    private var _maxYear = MutableStateFlow(2025)

    val selectedFilterOption: StateFlow<FilterBy> = _selectedFilterOption
    val appStart: StateFlow<Boolean> = _appStart
    val selectedYear: StateFlow<Int> = _selectedYear
    val sortBy: StateFlow<Pair<FilterOptions, Boolean>> = _sortBy
    val selectedMediaTypes: StateFlow<List<String>> = _selectedMediaTypes
    val availabilityFilter: StateFlow<Boolean> = _availabilityFilter
    val dueDateFilter: StateFlow<String> = _dueDateFilter
    val selectedYearRange: StateFlow<Pair<Int, Int>> = _selectedYearRange
    val minYear: StateFlow<Int> = _minYear
    val maxYear: StateFlow<Int> = _maxYear

    val kindOfMediumList: List<String> = FilterBy.entries.map { it.name }

    fun setSelectedFilterOption(option: Int) {
        _selectedFilterOption.value = FilterBy.entries.toTypedArray()[option]

        // When filter option changes, update selectedMediaTypes with default values from the enum
        if (_selectedFilterOption.value.getKindOfMedium().isNotEmpty()) {
            _selectedMediaTypes.value = _selectedFilterOption.value.getKindOfMedium()
        }
    }

    fun selectFilterByMediaType(mediaType: String) {
        val filterOption = FilterBy.entries.find { it.getKindOfMedium().contains(mediaType) }
        if (filterOption != null) {
            _selectedFilterOption.value = filterOption
        }
    }

    fun setSelectedYear(year: Int) {
        _selectedYear.value = year
    }

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

    fun setAvailabilityFilter(onlyAvailable: Boolean) {
        _availabilityFilter.value = onlyAvailable
    }

    fun setDueDateFilter(option: String) {
        _dueDateFilter.value = option
    }

    fun setSelectedYearRange(option: Pair<Int, Int>) {
        _selectedYearRange.value = option
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

    fun onAppStart() {
        _appStart.value = true
    }

    fun getAppliedFilters(): Map<String, Any> {
        val filters = mutableMapOf<String, Any>()

        when (_sortBy.value.first) {
            FilterOptions.YEAR -> filters["year"] = _selectedYear.value
            FilterOptions.KIND_OF_MEDIUM -> filters["mediaTypes"] = _selectedMediaTypes.value
            FilterOptions.AVAILABLE -> filters["onlyAvailable"] = _availabilityFilter.value
            FilterOptions.DUE_DATE -> filters["dueDate"] = _dueDateFilter.value
        }

        return filters
    }
}
