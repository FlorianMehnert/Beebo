package com.fm.beebo.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.searchHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")

@Serializable
data class SearchHistoryItem(
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

class SearchHistoryManager(private val context: Context) {
    private val searchHistoryKey = stringPreferencesKey("search_history")
    private val maxHistorySize = 10 // Maximum number of search terms to keep

    val searchHistory: Flow<List<SearchHistoryItem>> = context.searchHistoryDataStore.data
        .map { preferences ->
            val historyJson = preferences[searchHistoryKey] ?: "[]"
            try {
                Json.decodeFromString<List<SearchHistoryItem>>(historyJson)
                    .sortedByDescending { it.timestamp } // Most recent first
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun addSearchTerm(query: String) {
        if (query.isBlank()) return

        context.searchHistoryDataStore.edit { preferences ->
            val currentHistoryJson = preferences[searchHistoryKey] ?: "[]"
            val currentHistory = try {
                Json.decodeFromString<List<SearchHistoryItem>>(currentHistoryJson)
            } catch (e: Exception) {
                emptyList()
            }

            // Remove any existing entry with the same query (case-insensitive)
            val filteredHistory = currentHistory.filter {
                !it.query.equals(query, ignoreCase = true)
            }

            // Add new search term at the beginning
            val newHistory = listOf(SearchHistoryItem(query)) + filteredHistory

            // Keep only the most recent items
            val trimmedHistory = newHistory.take(maxHistorySize)

            preferences[searchHistoryKey] = Json.encodeToString(trimmedHistory)
        }
    }

    suspend fun clearHistory() {
        context.searchHistoryDataStore.edit { preferences ->
            preferences[searchHistoryKey] = "[]"
        }
    }

    suspend fun removeSearchTerm(query: String) {
        context.searchHistoryDataStore.edit { preferences ->
            val currentHistoryJson = preferences[searchHistoryKey] ?: "[]"
            val currentHistory = try {
                Json.decodeFromString<List<SearchHistoryItem>>(currentHistoryJson)
            } catch (e: Exception) {
                emptyList()
            }

            val filteredHistory = currentHistory.filter {
                !it.query.equals(query, ignoreCase = true)
            }

            preferences[searchHistoryKey] = Json.encodeToString(filteredHistory)
        }
    }
}