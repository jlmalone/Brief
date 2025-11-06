package com.techventus.wikipedianews.model.repository

import com.techventus.wikipedianews.model.domain.NewsSection
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for news data.
 *
 * Following android-template and Clean Architecture patterns:
 * - Defines abstract contract for data operations
 * - Hides implementation details from consumers
 * - Supports reactive data flow with Kotlin Flow
 * - Implements offline-first strategy
 */
interface NewsRepository {

    /**
     * Observe news sections as a reactive Flow.
     * Emits cached data immediately, then updates from network.
     *
     * @return Flow of news sections from local cache
     */
    fun observeNews(): Flow<List<NewsSection>>

    /**
     * Refresh news from remote source.
     * Fetches latest data from Wikipedia and updates local cache.
     *
     * @return Result indicating success or failure
     */
    suspend fun refreshNews(): Result<Unit>

    /**
     * Force refresh news, ignoring cache.
     *
     * @return Result with fresh news sections
     */
    suspend fun forceRefresh(): Result<List<NewsSection>>

    /**
     * Get cached news count.
     *
     * @return Number of cached articles
     */
    suspend fun getCachedNewsCount(): Int

    /**
     * Clear all cached news.
     */
    suspend fun clearCache()

    /**
     * Observe bookmarked articles as a reactive Flow.
     *
     * @return Flow of bookmarked news sections
     */
    fun observeBookmarkedNews(): Flow<List<NewsSection>>

    /**
     * Toggle bookmark status for an article.
     *
     * @param articleId The article ID to toggle
     * @param isBookmarked The new bookmark status
     */
    suspend fun toggleBookmark(articleId: String, isBookmarked: Boolean)

    /**
     * Get bookmarked articles count.
     *
     * @return Number of bookmarked articles
     */
    suspend fun getBookmarkedCount(): Int

    /**
     * Search articles by query.
     *
     * @param query Search query string
     * @return List of news sections matching the query
     */
    suspend fun searchArticles(query: String): List<NewsSection>

    /**
     * Observe search results as a reactive Flow.
     *
     * @param query Search query string
     * @return Flow of news sections matching the query
     */
    fun observeSearchResults(query: String): Flow<List<NewsSection>>
}
