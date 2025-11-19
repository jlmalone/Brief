package com.techventus.wikipedianews.model.datasource

import com.techventus.wikipedianews.TestData
import com.techventus.wikipedianews.model.database.dao.NewsDao
import com.techventus.wikipedianews.model.database.entity.NewsArticleEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NewsLocalDataSource.
 *
 * Tests:
 * - Data grouping by section headers
 * - Flow transformations
 * - Error handling
 * - Entity/Domain conversions
 * - Search functionality
 * - Bookmark operations
 *
 * Uses MockK for DAO mocking.
 */
class NewsLocalDataSourceTest {

    private lateinit var newsDao: NewsDao
    private lateinit var dataSource: NewsLocalDataSource

    @Before
    fun setup() {
        newsDao = mockk(relaxed = true)
        dataSource = NewsLocalDataSource(newsDao)
    }

    @Test
    fun `observeNews groups entities by section header`() = runTest {
        // Given - entities from different sections
        val entities = listOf(
            TestData.createNewsArticleEntity(id = "1", sectionHeader = "Section A", title = "Article 1"),
            TestData.createNewsArticleEntity(id = "2", sectionHeader = "Section A", title = "Article 2"),
            TestData.createNewsArticleEntity(id = "3", sectionHeader = "Section B", title = "Article 3")
        )
        coEvery { newsDao.observeAllArticles() } returns flowOf(entities)

        // When
        val result = dataSource.observeNews().first()

        // Then
        assertEquals(2, result.size) // 2 sections
        assertEquals("Section A", result[0].header)
        assertEquals(2, result[0].articles.size)
        assertEquals("Article 1", result[0].articles[0].title)
        assertEquals("Article 2", result[0].articles[1].title)
        assertEquals("Section B", result[1].header)
        assertEquals(1, result[1].articles.size)
        assertEquals("Article 3", result[1].articles[0].title)
    }

    @Test
    fun `observeNews with empty data returns empty list`() = runTest {
        // Given
        coEvery { newsDao.observeAllArticles() } returns flowOf(emptyList())

        // When
        val result = dataSource.observeNews().first()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `observeNews preserves all entity fields in domain conversion`() = runTest {
        // Given
        val entity = TestData.createNewsArticleEntity(
            id = "test-id",
            sectionHeader = "Test Section",
            title = "Test Title",
            htmlContent = "<p>Content</p>",
            url = "https://test.com",
            timestamp = 12345L,
            isBookmarked = true
        )
        coEvery { newsDao.observeAllArticles() } returns flowOf(listOf(entity))

        // When
        val result = dataSource.observeNews().first()

        // Then
        val article = result[0].articles[0]
        assertEquals("test-id", article.id)
        assertEquals("Test Title", article.title)
        assertEquals("<p>Content</p>", article.htmlContent)
        assertEquals("https://test.com", article.url)
        assertEquals(12345L, article.timestamp)
        assertEquals(true, article.isBookmarked)
    }

    @Test
    fun `getAllNews returns grouped sections`() = runTest {
        // Given
        val entities = TestData.createEntitiesInMultipleSections(
            sections = listOf("Topics", "Ongoing")
        )
        coEvery { newsDao.getAllArticles() } returns entities

        // When
        val result = dataSource.getAllNews()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.any { it.header == "Topics" })
        assertTrue(result.any { it.header == "Ongoing" })
    }

    @Test
    fun `getAllNews with no data returns empty list`() = runTest {
        // Given
        coEvery { newsDao.getAllArticles() } returns emptyList()

        // When
        val result = dataSource.getAllNews()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `saveNews converts sections to entities and inserts`() = runTest {
        // Given
        val sections = listOf(
            TestData.createNewsSection(
                header = "Section 1",
                articles = listOf(
                    TestData.createNewsArticle(id = "1", title = "Article 1"),
                    TestData.createNewsArticle(id = "2", title = "Article 2")
                )
            ),
            TestData.createNewsSection(
                header = "Section 2",
                articles = listOf(
                    TestData.createNewsArticle(id = "3", title = "Article 3")
                )
            )
        )

        val capturedEntities = slot<List<NewsArticleEntity>>()
        coEvery { newsDao.insertArticles(capture(capturedEntities)) } returns Unit

        // When
        dataSource.saveNews(sections)

        // Then
        coVerify { newsDao.insertArticles(any()) }
        val entities = capturedEntities.captured
        assertEquals(3, entities.size)

        // Verify first section's articles
        assertEquals("1", entities[0].id)
        assertEquals("Section 1", entities[0].sectionHeader)
        assertEquals("Article 1", entities[0].title)

        assertEquals("2", entities[1].id)
        assertEquals("Section 1", entities[1].sectionHeader)

        // Verify second section's article
        assertEquals("3", entities[2].id)
        assertEquals("Section 2", entities[2].sectionHeader)
    }

    @Test
    fun `saveNews sets cachedAt timestamp`() = runTest {
        // Given
        val section = TestData.createNewsSection(
            articles = listOf(TestData.createNewsArticle(id = "1"))
        )

        val capturedEntities = slot<List<NewsArticleEntity>>()
        coEvery { newsDao.insertArticles(capture(capturedEntities)) } returns Unit

        val beforeTime = System.currentTimeMillis()

        // When
        dataSource.saveNews(listOf(section))

        val afterTime = System.currentTimeMillis()

        // Then
        val entity = capturedEntities.captured[0]
        assertTrue(entity.cachedAt >= beforeTime)
        assertTrue(entity.cachedAt <= afterTime)
    }

    @Test
    fun `saveNews with empty sections does nothing`() = runTest {
        // When
        dataSource.saveNews(emptyList())

        // Then
        coVerify { newsDao.insertArticles(emptyList()) }
    }

    @Test
    fun `saveNews handles exception and rethrows`() = runTest {
        // Given
        val sections = listOf(TestData.createNewsSection())
        coEvery { newsDao.insertArticles(any()) } throws RuntimeException("Database error")

        // When/Then
        try {
            dataSource.saveNews(sections)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }

    @Test
    fun `clearAll calls deleteAllArticles`() = runTest {
        // Given
        coEvery { newsDao.deleteAllArticles() } returns Unit

        // When
        dataSource.clearAll()

        // Then
        coVerify { newsDao.deleteAllArticles() }
    }

    @Test
    fun `clearAll handles exception and rethrows`() = runTest {
        // Given
        coEvery { newsDao.deleteAllArticles() } throws RuntimeException("Delete failed")

        // When/Then
        try {
            dataSource.clearAll()
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Delete failed", e.message)
        }
    }

    @Test
    fun `getCount returns dao count`() = runTest {
        // Given
        coEvery { newsDao.getArticlesCount() } returns 42

        // When
        val result = dataSource.getCount()

        // Then
        assertEquals(42, result)
        coVerify { newsDao.getArticlesCount() }
    }

    @Test
    fun `deleteExpired calls dao with correct time`() = runTest {
        // Given
        val expiryTime = 1234567890L
        coEvery { newsDao.deleteExpiredArticles(expiryTime) } returns Unit

        // When
        dataSource.deleteExpired(expiryTime)

        // Then
        coVerify { newsDao.deleteExpiredArticles(expiryTime) }
    }

    @Test
    fun `deleteExpired handles exception gracefully`() = runTest {
        // Given - should not throw, just log
        coEvery { newsDao.deleteExpiredArticles(any()) } throws RuntimeException("Delete failed")

        // When - should not throw
        dataSource.deleteExpired(1000L)

        // Then
        coVerify { newsDao.deleteExpiredArticles(1000L) }
    }

    @Test
    fun `observeBookmarkedNews returns only bookmarked articles`() = runTest {
        // Given
        val entities = listOf(
            TestData.createNewsArticleEntity(id = "1", sectionHeader = "Section A", isBookmarked = true),
            TestData.createNewsArticleEntity(id = "2", sectionHeader = "Section B", isBookmarked = true)
        )
        coEvery { newsDao.observeBookmarkedArticles() } returns flowOf(entities)

        // When
        val result = dataSource.observeBookmarkedNews().first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { section -> section.articles.all { it.isBookmarked } })
    }

    @Test
    fun `observeBookmarkedNews groups bookmarks by section`() = runTest {
        // Given
        val entities = listOf(
            TestData.createNewsArticleEntity(id = "1", sectionHeader = "Topics", isBookmarked = true),
            TestData.createNewsArticleEntity(id = "2", sectionHeader = "Topics", isBookmarked = true),
            TestData.createNewsArticleEntity(id = "3", sectionHeader = "Ongoing", isBookmarked = true)
        )
        coEvery { newsDao.observeBookmarkedArticles() } returns flowOf(entities)

        // When
        val result = dataSource.observeBookmarkedNews().first()

        // Then
        assertEquals(2, result.size)
        val topicsSection = result.find { it.header == "Topics" }
        assertEquals(2, topicsSection?.articles?.size)
    }

    @Test
    fun `getBookmarkedNews returns bookmarked sections`() = runTest {
        // Given
        val entities = listOf(
            TestData.createNewsArticleEntity(id = "1", sectionHeader = "Test", isBookmarked = true)
        )
        coEvery { newsDao.getBookmarkedArticles() } returns entities

        // When
        val result = dataSource.getBookmarkedNews()

        // Then
        assertEquals(1, result.size)
        assertEquals(1, result[0].articles.size)
        assertTrue(result[0].articles[0].isBookmarked)
    }

    @Test
    fun `toggleBookmark calls dao with correct parameters`() = runTest {
        // Given
        coEvery { newsDao.updateBookmarkStatus(any(), any()) } returns Unit

        // When
        dataSource.toggleBookmark("article-123", true)

        // Then
        coVerify { newsDao.updateBookmarkStatus("article-123", true) }
    }

    @Test
    fun `toggleBookmark handles exception and rethrows`() = runTest {
        // Given
        coEvery { newsDao.updateBookmarkStatus(any(), any()) } throws RuntimeException("Update failed")

        // When/Then
        try {
            dataSource.toggleBookmark("123", true)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Update failed", e.message)
        }
    }

    @Test
    fun `getBookmarkedCount returns dao count`() = runTest {
        // Given
        coEvery { newsDao.getBookmarkedCount() } returns 5

        // When
        val result = dataSource.getBookmarkedCount()

        // Then
        assertEquals(5, result)
        coVerify { newsDao.getBookmarkedCount() }
    }

    @Test
    fun `searchArticles with valid query returns results`() = runTest {
        // Given
        val entities = listOf(
            TestData.createNewsArticleEntity(id = "1", sectionHeader = "Section A", title = "Climate Change")
        )
        coEvery { newsDao.searchArticles("Climate") } returns entities

        // When
        val result = dataSource.searchArticles("Climate")

        // Then
        assertEquals(1, result.size)
        assertEquals("Climate Change", result[0].articles[0].title)
        coVerify { newsDao.searchArticles("Climate") }
    }

    @Test
    fun `searchArticles with blank query returns empty`() = runTest {
        // When
        val result1 = dataSource.searchArticles("")
        val result2 = dataSource.searchArticles("   ")

        // Then
        assertEquals(0, result1.size)
        assertEquals(0, result2.size)
        coVerify(exactly = 0) { newsDao.searchArticles(any()) }
    }

    @Test
    fun `searchArticles groups results by section`() = runTest {
        // Given
        val entities = listOf(
            TestData.createNewsArticleEntity(id = "1", sectionHeader = "Politics", title = "Election"),
            TestData.createNewsArticleEntity(id = "2", sectionHeader = "Politics", title = "Election Results"),
            TestData.createNewsArticleEntity(id = "3", sectionHeader = "Sports", title = "Election Day Game")
        )
        coEvery { newsDao.searchArticles("Election") } returns entities

        // When
        val result = dataSource.searchArticles("Election")

        // Then
        assertEquals(2, result.size) // 2 sections
        val politicsSection = result.find { it.header == "Politics" }
        assertEquals(2, politicsSection?.articles?.size)
    }

    @Test
    fun `observeSearchResults with valid query emits results`() = runTest {
        // Given
        val entities = listOf(
            TestData.createNewsArticleEntity(id = "1", sectionHeader = "Tech", title = "AI News")
        )
        every { newsDao.observeSearchResults("AI") } returns flowOf(entities)

        // When
        val result = dataSource.observeSearchResults("AI").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("AI News", result[0].articles[0].title)
    }

    @Test
    fun `observeSearchResults with blank query emits empty`() = runTest {
        // When
        val result1 = dataSource.observeSearchResults("").first()
        val result2 = dataSource.observeSearchResults("   ").first()

        // Then
        assertEquals(0, result1.size)
        assertEquals(0, result2.size)
        coVerify(exactly = 0) { newsDao.observeSearchResults(any()) }
    }

    @Test
    fun `groupEntitiesIntoSections handles single section`() = runTest {
        // Given
        val entities = TestData.createMultipleEntities(count = 3, sectionHeader = "Single Section")
        coEvery { newsDao.getAllArticles() } returns entities

        // When
        val result = dataSource.getAllNews()

        // Then
        assertEquals(1, result.size)
        assertEquals("Single Section", result[0].header)
        assertEquals(3, result[0].articles.size)
    }

    @Test
    fun `groupEntitiesIntoSections maintains article order`() = runTest {
        // Given - entities ordered by timestamp DESC
        val entities = listOf(
            TestData.createNewsArticleEntity(id = "3", sectionHeader = "Test", title = "Third", timestamp = 3000L),
            TestData.createNewsArticleEntity(id = "2", sectionHeader = "Test", title = "Second", timestamp = 2000L),
            TestData.createNewsArticleEntity(id = "1", sectionHeader = "Test", title = "First", timestamp = 1000L)
        )
        coEvery { newsDao.getAllArticles() } returns entities

        // When
        val result = dataSource.getAllNews()

        // Then - order should be preserved
        val articles = result[0].articles
        assertEquals("Third", articles[0].title)
        assertEquals("Second", articles[1].title)
        assertEquals("First", articles[2].title)
    }
}
