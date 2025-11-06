package com.techventus.wikipedianews.model.repository

import com.techventus.wikipedianews.inject.IoDispatcher
import com.techventus.wikipedianews.model.datasource.NewsLocalDataSource
import com.techventus.wikipedianews.model.datasource.NewsRemoteDataSource
import com.techventus.wikipedianews.model.domain.NewsSection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NewsRepository following offline-first architecture.
 *
 * Data flow:
 * 1. Always return cached data immediately via Flow
 * 2. Fetch fresh data from remote in background
 * 3. Update cache with fresh data
 * 4. Flow automatically emits updated data
 *
 * Following android-template pattern for repository implementation.
 */
@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NewsRemoteDataSource,
    private val localDataSource: NewsLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NewsRepository {

    /**
     * Observe news with offline-first strategy.
     * Immediately returns cached data, then refreshes from network.
     */
    override fun observeNews(): Flow<List<NewsSection>> {
        return localDataSource.observeNews()
            .flowOn(ioDispatcher)
    }

    /**
     * Refresh news from remote source.
     * Updates local cache on success.
     */
    override suspend fun refreshNews(): Result<Unit> = withContext(ioDispatcher) {
        try {
            Timber.d("Refreshing news from remote source")

            // Fetch fresh data from Wikipedia
            val freshSections = remoteDataSource.fetchCurrentEvents()

            if (freshSections.isEmpty()) {
                Timber.w("Remote source returned empty data")
                return@withContext Result.failure(Exception("No data available"))
            }

            // Clear old cache and save fresh data
            localDataSource.clearAll()
            localDataSource.saveNews(freshSections)

            Timber.d("Successfully refreshed news: ${freshSections.size} sections")

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh news")
            Result.failure(e)
        }
    }

    /**
     * Force refresh ignoring cache.
     * Returns fresh data directly without using cache.
     */
    override suspend fun forceRefresh(): Result<List<NewsSection>> = withContext(ioDispatcher) {
        try {
            Timber.d("Force refreshing news")

            val freshSections = remoteDataSource.fetchCurrentEvents()

            if (freshSections.isEmpty()) {
                return@withContext Result.failure(Exception("No data available"))
            }

            // Update cache
            localDataSource.clearAll()
            localDataSource.saveNews(freshSections)

            Result.success(freshSections)
        } catch (e: Exception) {
            Timber.e(e, "Force refresh failed")

            // Fall back to cached data if available
            val cachedSections = localDataSource.getAllNews()
            if (cachedSections.isNotEmpty()) {
                Timber.d("Returning cached data as fallback")
                Result.success(cachedSections)
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Get cached news count.
     */
    override suspend fun getCachedNewsCount(): Int = withContext(ioDispatcher) {
        try {
            localDataSource.getCount()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get cached count")
            0
        }
    }

    /**
     * Clear all cached news.
     */
    override suspend fun clearCache() = withContext(ioDispatcher) {
        try {
            Timber.d("Clearing cache")
            localDataSource.clearAll()
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear cache")
        }
    }
}
