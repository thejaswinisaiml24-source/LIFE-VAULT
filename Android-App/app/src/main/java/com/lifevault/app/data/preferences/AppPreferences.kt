package com.lifevault.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lifevault_prefs")

/**
 * Local device preferences: theme choice and PIN lock. These are NOT
 * synced to Firestore — they're per-device settings, same as any
 * standard Android app's local preferences.
 */
class AppPreferences(private val context: Context) {

    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
        private val PIN_HASH_KEY = stringPreferencesKey("pin_hash")
        private val PIN_LOCK_ENABLED_KEY = booleanPreferencesKey("pin_lock_enabled")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_THEME_KEY] ?: true // LifeVault defaults to dark, per original design
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[DARK_THEME_KEY] = enabled }
    }

    val pinHash: Flow<String?> = context.dataStore.data.map { it[PIN_HASH_KEY] }
    val isPinLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[PIN_LOCK_ENABLED_KEY] ?: false }

    suspend fun setPin(hash: String) {
        context.dataStore.edit {
            it[PIN_HASH_KEY] = hash
            it[PIN_LOCK_ENABLED_KEY] = true
        }
    }

    suspend fun disablePinLock() {
        context.dataStore.edit {
            it.remove(PIN_HASH_KEY)
            it[PIN_LOCK_ENABLED_KEY] = false
        }
    }
}
