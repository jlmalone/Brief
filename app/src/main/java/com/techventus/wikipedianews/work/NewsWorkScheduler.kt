package com.techventus.wikipedianews.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler for managing background news sync using WorkManager.
 *
 * Provides methods to:
 * - Schedule periodic news sync
 * - Cancel scheduled sync
 * - Configure sync constraints (network, battery, etc.)
 *
 * Usage:
 * ```
 * // Schedule sync every 6 hours
 * newsWorkScheduler.schedulePeriodicSync(intervalHours = 6)
 *
 * // Cancel scheduled sync
 * newsWorkScheduler.cancelPeriodicSync()
 * ```
 */
@Singleton
class NewsWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule periodic news sync.
     *
     * @param intervalHours Sync interval in hours (minimum 1 hour for periodic work)
     * @param requiresNetwork Whether to require network connectivity (default: true)
     * @param requiresBatteryNotLow Whether to require battery not low (default: false)
     */
    fun schedulePeriodicSync(
        intervalHours: Long = DEFAULT_SYNC_INTERVAL_HOURS,
        requiresNetwork: Boolean = true,
        requiresBatteryNotLow: Boolean = false
    ) {
        Timber.i("Scheduling periodic news sync: every $intervalHours hours")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (requiresNetwork) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(requiresBatteryNotLow)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<NewsSyncWorker>(
            intervalHours,
            TimeUnit.HOURS,
            // Flex interval: allow sync to run in last 15 minutes of interval
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(NewsSyncWorker.TAG)
            .build()

        // Use KEEP policy: if work already scheduled, keep existing schedule
        workManager.enqueueUniquePeriodicWork(
            NewsSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        Timber.d("Periodic news sync scheduled successfully")
    }

    /**
     * Cancel all periodic news sync work.
     */
    fun cancelPeriodicSync() {
        Timber.i("Cancelling periodic news sync")
        workManager.cancelUniqueWork(NewsSyncWorker.WORK_NAME)
    }

    /**
     * Cancel all news sync work (including one-time work).
     */
    fun cancelAllSync() {
        Timber.i("Cancelling all news sync work")
        workManager.cancelAllWorkByTag(NewsSyncWorker.TAG)
    }

    companion object {
        /**
         * Default sync interval: 6 hours
         */
        const val DEFAULT_SYNC_INTERVAL_HOURS = 6L

        /**
         * Minimum sync interval supported by WorkManager: 1 hour
         */
        const val MIN_SYNC_INTERVAL_HOURS = 1L

        /**
         * Available sync interval options
         */
        val SYNC_INTERVAL_OPTIONS = listOf(
            1L,  // 1 hour
            3L,  // 3 hours
            6L,  // 6 hours
            12L, // 12 hours
            24L  // 24 hours (daily)
        )
    }
}
