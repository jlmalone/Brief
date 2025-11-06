package com.techventus.wikipedianews.model.datasource

import com.techventus.wikipedianews.model.database.dao.NewsDao
import com.techventus.wikipedianews.model.database.entity.NewsArticleEntity
import com.techventus.wikipedianews.model.database.entity.toDomain
import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.model.domain.NewsSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local data source for news using Room database.
 *
 * Handles all local database operations for news articles.
 * Following android-template pattern for data source abstraction.
 */
@Singleton
class NewsLocalDataSource @Inject constructor(
    private val newsDao: NewsDao
) {

    /**
     * Observe all news articles from database as Flow.
     * Groups articles by section header.
     */
    fun observeNews(): Flow<List<NewsSection>> {
        return newsDao.observeAllArticles()
            .map { entities -> groupEntitiesIntoSections(entities) }
    }

    /**
     * Get all news articles from database.
     */
    suspend fun getAllNews(): List<NewsSection> {
        val entities = newsDao.getAllArticles()
        return groupEntitiesIntoSections(entities)
    }

    /**
     * Save news articles to database.
     * Replaces existing articles.
     */
    suspend fun saveNews(sections: List<NewsSection>) {
        try {
            val entities = sections.flatMap { section ->
                section.articles.map { article ->
                    NewsArticleEntity(
                        id = article.id,
                        sectionHeader = section.header,
                        title = article.title,
                        htmlContent = article.htmlContent,
                        url = article.url,
                        timestamp = article.timestamp,
                        cachedAt = System.currentTimeMillis()
                    )
                }
            }

            newsDao.insertArticles(entities)
            Timber.d("Saved ${entities.size} articles to database")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save news to database")
            throw e
        }
    }

    /**
     * Clear all news from database.
     */
    suspend fun clearAll() {
        try {
            newsDao.deleteAllArticles()
            Timber.d("Cleared all news from database")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear database")
            throw e
        }
    }

    /**
     * Get count of cached articles.
     */
    suspend fun getCount(): Int {
        return newsDao.getArticlesCount()
    }

    /**
     * Delete expired articles older than given time.
     */
    suspend fun deleteExpired(expiryTime: Long) {
        try {
            newsDao.deleteExpiredArticles(expiryTime)
            Timber.d("Deleted articles older than $expiryTime")
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete expired articles")
        }
    }

    /**
     * Observe bookmarked articles from database as Flow.
     */
    fun observeBookmarkedNews(): Flow<List<NewsSection>> {
        return newsDao.observeBookmarkedArticles()
            .map { entities -> groupEntitiesIntoSections(entities) }
    }

    /**
     * Get bookmarked articles from database.
     */
    suspend fun getBookmarkedNews(): List<NewsSection> {
        val entities = newsDao.getBookmarkedArticles()
        return groupEntitiesIntoSections(entities)
    }

    /**
     * Toggle bookmark status for an article.
     */
    suspend fun toggleBookmark(articleId: String, isBookmarked: Boolean) {
        try {
            newsDao.updateBookmarkStatus(articleId, isBookmarked)
            Timber.d("Updated bookmark for article $articleId: $isBookmarked")
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle bookmark")
            throw e
        }
    }

    /**
     * Get count of bookmarked articles.
     */
    suspend fun getBookmarkedCount(): Int {
        return newsDao.getBookmarkedCount()
    }

    /**
     * Group entities into sections by header.
     */
    private fun groupEntitiesIntoSections(entities: List<NewsArticleEntity>): List<NewsSection> {
        return entities
            .groupBy { it.sectionHeader }
            .map { (header, articles) ->
                NewsSection(
                    header = header,
                    articles = articles.map { it.toDomain() }
                )
            }
    }
}
