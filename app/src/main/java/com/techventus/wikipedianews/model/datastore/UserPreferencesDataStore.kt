package com.techventus.wikipedianews.model.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.techventus.wikipedianews.work.NewsWorkScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore for managing user preferences.
 *
 * Provides type-safe access to user settings:
 * - Theme preference (dark mode)
 * - Background sync settings
 * - Notification preferences
 *
 * Uses Kotlin Flow for reactive updates.
 */
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    /**
     * User preferences data class
     */
    data class UserPreferences(
        val isDarkTheme: Boolean = false,
        val isBackgroundSyncEnabled: Boolean = true,
        val syncIntervalHours: Long = NewsWorkScheduler.DEFAULT_SYNC_INTERVAL_HOURS,
        val areNotificationsEnabled: Boolean = false
    )

    /**
     * Observe user preferences as Flow
     */
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                isDarkTheme = preferences[Keys.IS_DARK_THEME] ?: false,
                isBackgroundSyncEnabled = preferences[Keys.IS_BACKGROUND_SYNC_ENABLED] ?: true,
                syncIntervalHours = preferences[Keys.SYNC_INTERVAL_HOURS] ?: NewsWorkScheduler.DEFAULT_SYNC_INTERVAL_HOURS,
                areNotificationsEnabled = preferences[Keys.ARE_NOTIFICATIONS_ENABLED] ?: false
            )
        }

    /**
     * Update dark theme preference
     */
    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_DARK_THEME] = enabled
        }
    }

    /**
     * Update background sync enabled preference
     */
    suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_BACKGROUND_SYNC_ENABLED] = enabled
        }
    }

    /**
     * Update sync interval preference
     */
    suspend fun setSyncIntervalHours(hours: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SYNC_INTERVAL_HOURS] = hours
        }
    }

    /**
     * Update notifications enabled preference
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ARE_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Clear all preferences (reset to defaults)
     */
    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val IS_BACKGROUND_SYNC_ENABLED = booleanPreferencesKey("is_background_sync_enabled")
        val SYNC_INTERVAL_HOURS = longPreferencesKey("sync_interval_hours")
        val ARE_NOTIFICATIONS_ENABLED = booleanPreferencesKey("are_notifications_enabled")
    }
}
