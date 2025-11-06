package com.techventus.wikipedianews.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.techventus.wikipedianews.model.database.dao.NewsDao
import com.techventus.wikipedianews.model.database.entity.NewsArticleEntity

/**
 * Room database for Brief app.
 *
 * Following android-template pattern for database architecture.
 */
@Database(
    entities = [
        NewsArticleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao

    companion object {
        const val DATABASE_NAME = "brief_database"
    }
}
