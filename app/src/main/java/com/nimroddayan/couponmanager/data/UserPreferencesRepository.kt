package com.nimroddayan.couponmanager.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by
        preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {
    private val SORT_OPTION = stringPreferencesKey("sort_option")

    val sortOption: Flow<String?> =
            context.userPreferencesDataStore.data.map { preferences -> preferences[SORT_OPTION] }

    suspend fun saveSortOption(sortOption: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[SORT_OPTION] = sortOption
        }
    }
}
