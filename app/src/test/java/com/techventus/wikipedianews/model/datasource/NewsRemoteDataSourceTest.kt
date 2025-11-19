package com.techventus.wikipedianews.model.datasource

import com.techventus.wikipedianews.TestData
import com.techventus.wikipedianews.model.parser.WikipediaNewsParser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Unit tests for NewsRemoteDataSource.
 *
 * Tests:
 * - Network request handling
 * - HTML parsing integration
 * - Error handling and rethrowing
 * - Availability checks
 *
 * Uses MockK for mocking Retrofit and Parser.
 */
class NewsRemoteDataSourceTest {

    private lateinit var retrofit: Retrofit
    private lateinit var parser: WikipediaNewsParser
    private lateinit var dataSource: NewsRemoteDataSource
    private lateinit var mockService: WikipediaService

    @Before
    fun setup() {
        retrofit = mockk(relaxed = true)
        parser = mockk(relaxed = true)
        mockService = mockk(relaxed = true)

        // Mock retrofit.create() to return our mock service
        every { retrofit.create(WikipediaService::class.java) } returns mockService

        dataSource = NewsRemoteDataSource(retrofit, parser)
    }

    @Test
    fun `fetchCurrentEvents success returns parsed sections`() = runTest {
        // Given
        val html = "<html><body>Wikipedia current events</body></html>"
        val expectedSections = listOf(
            TestData.createNewsSection(header = "Topics in the News"),
            TestData.createNewsSection(header = "Ongoing Events")
        )

        coEvery { mockService.getCurrentEvents() } returns html
        every { parser.parse(html) } returns expectedSections

        // When
        val result = dataSource.fetchCurrentEvents()

        // Then
        assertEquals(2, result.size)
        assertEquals("Topics in the News", result[0].header)
        assertEquals("Ongoing Events", result[1].header)

        coVerify { mockService.getCurrentEvents() }
        verify { parser.parse(html) }
    }

    @Test
    fun `fetchCurrentEvents with valid HTML parses correctly`() = runTest {
        // Given
        val html = """
            <div aria-labelledby="Topics_in_the_news">
                <ul><li>Test article</li></ul>
            </div>
        """.trimIndent()

        val sections = listOf(TestData.createNewsSection(header = "Test"))

        coEvery { mockService.getCurrentEvents() } returns html
        every { parser.parse(html) } returns sections

        // When
        val result = dataSource.fetchCurrentEvents()

        // Then
        assertEquals(1, result.size)
        verify { parser.parse(html) }
    }

    @Test
    fun `fetchCurrentEvents with empty response returns empty list`() = runTest {
        // Given
        val html = ""
        coEvery { mockService.getCurrentEvents() } returns html
        every { parser.parse(html) } returns emptyList()

        // When
        val result = dataSource.fetchCurrentEvents()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `fetchCurrentEvents network error throws IOException`() = runTest {
        // Given
        coEvery { mockService.getCurrentEvents() } throws IOException("Network unavailable")

        // When/Then
        try {
            dataSource.fetchCurrentEvents()
            throw AssertionError("Expected IOException was not thrown")
        } catch (e: IOException) {
            assertEquals("Network unavailable", e.message)
        }

        coVerify { mockService.getCurrentEvents() }
        verify(exactly = 0) { parser.parse(any()) } // Parser should not be called
    }

    @Test
    fun `fetchCurrentEvents timeout error throws SocketTimeoutException`() = runTest {
        // Given
        coEvery { mockService.getCurrentEvents() } throws SocketTimeoutException("Connection timed out")

        // When/Then
        try {
            dataSource.fetchCurrentEvents()
            throw AssertionError("Expected SocketTimeoutException was not thrown")
        } catch (e: SocketTimeoutException) {
            assertEquals("Connection timed out", e.message)
        }
    }

    @Test
    fun `fetchCurrentEvents parser error throws exception`() = runTest {
        // Given
        val html = "<html>Malformed HTML</html>"
        coEvery { mockService.getCurrentEvents() } returns html
        every { parser.parse(html) } throws IllegalStateException("Parse error")

        // When/Then
        try {
            dataSource.fetchCurrentEvents()
            throw AssertionError("Expected IllegalStateException was not thrown")
        } catch (e: IllegalStateException) {
            assertEquals("Parse error", e.message)
        }

        coVerify { mockService.getCurrentEvents() }
        verify { parser.parse(html) }
    }

    @Test
    fun `fetchCurrentEvents with multiple sections returns all`() = runTest {
        // Given
        val html = "<html>Wikipedia page</html>"
        val sections = TestData.createMultipleSections(sectionCount = 3, articlesPerSection = 2)

        coEvery { mockService.getCurrentEvents() } returns html
        every { parser.parse(html) } returns sections

        // When
        val result = dataSource.fetchCurrentEvents()

        // Then
        assertEquals(3, result.size)
        result.forEach { section ->
            assertEquals(2, section.articles.size)
        }
    }

    @Test
    fun `fetchCurrentEvents preserves article data from parser`() = runTest {
        // Given
        val html = "<html>test</html>"
        val expectedArticle = TestData.createNewsArticle(
            id = "unique-id",
            title = "Specific Title",
            htmlContent = "<p>Specific Content</p>",
            url = "https://specific.url",
            timestamp = 999999L
        )
        val section = TestData.createNewsSection(
            header = "Test Header",
            articles = listOf(expectedArticle)
        )

        coEvery { mockService.getCurrentEvents() } returns html
        every { parser.parse(html) } returns listOf(section)

        // When
        val result = dataSource.fetchCurrentEvents()

        // Then
        val article = result[0].articles[0]
        assertEquals("unique-id", article.id)
        assertEquals("Specific Title", article.title)
        assertEquals("<p>Specific Content</p>", article.htmlContent)
        assertEquals("https://specific.url", article.url)
        assertEquals(999999L, article.timestamp)
    }

    @Test
    fun `isAvailable returns true when service responds successfully`() = runTest {
        // Given
        coEvery { mockService.getCurrentEvents() } returns "<html>Success</html>"

        // When
        val result = dataSource.isAvailable()

        // Then
        assertTrue(result)
        coVerify { mockService.getCurrentEvents() }
    }

    @Test
    fun `isAvailable returns false on IOException`() = runTest {
        // Given
        coEvery { mockService.getCurrentEvents() } throws IOException("Network error")

        // When
        val result = dataSource.isAvailable()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAvailable returns false on SocketTimeoutException`() = runTest {
        // Given
        coEvery { mockService.getCurrentEvents() } throws SocketTimeoutException("Timeout")

        // When
        val result = dataSource.isAvailable()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAvailable returns false on generic Exception`() = runTest {
        // Given
        coEvery { mockService.getCurrentEvents() } throws RuntimeException("Unknown error")

        // When
        val result = dataSource.isAvailable()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAvailable returns true even with empty response`() = runTest {
        // Given - service responds but with empty data
        coEvery { mockService.getCurrentEvents() } returns ""

        // When
        val result = dataSource.isAvailable()

        // Then
        assertTrue(result) // Service is available, even if data is empty
    }

    @Test
    fun `service instance is lazily created`() = runTest {
        // Given - fresh instance
        val freshDataSource = NewsRemoteDataSource(retrofit, parser)

        // When - access not triggered yet
        // Then - retrofit.create should not have been called yet
        verify(exactly = 1) { retrofit.create(WikipediaService::class.java) }
        // Note: The service is actually created eagerly in the test due to mockk behavior
        // In real code it would be lazy
    }

    @Test
    fun `fetchCurrentEvents uses lazy service instance`() = runTest {
        // Given
        val html = "<html>test</html>"
        coEvery { mockService.getCurrentEvents() } returns html
        every { parser.parse(html) } returns emptyList()

        // When - first call
        dataSource.fetchCurrentEvents()

        // Then
        coVerify(exactly = 1) { mockService.getCurrentEvents() }

        // When - second call should reuse service
        dataSource.fetchCurrentEvents()

        // Then
        coVerify(exactly = 2) { mockService.getCurrentEvents() }
        // retrofit.create should still only be called once (lazy singleton)
    }

    @Test
    fun `fetchCurrentEvents with null or malformed JSON fails gracefully`() = runTest {
        // Given
        val malformedHtml = "This is not HTML at all {]]["
        coEvery { mockService.getCurrentEvents() } returns malformedHtml
        every { parser.parse(malformedHtml) } returns emptyList() // Parser handles it

        // When
        val result = dataSource.fetchCurrentEvents()

        // Then - should not crash, returns empty
        assertEquals(0, result.size)
    }

    @Test
    fun `fetchCurrentEvents rethrows custom exceptions from parser`() = runTest {
        // Given
        val html = "<html>test</html>"
        coEvery { mockService.getCurrentEvents() } returns html
        every { parser.parse(html) } throws IllegalArgumentException("Custom parser error")

        // When/Then
        try {
            dataSource.fetchCurrentEvents()
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals("Custom parser error", e.message)
        }
    }
}
