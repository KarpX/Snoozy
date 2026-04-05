package com.wem.snoozy.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(
    private val context: Context
) {

    companion object {
        val SLEEP_START_TIME = stringPreferencesKey("sleep_start_time")
        val CYCLE_LENGTH = stringPreferencesKey("cycle_length")
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val USER_ID = intPreferencesKey("user_id")
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

    val accessTokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    val userIdFlow: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]
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

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = token
        }
    }

    suspend fun saveUserId(userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    suspend fun clearAccessToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(USER_ID)
        }
    }

    suspend fun hasToken(): Boolean {
        return accessTokenFlow.first() != null
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
