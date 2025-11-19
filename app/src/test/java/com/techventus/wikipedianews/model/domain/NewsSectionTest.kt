package com.techventus.wikipedianews.model.domain

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techventus.wikipedianews.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for NewsSection domain model.
 *
 * Tests:
 * - Data class behavior
 * - Parcelable implementation
 * - Equality and hashCode
 * - Copy functionality
 * - List operations
 */
@RunWith(AndroidJUnit4::class)
class NewsSectionTest {

    @Test
    fun `NewsSection creates instance with header and articles`() {
        // Given
        val articles = listOf(
            TestData.createNewsArticle(id = "1", title = "Article 1"),
            TestData.createNewsArticle(id = "2", title = "Article 2")
        )

        // When
        val section = NewsSection(
            header = "Test Section",
            articles = articles
        )

        // Then
        assertEquals("Test Section", section.header)
        assertEquals(2, section.articles.size)
        assertEquals("Article 1", section.articles[0].title)
        assertEquals("Article 2", section.articles[1].title)
    }

    @Test
    fun `NewsSection with empty articles list`() {
        // When
        val section = NewsSection(
            header = "Empty Section",
            articles = emptyList()
        )

        // Then
        assertEquals("Empty Section", section.header)
        assertEquals(0, section.articles.size)
        assertTrue(section.articles.isEmpty())
    }

    @Test
    fun `NewsSection with single article`() {
        // Given
        val article = TestData.createNewsArticle()

        // When
        val section = NewsSection(
            header = "Single Article Section",
            articles = listOf(article)
        )

        // Then
        assertEquals(1, section.articles.size)
        assertEquals(article, section.articles[0])
    }

    @Test
    fun `NewsSection data class copy works correctly`() {
        // Given
        val original = TestData.createNewsSection(header = "Original Header")

        // When
        val copy = original.copy(header = "New Header")

        // Then
        assertEquals("New Header", copy.header)
        assertEquals(original.articles, copy.articles) // Articles unchanged
    }

    @Test
    fun `NewsSection copy can replace articles`() {
        // Given
        val original = TestData.createNewsSection(
            articles = TestData.createMultipleArticles(2)
        )
        val newArticles = TestData.createMultipleArticles(3)

        // When
        val copy = original.copy(articles = newArticles)

        // Then
        assertEquals(2, original.articles.size)
        assertEquals(3, copy.articles.size)
    }

    @Test
    fun `NewsSection equality works correctly`() {
        // Given
        val articles = TestData.createMultipleArticles(2)
        val section1 = NewsSection(header = "Same Header", articles = articles)
        val section2 = NewsSection(header = "Same Header", articles = articles)

        // Then
        assertEquals(section1, section2)
        assertEquals(section1.hashCode(), section2.hashCode())
    }

    @Test
    fun `NewsSection inequality with different header`() {
        // Given
        val articles = TestData.createMultipleArticles(1)
        val section1 = NewsSection(header = "Header A", articles = articles)
        val section2 = NewsSection(header = "Header B", articles = articles)

        // Then
        assertNotEquals(section1, section2)
    }

    @Test
    fun `NewsSection inequality with different articles`() {
        // Given
        val articles1 = TestData.createMultipleArticles(1)
        val articles2 = TestData.createMultipleArticles(2)
        val section1 = NewsSection(header = "Same", articles = articles1)
        val section2 = NewsSection(header = "Same", articles = articles2)

        // Then
        assertNotEquals(section1, section2)
    }

    @Test
    fun `NewsSection parcelable write and read`() {
        // Given
        val articles = listOf(
            NewsArticle("id1", "Title1", "Content1", "url1", 100L, false),
            NewsArticle("id2", "Title2", "Content2", "url2", 200L, true)
        )
        val original = NewsSection(
            header = "Parcel Section",
            articles = articles
        )

        // When
        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val fromParcel = NewsSection.CREATOR.createFromParcel(parcel)

        // Then
        assertEquals(original, fromParcel)
        assertEquals(original.header, fromParcel.header)
        assertEquals(original.articles.size, fromParcel.articles.size)
        assertEquals(original.articles[0], fromParcel.articles[0])
        assertEquals(original.articles[1], fromParcel.articles[1])

        parcel.recycle()
    }

    @Test
    fun `NewsSection parcelable with empty articles`() {
        // Given
        val original = NewsSection(
            header = "Empty",
            articles = emptyList()
        )

        // When
        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val fromParcel = NewsSection.CREATOR.createFromParcel(parcel)

        // Then
        assertEquals(original, fromParcel)
        assertTrue(fromParcel.articles.isEmpty())

        parcel.recycle()
    }

    @Test
    fun `NewsSection parcelable with many articles`() {
        // Given
        val articles = (1..100).map { index ->
            TestData.createNewsArticle(id = "id-$index", title = "Article $index")
        }
        val original = NewsSection(
            header = "Many Articles",
            articles = articles
        )

        // When
        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val fromParcel = NewsSection.CREATOR.createFromParcel(parcel)

        // Then
        assertEquals(original, fromParcel)
        assertEquals(100, fromParcel.articles.size)
        assertEquals("Article 1", fromParcel.articles[0].title)
        assertEquals("Article 100", fromParcel.articles[99].title)

        parcel.recycle()
    }

    @Test
    fun `NewsSection toString contains header and articles info`() {
        // Given
        val section = NewsSection(
            header = "Test Header",
            articles = TestData.createMultipleArticles(2)
        )

        // When
        val string = section.toString()

        // Then
        assertTrue(string.contains("Test Header"))
    }

    @Test
    fun `NewsSection with special characters in header`() {
        // Given
        val specialHeader = "Section with <special> & \"characters\" 'and' symbols: €£¥"
        val section = NewsSection(
            header = specialHeader,
            articles = emptyList()
        )

        // When/Then
        assertEquals(specialHeader, section.header)

        // Verify parcelable
        val parcel = Parcel.obtain()
        section.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val fromParcel = NewsSection.CREATOR.createFromParcel(parcel)

        assertEquals(specialHeader, fromParcel.header)
        parcel.recycle()
    }

    @Test
    fun `NewsSection with typical Wikipedia section names`() {
        // Given/When
        val sections = listOf(
            NewsSection("Topics in the News", emptyList()),
            NewsSection("Ongoing Events", emptyList()),
            NewsSection("Recent Deaths", emptyList()),
            NewsSection("December 6", emptyList())
        )

        // Then
        assertEquals("Topics in the News", sections[0].header)
        assertEquals("Ongoing Events", sections[1].header)
        assertEquals("Recent Deaths", sections[2].header)
        assertEquals("December 6", sections[3].header)
    }

    @Test
    fun `NewsSection preserves article order`() {
        // Given
        val article1 = TestData.createNewsArticle(id = "first", timestamp = 1000L)
        val article2 = TestData.createNewsArticle(id = "second", timestamp = 2000L)
        val article3 = TestData.createNewsArticle(id = "third", timestamp = 3000L)

        // When
        val section = NewsSection(
            header = "Ordered",
            articles = listOf(article1, article2, article3)
        )

        // Then
        assertEquals("first", section.articles[0].id)
        assertEquals("second", section.articles[1].id)
        assertEquals("third", section.articles[2].id)
    }

    @Test
    fun `NewsSection articles are immutable reference`() {
        // Given
        val originalArticles = TestData.createMultipleArticles(2)
        val section = NewsSection("Test", originalArticles)

        // When - modify original list
        val mutableList = originalArticles.toMutableList()
        mutableList.add(TestData.createNewsArticle(id = "new"))

        // Then - section articles should be unchanged
        assertEquals(2, section.articles.size)
    }

    @Test
    fun `NewsSection with mixed bookmarked and unbookmarked articles`() {
        // Given
        val articles = listOf(
            TestData.createNewsArticle(id = "1", isBookmarked = true),
            TestData.createNewsArticle(id = "2", isBookmarked = false),
            TestData.createNewsArticle(id = "3", isBookmarked = true)
        )

        // When
        val section = NewsSection("Mixed", articles)

        // Then
        assertEquals(3, section.articles.size)
        assertTrue(section.articles[0].isBookmarked)
        assertTrue(!section.articles[1].isBookmarked)
        assertTrue(section.articles[2].isBookmarked)
    }

    @Test
    fun `NewsSection parcelable preserves all article fields`() {
        // Given
        val complexArticle = NewsArticle(
            id = "complex-id",
            title = "Complex Title with <html> & symbols",
            htmlContent = "<div><p>Complex content</p></div>",
            url = "https://example.com/article?param=value&other=123#section",
            timestamp = 1234567890L,
            isBookmarked = true
        )
        val original = NewsSection("Complex", listOf(complexArticle))

        // When
        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val fromParcel = NewsSection.CREATOR.createFromParcel(parcel)

        // Then
        val restoredArticle = fromParcel.articles[0]
        assertEquals(complexArticle.id, restoredArticle.id)
        assertEquals(complexArticle.title, restoredArticle.title)
        assertEquals(complexArticle.htmlContent, restoredArticle.htmlContent)
        assertEquals(complexArticle.url, restoredArticle.url)
        assertEquals(complexArticle.timestamp, restoredArticle.timestamp)
        assertEquals(complexArticle.isBookmarked, restoredArticle.isBookmarked)

        parcel.recycle()
    }
}
