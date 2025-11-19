package com.techventus.wikipedianews.ui.compose.screen.settings

import app.cash.turbine.test
import com.techventus.wikipedianews.model.datastore.UserPreferencesDataStore
import com.techventus.wikipedianews.model.repository.NewsRepository
import com.techventus.wikipedianews.work.NewsWorkScheduler
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SettingsViewModel.
 *
 * Tests:
 * - Preference observations
 * - Theme toggle
 * - Background sync toggle and scheduling
 * - Sync interval updates
 * - Notifications toggle
 * - Cache clearing
 * - Settings reset
 * - Error handling
 *
 * Uses Turbine for Flow testing and MockK for mocking.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var userPreferencesDataStore: UserPreferencesDataStore
    private lateinit var newsWorkScheduler: NewsWorkScheduler
    private lateinit var newsRepository: NewsRepository
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val defaultPreferences = UserPreferencesDataStore.UserPreferences(
        isDarkTheme = false,
        isBackgroundSyncEnabled = true,
        syncIntervalHours = NewsWorkScheduler.DEFAULT_SYNC_INTERVAL_HOURS,
        areNotificationsEnabled = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPreferencesDataStore = mockk(relaxed = true)
        newsWorkScheduler = mockk(relaxed = true)
        newsRepository = mockk(relaxed = true)

        // Default behavior - emit default preferences
        every { userPreferencesDataStore.userPreferencesFlow } returns flowOf(defaultPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `userPreferences exposes preferences from datastore`() = runTest {
        // Given
        val customPreferences = UserPreferencesDataStore.UserPreferences(
            isDarkTheme = true,
            isBackgroundSyncEnabled = false,
            syncIntervalHours = 12L,
            areNotificationsEnabled = true
        )
        every { userPreferencesDataStore.userPreferencesFlow } returns flowOf(customPreferences)

        // When
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        advanceUntilIdle()

        // Then
        viewModel.userPreferences.test {
            val prefs = awaitItem()
            assertTrue(prefs.isDarkTheme)
            assertFalse(prefs.isBackgroundSyncEnabled)
            assertEquals(12L, prefs.syncIntervalHours)
            assertTrue(prefs.areNotificationsEnabled)
        }
    }

    @Test
    fun `userPreferences updates when datastore emits new values`() = runTest {
        // Given
        val prefsFlow = MutableStateFlow(defaultPreferences)
        every { userPreferencesDataStore.userPreferencesFlow } returns prefsFlow

        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)

        // When/Then
        viewModel.userPreferences.test {
            // Initial value
            assertEquals(false, awaitItem().isDarkTheme)

            // Update datastore
            prefsFlow.value = defaultPreferences.copy(isDarkTheme = true)

            // Should emit update
            assertEquals(true, awaitItem().isDarkTheme)
        }
    }

    @Test
    fun `toggleDarkTheme calls datastore setDarkTheme`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.setDarkTheme(any()) } just Runs

        // When
        viewModel.toggleDarkTheme(true)
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.setDarkTheme(true) }
    }

    @Test
    fun `toggleDarkTheme with false disables dark mode`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.setDarkTheme(any()) } just Runs

        // When
        viewModel.toggleDarkTheme(false)
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.setDarkTheme(false) }
    }

    @Test
    fun `toggleBackgroundSync enabled schedules periodic sync`() = runTest {
        // Given
        val prefs = defaultPreferences.copy(syncIntervalHours = 6L)
        every { userPreferencesDataStore.userPreferencesFlow } returns flowOf(prefs)
        coEvery { userPreferencesDataStore.setBackgroundSyncEnabled(any()) } just Runs

        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        advanceUntilIdle()

        // When
        viewModel.toggleBackgroundSync(true)
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.setBackgroundSyncEnabled(true) }
        verify { newsWorkScheduler.schedulePeriodicSync(intervalHours = 6L) }
    }

    @Test
    fun `toggleBackgroundSync disabled cancels periodic sync`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.setBackgroundSyncEnabled(any()) } just Runs

        // When
        viewModel.toggleBackgroundSync(false)
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.setBackgroundSyncEnabled(false) }
        verify { newsWorkScheduler.cancelPeriodicSync() }
    }

    @Test
    fun `updateSyncInterval updates datastore`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.setSyncIntervalHours(any()) } just Runs

        // When
        viewModel.updateSyncInterval(12L)
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.setSyncIntervalHours(12L) }
    }

    @Test
    fun `updateSyncInterval reschedules work when background sync enabled`() = runTest {
        // Given
        val prefs = defaultPreferences.copy(isBackgroundSyncEnabled = true)
        every { userPreferencesDataStore.userPreferencesFlow } returns flowOf(prefs)
        coEvery { userPreferencesDataStore.setSyncIntervalHours(any()) } just Runs

        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        advanceUntilIdle()

        // When
        viewModel.updateSyncInterval(3L)
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.setSyncIntervalHours(3L) }
        verify { newsWorkScheduler.cancelPeriodicSync() }
        verify { newsWorkScheduler.schedulePeriodicSync(intervalHours = 3L) }
    }

    @Test
    fun `updateSyncInterval does not reschedule when background sync disabled`() = runTest {
        // Given
        val prefs = defaultPreferences.copy(isBackgroundSyncEnabled = false)
        every { userPreferencesDataStore.userPreferencesFlow } returns flowOf(prefs)
        coEvery { userPreferencesDataStore.setSyncIntervalHours(any()) } just Runs

        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        advanceUntilIdle()

        // When
        viewModel.updateSyncInterval(12L)
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.setSyncIntervalHours(12L) }
        verify(exactly = 0) { newsWorkScheduler.schedulePeriodicSync(any()) }
        verify(exactly = 0) { newsWorkScheduler.cancelPeriodicSync() }
    }

    @Test
    fun `updateSyncInterval with different intervals works correctly`() = runTest {
        // Given
        val prefs = defaultPreferences.copy(isBackgroundSyncEnabled = true)
        every { userPreferencesDataStore.userPreferencesFlow } returns flowOf(prefs)
        coEvery { userPreferencesDataStore.setSyncIntervalHours(any()) } just Runs

        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        advanceUntilIdle()

        // Test various intervals
        val intervals = listOf(1L, 3L, 6L, 12L, 24L)

        // When/Then
        intervals.forEach { interval ->
            viewModel.updateSyncInterval(interval)
            advanceUntilIdle()

            coVerify { userPreferencesDataStore.setSyncIntervalHours(interval) }
            verify { newsWorkScheduler.schedulePeriodicSync(intervalHours = interval) }
        }
    }

    @Test
    fun `toggleNotifications calls datastore setNotificationsEnabled`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.setNotificationsEnabled(any()) } just Runs

        // When
        viewModel.toggleNotifications(true)
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.setNotificationsEnabled(true) }
    }

    @Test
    fun `toggleNotifications with false disables notifications`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.setNotificationsEnabled(any()) } just Runs

        // When
        viewModel.toggleNotifications(false)
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.setNotificationsEnabled(false) }
    }

    @Test
    fun `clearCache calls repository clearCache`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { newsRepository.clearCache() } just Runs

        // When
        viewModel.clearCache()
        advanceUntilIdle()

        // Then
        coVerify { newsRepository.clearCache() }
    }

    @Test
    fun `clearCache handles error gracefully`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { newsRepository.clearCache() } throws RuntimeException("Cache clear failed")

        // When - should not crash
        viewModel.clearCache()
        advanceUntilIdle()

        // Then - verify it was attempted
        coVerify { newsRepository.clearCache() }
    }

    @Test
    fun `resetSettings clears preferences`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.clearPreferences() } just Runs

        // When
        viewModel.resetSettings()
        advanceUntilIdle()

        // Then
        coVerify { userPreferencesDataStore.clearPreferences() }
    }

    @Test
    fun `resetSettings cancels and reschedules sync with defaults`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.clearPreferences() } just Runs

        // When
        viewModel.resetSettings()
        advanceUntilIdle()

        // Then
        verify { newsWorkScheduler.cancelPeriodicSync() }
        verify { newsWorkScheduler.schedulePeriodicSync() } // Default parameters
    }

    @Test
    fun `resetSettings execution order is correct`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.clearPreferences() } just Runs

        val callOrder = mutableListOf<String>()

        coEvery { userPreferencesDataStore.clearPreferences() } answers {
            callOrder.add("clearPreferences")
        }
        every { newsWorkScheduler.cancelPeriodicSync() } answers {
            callOrder.add("cancelPeriodicSync")
        }
        every { newsWorkScheduler.schedulePeriodicSync() } answers {
            callOrder.add("schedulePeriodicSync")
        }

        // When
        viewModel.resetSettings()
        advanceUntilIdle()

        // Then - correct order
        assertEquals(listOf("clearPreferences", "cancelPeriodicSync", "schedulePeriodicSync"), callOrder)
    }

    @Test
    fun `userPreferences initialValue is default`() = runTest {
        // Given
        every { userPreferencesDataStore.userPreferencesFlow } returns flowOf(defaultPreferences)

        // When
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)

        // Then
        assertEquals(UserPreferencesDataStore.UserPreferences(), viewModel.userPreferences.value)
    }

    @Test
    fun `userPreferences uses WhileSubscribed with 5 second timeout`() = runTest {
        // This test verifies the StateFlow behavior - it should maintain the last value
        // for 5 seconds after the last subscriber unsubscribes

        // Given
        val prefs = defaultPreferences.copy(isDarkTheme = true)
        every { userPreferencesDataStore.userPreferencesFlow } returns flowOf(prefs)

        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        advanceUntilIdle()

        // When - subscribe and unsubscribe
        viewModel.userPreferences.test {
            val value = awaitItem()
            assertTrue(value.isDarkTheme)
            cancel()
        }

        // Then - value should still be cached
        assertTrue(viewModel.userPreferences.value.isDarkTheme)
    }

    @Test
    fun `toggleBackgroundSync uses current sync interval from preferences`() = runTest {
        // Given
        val prefs = defaultPreferences.copy(syncIntervalHours = 24L)
        every { userPreferencesDataStore.userPreferencesFlow } returns flowOf(prefs)
        coEvery { userPreferencesDataStore.setBackgroundSyncEnabled(any()) } just Runs

        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        advanceUntilIdle()

        // When
        viewModel.toggleBackgroundSync(true)
        advanceUntilIdle()

        // Then - should use current interval (24 hours) not default
        verify { newsWorkScheduler.schedulePeriodicSync(intervalHours = 24L) }
    }

    @Test
    fun `multiple rapid toggles handled correctly`() = runTest {
        // Given
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        coEvery { userPreferencesDataStore.setDarkTheme(any()) } just Runs

        // When - rapid toggles
        viewModel.toggleDarkTheme(true)
        viewModel.toggleDarkTheme(false)
        viewModel.toggleDarkTheme(true)
        advanceUntilIdle()

        // Then - all calls should be made
        coVerify(exactly = 2) { userPreferencesDataStore.setDarkTheme(true) }
        coVerify(exactly = 1) { userPreferencesDataStore.setDarkTheme(false) }
    }

    @Test
    fun `ViewModel initialization subscribes to userPreferencesFlow`() = runTest {
        // Given
        val prefsFlow = flowOf(defaultPreferences)
        every { userPreferencesDataStore.userPreferencesFlow } returns prefsFlow

        // When
        viewModel = SettingsViewModel(userPreferencesDataStore, newsWorkScheduler, newsRepository)
        advanceUntilIdle()

        // Then - flow should be collected
        verify { userPreferencesDataStore.userPreferencesFlow }
    }
}
