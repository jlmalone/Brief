package com.techventus.wikipedianews

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Brief Application class.
 *
 * Following android-template pattern:
 * - Minimal Application class
 * - Only @HiltAndroidApp annotation
 * - Timber initialization only
 * - No business logic
 */
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber in debug builds
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Brief app started")
        }
    }
}
