package com.fm.beebo.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings_prefs")

class SettingsDataStore(private val context: Context) {

    companion object {
        val ENABLE_DEFAULT_SEARCH_TERM_KEY = booleanPreferencesKey("enable_default_search_term")
        val DEFAULT_SEARCH_TERM_KEY = stringPreferencesKey("default_search_term")
        val MAX_PAGES_KEY = intPreferencesKey("max_pages_term")
    }

    val enableDefaultSearchTermFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_DEFAULT_SEARCH_TERM_KEY] == true
        }

    val defaultSearchTermFlow: Flow<String> = context.dataStore.data
        .map {
            preferences ->
            preferences[DEFAULT_SEARCH_TERM_KEY] ?: ""
        }

    val maxPagesFlow: Flow<Int> = context.dataStore.data
        .map {
                preferences ->
            preferences[MAX_PAGES_KEY] ?: 3
        }

    suspend fun enableDefaultSearchTerm(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_DEFAULT_SEARCH_TERM_KEY] = enabled
        }
    }

    suspend fun setDefaultSearchTerm(searchTerm: String) {
        println("set search term:" + searchTerm)
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_SEARCH_TERM_KEY] = searchTerm
        }
    }

    suspend fun setMaxPages(maxPages: Int) {
        println("set max pages to:" + maxPages)
        context.dataStore.edit { preferences ->
            preferences[MAX_PAGES_KEY] = maxPages
        }
    }
}
