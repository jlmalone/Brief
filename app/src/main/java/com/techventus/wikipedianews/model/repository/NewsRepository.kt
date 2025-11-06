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
}
