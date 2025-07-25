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
        val BULK_FETCH = booleanPreferencesKey("experimental_feature")
        val SWITCH_TO_BOTTOM_NAVIGATION = booleanPreferencesKey("switch_to_bottom_navigation")
        val ENABLE_OVERVIEW_MAP_KEY = booleanPreferencesKey("enable_overview_map")
        val ENABLE_ANIMATE_TO_MARKER_KEY = booleanPreferencesKey("enable_animate_to_marker")
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

    val bulkFetchEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map {
            preferences ->
            preferences[BULK_FETCH] == true
        }

    val switchToBottomNavigationFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SWITCH_TO_BOTTOM_NAVIGATION] == true
        }

    val enableOverviewMapFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_OVERVIEW_MAP_KEY] == true
        }

    val enableAnimateToMarkerFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_ANIMATE_TO_MARKER_KEY] == true
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

    suspend fun setMaxPages(maxPages: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_PAGES_KEY] = maxPages
        }
    }

    suspend fun setBulkFetch(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BULK_FETCH] = enabled
        }
    }

    suspend fun setSwitchToBottomNavigation(enabled: Boolean){
        context.dataStore.edit { preferences ->
            preferences[SWITCH_TO_BOTTOM_NAVIGATION] = enabled
        }
    }

    suspend fun setEnableOverviewMap(enabled: Boolean){
        context.dataStore.edit { preferences ->
            preferences[ENABLE_OVERVIEW_MAP_KEY] = enabled
        }
    }

    suspend fun setAnimateToMarker(enabled: Boolean){
        context.dataStore.edit { preferences ->
            preferences[ENABLE_ANIMATE_TO_MARKER_KEY] = enabled
        }
    }
}
