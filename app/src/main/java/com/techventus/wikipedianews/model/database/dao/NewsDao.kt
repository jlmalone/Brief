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

    /**
     * Observe bookmarked articles.
     */
    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun observeBookmarkedArticles(): Flow<List<NewsArticleEntity>>

    /**
     * Get bookmarked articles.
     */
    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    suspend fun getBookmarkedArticles(): List<NewsArticleEntity>

    /**
     * Toggle bookmark status for an article.
     */
    @Query("UPDATE news_articles SET isBookmarked = :isBookmarked WHERE id = :articleId")
    suspend fun updateBookmarkStatus(articleId: String, isBookmarked: Boolean)

    /**
     * Get bookmark count.
     */
    @Query("SELECT COUNT(*) FROM news_articles WHERE isBookmarked = 1")
    suspend fun getBookmarkedCount(): Int

    /**
     * Search articles by query in title, content, or section header.
     * Returns articles matching the search query.
     */
    @Query("""
        SELECT * FROM news_articles
        WHERE title LIKE '%' || :query || '%'
        OR htmlContent LIKE '%' || :query || '%'
        OR sectionHeader LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    suspend fun searchArticles(query: String): List<NewsArticleEntity>

    /**
     * Observe search results as Flow.
     */
    @Query("""
        SELECT * FROM news_articles
        WHERE title LIKE '%' || :query || '%'
        OR htmlContent LIKE '%' || :query || '%'
        OR sectionHeader LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun observeSearchResults(query: String): Flow<List<NewsArticleEntity>>
}
