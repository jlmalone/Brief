package com.techventus.wikipedianews

import android.app.Application
import com.techventus.wikipedianews.logging.Logger
import com.techventus.wikipedianews.logging.Toaster
import com.techventus.wikipedianews.manager.PreferencesManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Brief Application class with Hilt dependency injection.
 *
 * Modernization notes:
 * - Added @HiltAndroidApp for dependency injection
 * - Removed static singleton pattern (use Hilt injection instead)
 * - Integrated Timber for logging
 * - Fixed debug flag logic (removed TODO)
 */
@HiltAndroidApp
class WikiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Brief app starting in DEBUG mode")
        }

        // Legacy Logger setup for gradual migration
        Logger.setShowErrorEnabled(BuildConfig.DEBUG)

        if (BuildConfig.DEBUG) {
            PreferencesManager.getInstance().isLoggingEnabled = true
        }

        // Developer option for logging in production
        if (PreferencesManager.getInstance().isLoggingEnabled) {
            Logger.setEnabled(BuildConfig.DEBUG)
            Logger.v("BRIEF", "Legacy logging enabled")
            Timber.v("Legacy logging enabled")
        }

        // Fixed: Enable Toaster only in debug builds (removed duplicate line)
        Toaster.setEnabled(BuildConfig.DEBUG)
    }

    companion object {
        private val TAG = WikiApplication::class.java.simpleName

        val isDebugEnabled: Boolean
            get() = BuildConfig.DEBUG
    }
}
