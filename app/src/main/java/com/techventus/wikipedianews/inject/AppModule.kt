package com.techventus.wikipedianews.inject

import android.content.Context
import com.techventus.wikipedianews.manager.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-level dependencies.
 * Following android-template pattern.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(): PreferencesManager {
        return PreferencesManager.getInstance()
    }

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(application: android.app.Application): Context {
        return application.applicationContext
    }
}
