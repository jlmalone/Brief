package com.techventus.wikipedianews.model.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.techventus.wikipedianews.model.database.entity.NewsArticleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for news articles.
 */
@Dao
interface NewsDao {

    /**
     * Observe all news articles, ordered by timestamp descending.
     */
    @Query("SELECT * FROM news_articles ORDER BY timestamp DESC")
    fun observeAllArticles(): Flow<List<NewsArticleEntity>>

    /**
     * Get all news articles.
     */
    @Query("SELECT * FROM news_articles ORDER BY timestamp DESC")
    suspend fun getAllArticles(): List<NewsArticleEntity>

    /**
     * Insert or replace news articles.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticleEntity>)

    /**
     * Delete all news articles.
     */
    @Query("DELETE FROM news_articles")
    suspend fun deleteAllArticles()

    /**
     * Delete expired articles older than the given time.
     */
    @Query("DELETE FROM news_articles WHERE cachedAt < :expiryTime")
    suspend fun deleteExpiredArticles(expiryTime: Long)

    /**
     * Get articles count.
     */
    @Query("SELECT COUNT(*) FROM news_articles")
    suspend fun getArticlesCount(): Int
}
