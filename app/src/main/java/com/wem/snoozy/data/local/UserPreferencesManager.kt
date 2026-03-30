package com.wem.snoozy.data.local

import android.content.Context
import androidx.core.text.isDigitsOnly
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(
    private val context: Context
) {

    companion object {

        val SLEEP_START_TIME = stringPreferencesKey("sleep_start_time")
        val CYCLE_LENGTH = stringPreferencesKey("cycle_length")
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }

    val sleepStartTimeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SLEEP_START_TIME] ?: "0"
        }

    val cycleLengthFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[CYCLE_LENGTH] ?: "90"
        }

    val darkThemeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_THEME_KEY] ?: true
        }

    suspend fun saveSleepStartTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[SLEEP_START_TIME] = time
        }
    }

    suspend fun saveCycleLength(value: String) {
        context.dataStore.edit { preferences ->
            preferences[CYCLE_LENGTH] = value
        }
    }

    suspend fun saveTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDark
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}