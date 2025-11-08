package com.techventus.wikipedianews.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.techventus.wikipedianews.model.datastore.UserPreferencesDataStore
import com.techventus.wikipedianews.model.repository.NewsRepository
import com.techventus.wikipedianews.notification.NotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * WorkManager worker for periodically syncing news in the background.
 *
 * This worker uses Hilt for dependency injection via @HiltWorker.
 * It fetches fresh news from the remote source and caches it locally,
 * ensuring users have recent news when they open the app.
 *
 * Optionally shows notifications based on user preferences.
 *
 * Schedule this worker using [NewsWorkScheduler].
 */
@HiltWorker
class NewsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val newsRepository: NewsRepository,
    private val notificationManager: NotificationManager,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("NewsSyncWorker started")

        return try {
            // Get article count before sync
            val previousCount = newsRepository.getCachedNewsCount()

            // Refresh news from remote source
            val result = newsRepository.refreshNews()

            if (result.isSuccess) {
                Timber.i("NewsSyncWorker: Successfully synced news")

                // Get article count after sync
                val newCount = newsRepository.getCachedNewsCount()
                val newArticles = newCount - previousCount

                // Show notification if enabled and there are new articles
                val preferences = userPreferencesDataStore.userPreferencesFlow.first()
                if (preferences.areNotificationsEnabled && newArticles > 0) {
                    notificationManager.showNewsUpdateNotification(newArticles)
                    Timber.d("Showed notification for $newArticles new articles")
                }

                Result.success()
            } else {
                val error = result.exceptionOrNull()
                Timber.w(error, "NewsSyncWorker: Failed to sync news")

                // Show error notification if enabled
                val preferences = userPreferencesDataStore.userPreferencesFlow.first()
                if (preferences.areNotificationsEnabled) {
                    notificationManager.showSyncErrorNotification(
                        error?.message ?: "Failed to sync news"
                    )
                }

                // Retry on failure
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "NewsSyncWorker: Unexpected error during sync")

            // Show error notification
            try {
                val preferences = userPreferencesDataStore.userPreferencesFlow.first()
                if (preferences.areNotificationsEnabled) {
                    notificationManager.showSyncErrorNotification(
                        e.message ?: "Unexpected sync error"
                    )
                }
            } catch (notifError: Exception) {
                Timber.e(notifError, "Failed to show error notification")
            }

            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "news_sync_work"
        const val TAG = "news_sync"
    }
}
