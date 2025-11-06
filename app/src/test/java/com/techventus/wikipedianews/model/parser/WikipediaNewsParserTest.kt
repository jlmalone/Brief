package com.techventus.wikipedianews.model.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for WikipediaNewsParser.
 *
 * Tests HTML parsing logic with sample Wikipedia HTML.
 */
class WikipediaNewsParserTest {

    private lateinit var parser: WikipediaNewsParser

    @Before
    fun setup() {
        parser = WikipediaNewsParser()
    }

    @Test
    fun `parse empty HTML returns empty list`() {
        // When
        val result = parser.parse("")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parse blank HTML returns empty list`() {
        // When
        val result = parser.parse("   ")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parse HTML with Topics in the News section`() {
        // Given
        val html = """
            <html>
            <body>
                <div aria-labelledby="Topics_in_the_news">
                    <ul>
                        <li>Test article 1 with <a href="/wiki/Test">link</a></li>
                        <li>Test article 2</li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        // When
        val result = parser.parse(html)

        // Then
        assertEquals(1, result.size)
        assertEquals("Topics in the News", result[0].header)
        assertEquals(2, result[0].articles.size)
    }

    @Test
    fun `parse HTML with Ongoing events section`() {
        // Given
        val html = """
            <html>
            <body>
                <div aria-labelledby="Ongoing_events">
                    <ul>
                        <li>Ongoing event 1</li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        // When
        val result = parser.parse(html)

        // Then
        assertEquals(1, result.size)
        assertEquals("Ongoing", result[0].header)
        assertEquals(1, result[0].articles.size)
    }

    @Test
    fun `parse HTML with Recent deaths section`() {
        // Given
        val html = """
            <html>
            <body>
                <div aria-labelledby="Recent_deaths">
                    <ul>
                        <li>Person A</li>
                        <li>Person B</li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        // When
        val result = parser.parse(html)

        // Then
        assertEquals(1, result.size)
        assertEquals("Recent Deaths", result[0].header)
        assertEquals(2, result[0].articles.size)
    }

    @Test
    fun `parse HTML with multiple sections`() {
        // Given
        val html = """
            <html>
            <body>
                <div aria-labelledby="Topics_in_the_news">
                    <ul>
                        <li>Topic 1</li>
                    </ul>
                </div>
                <div aria-labelledby="Ongoing_events">
                    <ul>
                        <li>Ongoing 1</li>
                    </ul>
                </div>
                <div aria-labelledby="Recent_deaths">
                    <ul>
                        <li>Death 1</li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        // When
        val result = parser.parse(html)

        // Then
        assertEquals(3, result.size)
        assertEquals("Topics in the News", result[0].header)
        assertEquals("Ongoing", result[1].header)
        assertEquals("Recent Deaths", result[2].header)
    }

    @Test
    fun `parse HTML with daily events section`() {
        // Given
        val html = """
            <html>
            <body>
                <div class="current-events-heading">
                    <span class="summary">December 6 (Wednesday)</span>
                </div>
                <div class="current-events-content">
                    <ul>
                        <li>Daily event 1</li>
                        <li>Daily event 2</li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        // When
        val result = parser.parse(html)

        // Then
        assertTrue(result.isNotEmpty())
        val dailySection = result.firstOrNull()
        assertEquals("December 6", dailySection?.header) // Day-of-week removed
        assertEquals(2, dailySection?.articles?.size)
    }

    @Test
    fun `parse fixes relative Wikipedia URLs`() {
        // Given
        val html = """
            <html>
            <body>
                <div aria-labelledby="Topics_in_the_news">
                    <ul>
                        <li><a href="/wiki/Test">Test article</a></li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        // When
        val result = parser.parse(html)

        // Then
        val article = result[0].articles[0]
        assertTrue(article.htmlContent.contains("https://en.m.wikipedia.org/wiki/Test"))
    }

    @Test
    fun `parse extracts article URL from HTML`() {
        // Given
        val html = """
            <html>
            <body>
                <div aria-labelledby="Topics_in_the_news">
                    <ul>
                        <li><a href="/wiki/TestArticle">Test</a></li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        // When
        val result = parser.parse(html)

        // Then
        val article = result[0].articles[0]
        assertTrue(article.url.contains("wikipedia.org/wiki/TestArticle"))
    }

    @Test
    fun `parse generates stable article IDs`() {
        // Given
        val html = """
            <html>
            <body>
                <div aria-labelledby="Topics_in_the_news">
                    <ul>
                        <li>Same content</li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        // When
        val result1 = parser.parse(html)
        val result2 = parser.parse(html)

        // Then
        assertEquals(result1[0].articles[0].id, result2[0].articles[0].id)
    }

    @Test
    fun `parse with missing section returns partial results`() {
        // Given - only has Topics, missing Ongoing and Deaths
        val html = """
            <html>
            <body>
                <div aria-labelledby="Topics_in_the_news">
                    <ul>
                        <li>Topic 1</li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        // When
        val result = parser.parse(html)

        // Then
        assertEquals(1, result.size)
        assertEquals("Topics in the News", result[0].header)
    }

    @Test
    fun `parse handles malformed HTML gracefully`() {
        // Given
        val html = """
            <html>
            <body>
                <div aria-labelledby="Topics_in_the_news">
                    <ul>
                        <li>Unclosed tag
                    </ul>
                </div>
            </body>
        """.trimIndent()

        // When
        val result = parser.parse(html)

        // Then - Jsoup should handle malformed HTML
        assertTrue(result.isEmpty() || result.isNotEmpty()) // Just shouldn't crash
    }

    @Test
    fun `parse invalid HTML returns empty list`() {
        // Given
        val html = "Not valid HTML at all"

        // When
        val result = parser.parse(html)

        // Then
        assertTrue(result.isEmpty())
    }
}
