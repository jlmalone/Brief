package com.techventus.wikipedianews.inject

import android.content.Context
import androidx.room.Room
import com.techventus.wikipedianews.model.database.AppDatabase
import com.techventus.wikipedianews.model.database.dao.NewsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing database dependencies.
 * Following android-template pattern.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development
            .build()
    }

    @Provides
    @Singleton
    fun provideNewsDao(database: AppDatabase): NewsDao {
        return database.newsDao()
    }
}
