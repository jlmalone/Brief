package com.techventus.wikipedianews.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.techventus.wikipedianews.model.repository.NewsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * WorkManager worker for periodically syncing news in the background.
 *
 * This worker uses Hilt for dependency injection via @HiltWorker.
 * It fetches fresh news from the remote source and caches it locally,
 * ensuring users have recent news when they open the app.
 *
 * Schedule this worker using [NewsWorkScheduler].
 */
@HiltWorker
class NewsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val newsRepository: NewsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("NewsSyncWorker started")

        return try {
            // Refresh news from remote source
            val result = newsRepository.refreshNews()

            if (result.isSuccess) {
                Timber.i("NewsSyncWorker: Successfully synced news")
                Result.success()
            } else {
                val error = result.exceptionOrNull()
                Timber.w(error, "NewsSyncWorker: Failed to sync news")
                // Retry on failure
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "NewsSyncWorker: Unexpected error during sync")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "news_sync_work"
        const val TAG = "news_sync"
    }
}
