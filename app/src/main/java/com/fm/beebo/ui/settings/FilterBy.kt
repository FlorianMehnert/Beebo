package com.fm.beebo.ui.settings

enum class FilterBy {
    Alles {
        override fun getSearchRestrictionValue1(): String {
            return ""
        }
        override fun getKindOfMedium(): List<String> {
            return listOf("")
        }
    },
    Bücher {
        override fun getSearchRestrictionValue1(): String {
            return "297"
        }
        override fun getKindOfMedium(): List<String> {
            return listOf("Buch", "eBook")
    }
    },
    Kinderbücher {
        override fun getSearchRestrictionValue1(): String {
            return "299"
        }
        override fun getKindOfMedium(): List<String> {
            return listOf("Kinderbuch")
        }
    },
    Filme {
        override fun getSearchRestrictionValue1(): String {
            return "305"
        }
        override fun getKindOfMedium(): List<String> {
            return listOf("DVD")
        }
    },
    CDs {
        override fun getSearchRestrictionValue1(): String {
            return "303"
        }
        override fun getKindOfMedium(): List<String> {
            return listOf("CD")
        }
    };

    companion object {
        fun iterator(): Iterator<FilterBy> = entries.iterator()
    }

    abstract fun getSearchRestrictionValue1(): String
    abstract fun getKindOfMedium(): List<String>
}