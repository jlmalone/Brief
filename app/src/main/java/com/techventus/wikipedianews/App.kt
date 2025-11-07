package com.techventus.wikipedianews

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.techventus.wikipedianews.work.NewsWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Brief Application class.
 *
 * Following android-template pattern:
 * - Minimal Application class
 * - @HiltAndroidApp for dependency injection
 * - Timber initialization
 * - WorkManager configuration with Hilt
 * - Background sync scheduling
 */
@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var newsWorkScheduler: NewsWorkScheduler

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Timber in debug builds
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Brief app started")
        }

        // Schedule periodic news sync (every 6 hours)
        newsWorkScheduler.schedulePeriodicSync()
        Timber.d("Background news sync scheduled")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()

    companion object {
        private var instance: App? = null

        @JvmStatic
        fun getInstance(): App? = instance
    }
}
