package com.techventus.wikipedianews

import android.os.StrictMode
import timber.log.Timber

class DebugApp : App() {
    override fun onCreate() {
        super.onCreate()

        // Enable StrictMode in debug builds
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        Timber.plant(Timber.DebugTree())
        Timber.d("Debug mode enabled with StrictMode")
    }
}
