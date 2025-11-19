package com.techventus.wikipedianews.model.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.techventus.wikipedianews.TestData
import com.techventus.wikipedianews.model.database.AppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for NewsDao.
 *
 * Tests all database operations including:
 * - CRUD operations
 * - Flow-based observations
 * - Search functionality
 * - Bookmark management
 * - Data expiration
 *
 * Uses in-memory database for fast, isolated tests.
 */
@RunWith(AndroidJUnit4::class)
class NewsDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var newsDao: NewsDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // For testing only
            .build()
        newsDao = database.newsDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertArticles_and_getAllArticles_returnsInsertedData() = runTest {
        // Given
        val articles = TestData.createMultipleEntities(count = 3)

        // When
        newsDao.insertArticles(articles)
        val result = newsDao.getAllArticles()

        // Then
        assertEquals(3, result.size)
        assertEquals("Test Article 1", result[2].title) // DESC order
        assertEquals("Test Article 3", result[0].title)
    }

    @Test
    fun insertArticles_withEmptyList_doesNothing() = runTest {
        // When
        newsDao.insertArticles(emptyList())
        val result = newsDao.getAllArticles()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun insertArticles_withConflict_replacesExisting() = runTest {
        // Given
        val article = TestData.createNewsArticleEntity(id = "1", title = "Original")
        newsDao.insertArticles(listOf(article))

        // When - insert same ID with different data
        val updatedArticle = TestData.createNewsArticleEntity(id = "1", title = "Updated")
        newsDao.insertArticles(listOf(updatedArticle))

        // Then
        val result = newsDao.getAllArticles()
        assertEquals(1, result.size)
        assertEquals("Updated", result[0].title)
    }

    @Test
    fun observeAllArticles_emitsInitialEmptyList() = runTest {
        // When/Then
        newsDao.observeAllArticles().test {
            val items = awaitItem()
            assertEquals(0, items.size)
            cancel()
        }
    }

    @Test
    fun observeAllArticles_emitsUpdatesOnInsert() = runTest {
        // When
        newsDao.observeAllArticles().test {
            // Initial empty state
            assertEquals(0, awaitItem().size)

            // Insert data
            val article = TestData.createNewsArticleEntity()
            newsDao.insertArticles(listOf(article))

            // Should emit update
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Test Article Title", items[0].title)

            cancel()
        }
    }

    @Test
    fun observeAllArticles_emitsUpdatesOnDelete() = runTest {
        // Given - start with data
        val article = TestData.createNewsArticleEntity()
        newsDao.insertArticles(listOf(article))

        // When
        newsDao.observeAllArticles().test {
            // Initial state with data
            assertEquals(1, awaitItem().size)

            // Delete all
            newsDao.deleteAllArticles()

            // Should emit update
            assertEquals(0, awaitItem().size)

            cancel()
        }
    }

    @Test
    fun deleteAllArticles_removesAllData() = runTest {
        // Given
        val articles = TestData.createMultipleEntities(count = 5)
        newsDao.insertArticles(articles)

        // When
        newsDao.deleteAllArticles()

        // Then
        val result = newsDao.getAllArticles()
        assertEquals(0, result.size)
    }

    @Test
    fun deleteExpiredArticles_removesOnlyOldArticles() = runTest {
        // Given - mix of old and new articles
        val oldArticle1 = TestData.createNewsArticleEntity(
            id = "old1",
            title = "Old 1",
            cachedAt = 1000L
        )
        val oldArticle2 = TestData.createNewsArticleEntity(
            id = "old2",
            title = "Old 2",
            cachedAt = 2000L
        )
        val newArticle = TestData.createNewsArticleEntity(
            id = "new1",
            title = "New 1",
            cachedAt = 5000L
        )

        newsDao.insertArticles(listOf(oldArticle1, oldArticle2, newArticle))

        // When - delete articles cached before 3000L
        newsDao.deleteExpiredArticles(expiryTime = 3000L)

        // Then - only new article should remain
        val remaining = newsDao.getAllArticles()
        assertEquals(1, remaining.size)
        assertEquals("New 1", remaining[0].title)
    }

    @Test
    fun deleteExpiredArticles_withNoExpiredArticles_keepsAll() = runTest {
        // Given
        val articles = TestData.createMultipleEntities(count = 3)
        newsDao.insertArticles(articles)

        // When - expiry time before all articles
        newsDao.deleteExpiredArticles(expiryTime = 0L)

        // Then - all articles remain
        val remaining = newsDao.getAllArticles()
        assertEquals(3, remaining.size)
    }

    @Test
    fun getArticlesCount_returnsCorrectCount() = runTest {
        // Given
        assertEquals(0, newsDao.getArticlesCount())

        // When
        val articles = TestData.createMultipleEntities(count = 7)
        newsDao.insertArticles(articles)

        // Then
        assertEquals(7, newsDao.getArticlesCount())
    }

    @Test
    fun updateBookmarkStatus_updatesSpecificArticle() = runTest {
        // Given
        val articles = TestData.createMultipleEntities(count = 3)
        newsDao.insertArticles(articles)

        // When
        newsDao.updateBookmarkStatus(articleId = "test-article-2", isBookmarked = true)

        // Then
        val allArticles = newsDao.getAllArticles()
        assertEquals(false, allArticles.find { it.id == "test-article-1" }?.isBookmarked)
        assertEquals(true, allArticles.find { it.id == "test-article-2" }?.isBookmarked)
        assertEquals(false, allArticles.find { it.id == "test-article-3" }?.isBookmarked)
    }

    @Test
    fun updateBookmarkStatus_togglesBookmark() = runTest {
        // Given
        val article = TestData.createNewsArticleEntity(id = "1", isBookmarked = false)
        newsDao.insertArticles(listOf(article))

        // When - bookmark it
        newsDao.updateBookmarkStatus("1", true)
        var result = newsDao.getAllArticles()[0]
        assertEquals(true, result.isBookmarked)

        // When - unbookmark it
        newsDao.updateBookmarkStatus("1", false)
        result = newsDao.getAllArticles()[0]
        assertEquals(false, result.isBookmarked)
    }

    @Test
    fun getBookmarkedArticles_returnsOnlyBookmarked() = runTest {
        // Given
        val bookmarked1 = TestData.createNewsArticleEntity(id = "b1", title = "Bookmarked 1", isBookmarked = true)
        val notBookmarked = TestData.createNewsArticleEntity(id = "nb", title = "Not Bookmarked", isBookmarked = false)
        val bookmarked2 = TestData.createNewsArticleEntity(id = "b2", title = "Bookmarked 2", isBookmarked = true)

        newsDao.insertArticles(listOf(bookmarked1, notBookmarked, bookmarked2))

        // When
        val result = newsDao.getBookmarkedArticles()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isBookmarked })
        assertTrue(result.any { it.title == "Bookmarked 1" })
        assertTrue(result.any { it.title == "Bookmarked 2" })
    }

    @Test
    fun observeBookmarkedArticles_emitsOnlyBookmarked() = runTest {
        // Given
        val bookmarked = TestData.createNewsArticleEntity(id = "b1", isBookmarked = true)
        val notBookmarked = TestData.createNewsArticleEntity(id = "nb", isBookmarked = false)
        newsDao.insertArticles(listOf(bookmarked, notBookmarked))

        // When/Then
        newsDao.observeBookmarkedArticles().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(true, items[0].isBookmarked)
            cancel()
        }
    }

    @Test
    fun observeBookmarkedArticles_updatesOnBookmarkChange() = runTest {
        // Given
        val article = TestData.createNewsArticleEntity(id = "1", isBookmarked = false)
        newsDao.insertArticles(listOf(article))

        // When
        newsDao.observeBookmarkedArticles().test {
            // Initial - no bookmarks
            assertEquals(0, awaitItem().size)

            // Bookmark the article
            newsDao.updateBookmarkStatus("1", true)

            // Should emit update
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(true, items[0].isBookmarked)

            cancel()
        }
    }

    @Test
    fun getBookmarkedCount_returnsCorrectCount() = runTest {
        // Given
        val bookmarked1 = TestData.createNewsArticleEntity(id = "b1", isBookmarked = true)
        val bookmarked2 = TestData.createNewsArticleEntity(id = "b2", isBookmarked = true)
        val notBookmarked = TestData.createNewsArticleEntity(id = "nb", isBookmarked = false)

        newsDao.insertArticles(listOf(bookmarked1, bookmarked2, notBookmarked))

        // When
        val count = newsDao.getBookmarkedCount()

        // Then
        assertEquals(2, count)
    }

    @Test
    fun searchArticles_findsByTitle() = runTest {
        // Given
        val article1 = TestData.createNewsArticleEntity(id = "1", title = "Climate Change Report")
        val article2 = TestData.createNewsArticleEntity(id = "2", title = "Technology News")
        val article3 = TestData.createNewsArticleEntity(id = "3", title = "Climate Summit")

        newsDao.insertArticles(listOf(article1, article2, article3))

        // When
        val results = newsDao.searchArticles("Climate")

        // Then
        assertEquals(2, results.size)
        assertTrue(results.any { it.title == "Climate Change Report" })
        assertTrue(results.any { it.title == "Climate Summit" })
    }

    @Test
    fun searchArticles_findsByHtmlContent() = runTest {
        // Given
        val article1 = TestData.createNewsArticleEntity(
            id = "1",
            title = "Article 1",
            htmlContent = "<p>Content about quantum computing</p>"
        )
        val article2 = TestData.createNewsArticleEntity(
            id = "2",
            title = "Article 2",
            htmlContent = "<p>Content about weather</p>"
        )

        newsDao.insertArticles(listOf(article1, article2))

        // When
        val results = newsDao.searchArticles("quantum")

        // Then
        assertEquals(1, results.size)
        assertEquals("Article 1", results[0].title)
    }

    @Test
    fun searchArticles_findsBySectionHeader() = runTest {
        // Given
        val article1 = TestData.createNewsArticleEntity(
            id = "1",
            sectionHeader = "Topics in the News",
            title = "Article 1"
        )
        val article2 = TestData.createNewsArticleEntity(
            id = "2",
            sectionHeader = "Ongoing Events",
            title = "Article 2"
        )

        newsDao.insertArticles(listOf(article1, article2))

        // When
        val results = newsDao.searchArticles("Topics")

        // Then
        assertEquals(1, results.size)
        assertEquals("Article 1", results[0].title)
    }

    @Test
    fun searchArticles_isCaseInsensitive() = runTest {
        // Given
        val article = TestData.createNewsArticleEntity(id = "1", title = "UPPERCASE Title")
        newsDao.insertArticles(listOf(article))

        // When
        val results = newsDao.searchArticles("uppercase")

        // Then
        assertEquals(1, results.size)
    }

    @Test
    fun searchArticles_withNoMatches_returnsEmpty() = runTest {
        // Given
        val articles = TestData.createMultipleEntities(count = 3)
        newsDao.insertArticles(articles)

        // When
        val results = newsDao.searchArticles("NonExistentQuery")

        // Then
        assertEquals(0, results.size)
    }

    @Test
    fun observeSearchResults_emitsMatchingArticles() = runTest {
        // Given
        val article1 = TestData.createNewsArticleEntity(id = "1", title = "Bitcoin Price")
        val article2 = TestData.createNewsArticleEntity(id = "2", title = "Weather Update")
        newsDao.insertArticles(listOf(article1, article2))

        // When/Then
        newsDao.observeSearchResults("Bitcoin").test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Bitcoin Price", items[0].title)
            cancel()
        }
    }

    @Test
    fun observeSearchResults_updatesOnNewMatchingData() = runTest {
        // Given
        val article1 = TestData.createNewsArticleEntity(id = "1", title = "Sports News")
        newsDao.insertArticles(listOf(article1))

        // When
        newsDao.observeSearchResults("Sports").test {
            // Initial match
            assertEquals(1, awaitItem().size)

            // Insert another matching article
            val article2 = TestData.createNewsArticleEntity(id = "2", title = "Sports Update")
            newsDao.insertArticles(listOf(article2))

            // Should emit update
            val items = awaitItem()
            assertEquals(2, items.size)

            cancel()
        }
    }

    @Test
    fun getAllArticles_orderedByTimestampDesc() = runTest {
        // Given - insert in random order
        val old = TestData.createNewsArticleEntity(id = "old", timestamp = 1000L)
        val newest = TestData.createNewsArticleEntity(id = "newest", timestamp = 3000L)
        val middle = TestData.createNewsArticleEntity(id = "middle", timestamp = 2000L)

        newsDao.insertArticles(listOf(old, newest, middle))

        // When
        val results = newsDao.getAllArticles()

        // Then - should be ordered newest first
        assertEquals("newest", results[0].id)
        assertEquals("middle", results[1].id)
        assertEquals("old", results[2].id)
    }

    @Test
    fun getBookmarkedArticles_orderedByTimestampDesc() = runTest {
        // Given
        val old = TestData.createNewsArticleEntity(id = "old", timestamp = 1000L, isBookmarked = true)
        val newest = TestData.createNewsArticleEntity(id = "newest", timestamp = 3000L, isBookmarked = true)

        newsDao.insertArticles(listOf(old, newest))

        // When
        val results = newsDao.getBookmarkedArticles()

        // Then
        assertEquals("newest", results[0].id)
        assertEquals("old", results[1].id)
    }

    @Test
    fun searchArticles_orderedByTimestampDesc() = runTest {
        // Given
        val old = TestData.createNewsArticleEntity(id = "old", title = "Test Article", timestamp = 1000L)
        val newest = TestData.createNewsArticleEntity(id = "newest", title = "Test News", timestamp = 3000L)

        newsDao.insertArticles(listOf(old, newest))

        // When
        val results = newsDao.searchArticles("Test")

        // Then
        assertEquals("newest", results[0].id)
        assertEquals("old", results[1].id)
    }
}
