package com.techventus.wikipedianews.model.datasource

import com.techventus.wikipedianews.model.domain.NewsSection
import com.techventus.wikipedianews.model.parser.WikipediaNewsParser
import retrofit2.Retrofit
import retrofit2.http.GET
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit service for Wikipedia Current Events.
 */
interface WikipediaService {
    @GET("wiki/Portal:Current_events")
    suspend fun getCurrentEvents(): String
}

/**
 * Remote data source for news using Wikipedia API.
 *
 * Handles all network operations for fetching news.
 * Following android-template pattern for remote data source.
 */
@Singleton
class NewsRemoteDataSource @Inject constructor(
    private val retrofit: Retrofit,
    private val parser: WikipediaNewsParser
) {

    private val service: WikipediaService by lazy {
        retrofit.create(WikipediaService::class.java)
    }

    /**
     * Fetch current events from Wikipedia.
     * Parses HTML into structured news sections.
     *
     * @return List of news sections
     * @throws Exception if network request fails or parsing fails
     */
    suspend fun fetchCurrentEvents(): List<NewsSection> {
        return try {
            Timber.d("Fetching current events from Wikipedia")

            // Fetch HTML from Wikipedia
            val html = service.getCurrentEvents()

            // Parse HTML into structured data
            val sections = parser.parse(html)

            Timber.d("Successfully fetched ${sections.size} sections with ${sections.sumOf { it.articles.size }} articles")

            sections
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch current events")
            throw e
        }
    }

    /**
     * Check if remote source is available.
     * Makes a lightweight HEAD request to verify connectivity.
     *
     * @return true if Wikipedia is accessible
     */
    suspend fun isAvailable(): Boolean {
        return try {
            service.getCurrentEvents()
            true
        } catch (e: Exception) {
            Timber.w("Remote source not available: ${e.message}")
            false
        }
    }
}
