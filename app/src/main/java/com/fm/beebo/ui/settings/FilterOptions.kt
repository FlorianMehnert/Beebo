package com.fm.beebo.ui.settings

enum class FilterOptions {
    YEAR {
        override fun toString(): String {
            return "Jahr"
        }
    },
    KIND_OF_MEDIUM{
        override fun toString(): String {
            return "Medienart"
        }
    },
    AVAILABLE {
        override fun toString(): String {
            return "Verfügbarkeit"
        }
    },
    DUE_DATE {
        override fun toString(): String {
            return "Verfügbarkeitsdatum"
        }
    };


    companion object {
        fun iterator(): Iterator<FilterOptions> = FilterOptions.entries.iterator()
    }
}