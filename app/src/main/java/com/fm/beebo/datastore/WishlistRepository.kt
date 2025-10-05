package com.fm.beebo.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.ui.settings.mediaFromString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class WishlistRepository(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "wishlist_preferences")

    private val WISHLIST_KEY = stringSetPreferencesKey("wishlist_items")
    private val json = Json { ignoreUnknownKeys = true }

    val wishlistFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[WISHLIST_KEY] ?: emptySet()
        }

    suspend fun addToWishlist(itemId: String) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[WISHLIST_KEY]?.toMutableSet() ?: mutableSetOf()
            currentSet.add(itemId)
            preferences[WISHLIST_KEY] = currentSet
        }
    }

    suspend fun removeFromWishlist(itemId: String) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[WISHLIST_KEY]?.toMutableSet() ?: mutableSetOf()
            currentSet.remove(itemId)
            preferences[WISHLIST_KEY] = currentSet
            // Also remove the item details
            preferences.remove(stringPreferencesKey("item_$itemId"))
        }
    }

    suspend fun saveItemDetails(itemId: String, item: LibraryMedia) {
        context.dataStore.edit { preferences ->
            // Create a simple serializable format
            val itemData = "${item.title}|||${item.year}|||${item.kindOfMedium.name}|||${item.author}|||${item.url}|||${item.isAvailable}"
            preferences[stringPreferencesKey("item_$itemId")] = itemData
        }
    }

    suspend fun getWishlistItemsDetails(): List<LibraryMedia> {
        val preferences = context.dataStore.data.first()
        val wishlistIds = preferences[WISHLIST_KEY] ?: emptySet()

        return wishlistIds.mapNotNull { id ->
            val itemData = preferences[stringPreferencesKey("item_$id")]
            itemData?.let {
                val parts = it.split("|||")
                if (parts.size >= 6) {
                    LibraryMedia(
                        title = parts[0],
                        year = parts[1],
                        kindOfMedium = mediaFromString(parts[2]),
                        author = parts[3],
                        url = parts[4],
                        isAvailable = parts[5].toBoolean()
                    )
                } else null
            }
        }
    }
}
