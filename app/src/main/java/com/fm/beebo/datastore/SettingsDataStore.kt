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

    suspend fun enableDefaultSearchTerm(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_DEFAULT_SEARCH_TERM_KEY] = enabled
        }
    }

    suspend fun setDefaultSearchTerm(searchTerm: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_SEARCH_TERM_KEY] = searchTerm
        }
    }
}
