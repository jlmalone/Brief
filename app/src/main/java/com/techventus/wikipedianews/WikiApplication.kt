package com.techventus.wikipedianews

import android.app.Application
import com.techventus.wikipedianews.logging.Logger
import com.techventus.wikipedianews.logging.Toaster
import com.techventus.wikipedianews.manager.PreferencesManager

/**
 * Created by josephmalone on 16-06-23.
 */
class WikiApplication : Application() {

    override fun onCreate() {
        mWikiApp = this
        super.onCreate()

        Logger.setShowErrorEnabled(isDebugEnabled)
        // Logger.setEnabled(true)

        if (isDebugEnabled) {
            PreferencesManager.getInstance().isLoggingEnabled = true
        }

        // We have a developer option that enables logging in production.
        // Therefore, we separate the enabling and disabling of it into this
        // if statement.
        if (PreferencesManager.getInstance().isLoggingEnabled) {
            Logger.setEnabled(isDebugEnabled)
            Logger.v("BRIEF", "yes this is logging")
        }

        // TODO fix debug flag
        Toaster.setEnabled(isDebugEnabled)
        Toaster.setEnabled(true)
    }

    companion object {
        private val TAG = WikiApplication::class.java.simpleName
        private var mWikiApp: WikiApplication? = null

        @JvmStatic
        fun getInstance(): WikiApplication? {
            return mWikiApp
        }

        @JvmStatic
        val isDebugEnabled: Boolean
            get() = BuildConfig.DEBUG
    }
}
