package com.techventus.wikipedianews.model.domain

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techventus.wikipedianews.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for NewsArticle domain model.
 *
 * Tests:
 * - Data class behavior
 * - Parcelable implementation
 * - Equality and hashCode
 * - Copy functionality
 * - Default values
 */
@RunWith(AndroidJUnit4::class)
class NewsArticleTest {

    @Test
    fun `NewsArticle creates instance with all fields`() {
        // When
        val article = NewsArticle(
            id = "test-id",
            title = "Test Title",
            htmlContent = "<p>Content</p>",
            url = "https://test.com",
            timestamp = 123456L,
            isBookmarked = true
        )

        // Then
        assertEquals("test-id", article.id)
        assertEquals("Test Title", article.title)
        assertEquals("<p>Content</p>", article.htmlContent)
        assertEquals("https://test.com", article.url)
        assertEquals(123456L, article.timestamp)
        assertTrue(article.isBookmarked)
    }

    @Test
    fun `NewsArticle default timestamp is current time`() {
        // Given
        val beforeTime = System.currentTimeMillis()

        // When
        val article = NewsArticle(
            id = "id",
            title = "title",
            htmlContent = "content",
            url = "url"
        )

        val afterTime = System.currentTimeMillis()

        // Then
        assertTrue(article.timestamp >= beforeTime)
        assertTrue(article.timestamp <= afterTime)
    }

    @Test
    fun `NewsArticle default isBookmarked is false`() {
        // When
        val article = NewsArticle(
            id = "id",
            title = "title",
            htmlContent = "content",
            url = "url"
        )

        // Then
        assertFalse(article.isBookmarked)
    }

    @Test
    fun `NewsArticle data class copy works correctly`() {
        // Given
        val original = TestData.createNewsArticle(
            id = "original",
            title = "Original Title"
        )

        // When
        val copy = original.copy(title = "New Title")

        // Then
        assertEquals("original", copy.id) // Unchanged
        assertEquals("New Title", copy.title) // Changed
        assertEquals(original.htmlContent, copy.htmlContent) // Unchanged
        assertEquals(original.url, copy.url) // Unchanged
    }

    @Test
    fun `NewsArticle copy can toggle bookmark`() {
        // Given
        val unbookmarked = TestData.createNewsArticle(isBookmarked = false)

        // When
        val bookmarked = unbookmarked.copy(isBookmarked = true)

        // Then
        assertFalse(unbookmarked.isBookmarked)
        assertTrue(bookmarked.isBookmarked)
    }

    @Test
    fun `NewsArticle equality works correctly`() {
        // Given
        val article1 = NewsArticle(
            id = "same-id",
            title = "Title",
            htmlContent = "Content",
            url = "url",
            timestamp = 100L,
            isBookmarked = false
        )
        val article2 = NewsArticle(
            id = "same-id",
            title = "Title",
            htmlContent = "Content",
            url = "url",
            timestamp = 100L,
            isBookmarked = false
        )

        // Then
        assertEquals(article1, article2)
        assertEquals(article1.hashCode(), article2.hashCode())
    }

    @Test
    fun `NewsArticle inequality with different id`() {
        // Given
        val article1 = TestData.createNewsArticle(id = "id1")
        val article2 = TestData.createNewsArticle(id = "id2")

        // Then
        assertNotEquals(article1, article2)
    }

    @Test
    fun `NewsArticle inequality with different title`() {
        // Given
        val article1 = TestData.createNewsArticle(title = "Title A")
        val article2 = TestData.createNewsArticle(title = "Title B")

        // Then
        assertNotEquals(article1, article2)
    }

    @Test
    fun `NewsArticle inequality with different bookmark status`() {
        // Given
        val article1 = TestData.createNewsArticle(isBookmarked = true)
        val article2 = TestData.createNewsArticle(isBookmarked = false)

        // Then
        assertNotEquals(article1, article2)
    }

    @Test
    fun `NewsArticle parcelable write and read`() {
        // Given
        val original = NewsArticle(
            id = "parcel-id",
            title = "Parcel Title",
            htmlContent = "<p>Parcel Content</p>",
            url = "https://parcel.url",
            timestamp = 999999L,
            isBookmarked = true
        )

        // When
        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val fromParcel = NewsArticle.CREATOR.createFromParcel(parcel)

        // Then
        assertEquals(original, fromParcel)
        assertEquals(original.id, fromParcel.id)
        assertEquals(original.title, fromParcel.title)
        assertEquals(original.htmlContent, fromParcel.htmlContent)
        assertEquals(original.url, fromParcel.url)
        assertEquals(original.timestamp, fromParcel.timestamp)
        assertEquals(original.isBookmarked, fromParcel.isBookmarked)

        parcel.recycle()
    }

    @Test
    fun `NewsArticle parcelable handles empty strings`() {
        // Given
        val original = NewsArticle(
            id = "",
            title = "",
            htmlContent = "",
            url = "",
            timestamp = 0L,
            isBookmarked = false
        )

        // When
        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val fromParcel = NewsArticle.CREATOR.createFromParcel(parcel)

        // Then
        assertEquals(original, fromParcel)

        parcel.recycle()
    }

    @Test
    fun `NewsArticle parcelable preserves bookmark state`() {
        // Given
        val bookmarked = TestData.createNewsArticle(isBookmarked = true)
        val unbookmarked = TestData.createNewsArticle(isBookmarked = false)

        // When
        val parcel1 = Parcel.obtain()
        bookmarked.writeToParcel(parcel1, 0)
        parcel1.setDataPosition(0)
        val fromParcel1 = NewsArticle.CREATOR.createFromParcel(parcel1)

        val parcel2 = Parcel.obtain()
        unbookmarked.writeToParcel(parcel2, 0)
        parcel2.setDataPosition(0)
        val fromParcel2 = NewsArticle.CREATOR.createFromParcel(parcel2)

        // Then
        assertTrue(fromParcel1.isBookmarked)
        assertFalse(fromParcel2.isBookmarked)

        parcel1.recycle()
        parcel2.recycle()
    }

    @Test
    fun `NewsArticle toString contains all fields`() {
        // Given
        val article = NewsArticle(
            id = "test-id",
            title = "Test Title",
            htmlContent = "content",
            url = "url",
            timestamp = 123L,
            isBookmarked = true
        )

        // When
        val string = article.toString()

        // Then - toString should contain field names and values
        assertTrue(string.contains("test-id"))
        assertTrue(string.contains("Test Title"))
    }

    @Test
    fun `NewsArticle handles long HTML content`() {
        // Given
        val longContent = "<p>" + "Lorem ipsum ".repeat(1000) + "</p>"
        val article = NewsArticle(
            id = "long",
            title = "Long Article",
            htmlContent = longContent,
            url = "url"
        )

        // When/Then - should not crash
        assertEquals(longContent, article.htmlContent)

        // Parcelable should handle it too
        val parcel = Parcel.obtain()
        article.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val fromParcel = NewsArticle.CREATOR.createFromParcel(parcel)

        assertEquals(longContent, fromParcel.htmlContent)
        parcel.recycle()
    }

    @Test
    fun `NewsArticle handles special characters in title`() {
        // Given
        val specialTitle = "Article with <special> & \"characters\" 'and' symbols: €£¥"
        val article = TestData.createNewsArticle(title = specialTitle)

        // When/Then
        assertEquals(specialTitle, article.title)

        // Verify parcelable
        val parcel = Parcel.obtain()
        article.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val fromParcel = NewsArticle.CREATOR.createFromParcel(parcel)

        assertEquals(specialTitle, fromParcel.title)
        parcel.recycle()
    }

    @Test
    fun `NewsArticle handles URL with special characters`() {
        // Given
        val complexUrl = "https://en.wikipedia.org/wiki/Main_Page?search=test&lang=en#section"
        val article = TestData.createNewsArticle(url = complexUrl)

        // When/Then
        assertEquals(complexUrl, article.url)
    }

    @Test
    fun `NewsArticle with very old timestamp`() {
        // Given
        val oldTimestamp = 0L

        // When
        val article = TestData.createNewsArticle(timestamp = oldTimestamp)

        // Then
        assertEquals(0L, article.timestamp)
    }

    @Test
    fun `NewsArticle with future timestamp`() {
        // Given
        val futureTimestamp = System.currentTimeMillis() + 1000000L

        // When
        val article = TestData.createNewsArticle(timestamp = futureTimestamp)

        // Then
        assertEquals(futureTimestamp, article.timestamp)
    }
}
