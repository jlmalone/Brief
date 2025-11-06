package com.techventus.wikipedianews.ui.compose.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techventus.wikipedianews.model.datastore.UserPreferencesDataStore
import com.techventus.wikipedianews.model.repository.NewsRepository
import com.techventus.wikipedianews.work.NewsWorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 *
 * Manages:
 * - User preferences (theme, sync, notifications)
 * - Background sync scheduling
 * - Cache management
 *
 * Uses DataStore for persistent preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val newsWorkScheduler: NewsWorkScheduler,
    private val newsRepository: NewsRepository
) : ViewModel() {

    /**
     * User preferences state
     */
    val userPreferences: StateFlow<UserPreferencesDataStore.UserPreferences> =
        userPreferencesDataStore.userPreferencesFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserPreferencesDataStore.UserPreferences()
            )

    /**
     * Toggle dark theme
     */
    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            Timber.d("Dark theme: $enabled")
            userPreferencesDataStore.setDarkTheme(enabled)
        }
    }

    /**
     * Toggle background sync
     */
    fun toggleBackgroundSync(enabled: Boolean) {
        viewModelScope.launch {
            Timber.d("Background sync: $enabled")
            userPreferencesDataStore.setBackgroundSyncEnabled(enabled)

            if (enabled) {
                // Re-schedule with current interval
                val currentInterval = userPreferences.value.syncIntervalHours
                newsWorkScheduler.schedulePeriodicSync(intervalHours = currentInterval)
            } else {
                // Cancel scheduled work
                newsWorkScheduler.cancelPeriodicSync()
            }
        }
    }

    /**
     * Update sync interval
     */
    fun updateSyncInterval(hours: Long) {
        viewModelScope.launch {
            Timber.d("Sync interval: $hours hours")
            userPreferencesDataStore.setSyncIntervalHours(hours)

            // Reschedule if background sync is enabled
            if (userPreferences.value.isBackgroundSyncEnabled) {
                // Cancel existing work first
                newsWorkScheduler.cancelPeriodicSync()
                // Schedule with new interval
                newsWorkScheduler.schedulePeriodicSync(intervalHours = hours)
            }
        }
    }

    /**
     * Toggle notifications
     */
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            Timber.d("Notifications: $enabled")
            userPreferencesDataStore.setNotificationsEnabled(enabled)
        }
    }

    /**
     * Clear cached news
     */
    fun clearCache() {
        viewModelScope.launch {
            Timber.d("Clearing news cache")
            try {
                newsRepository.clearCache()
                Timber.i("Cache cleared successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear cache")
            }
        }
    }

    /**
     * Reset all settings to defaults
     */
    fun resetSettings() {
        viewModelScope.launch {
            Timber.d("Resetting all settings")
            userPreferencesDataStore.clearPreferences()

            // Reschedule background sync with default settings
            newsWorkScheduler.cancelPeriodicSync()
            newsWorkScheduler.schedulePeriodicSync()
        }
    }
}
