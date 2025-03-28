package com.fm.beebo.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class FilterBy {
    Alles {
        override fun getSearchRestrictionValue1(): String {
            return ""
        }
    },
    Bücher {
        override fun getSearchRestrictionValue1(): String {
            return "297"
        }
    },
    Kinderbücher {
        override fun getSearchRestrictionValue1(): String {
            return "299"
        }
    },
    Filme {
        override fun getSearchRestrictionValue1(): String {
            return "305"
        }
    },
    CDs {
        override fun getSearchRestrictionValue1(): String {
            return "303"
        }
    };

    companion object {
        fun iterator(): Iterator<FilterBy> = entries.iterator()
    }

    abstract fun getSearchRestrictionValue1(): String
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
