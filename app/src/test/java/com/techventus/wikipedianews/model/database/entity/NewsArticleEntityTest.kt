package com.techventus.wikipedianews.model.database.entity

import com.techventus.wikipedianews.TestData
import com.techventus.wikipedianews.model.domain.NewsArticle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for NewsArticleEntity and conversion extensions.
 *
 * Tests:
 * - Entity to domain conversion (toDomain)
 * - Domain to entity conversion (toEntity)
 * - Field mapping correctness
 * - Data preservation during conversion
 */
class NewsArticleEntityTest {

    @Test
    fun `toDomain converts entity to domain model correctly`() {
        // Given
        val entity = NewsArticleEntity(
            id = "test-id-123",
            sectionHeader = "Test Section",
            title = "Test Article Title",
            htmlContent = "<p>Article content</p>",
            url = "https://en.wikipedia.org/wiki/Test",
            timestamp = 1234567890L,
            cachedAt = 9876543210L,
            isBookmarked = true
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals("test-id-123", domain.id)
        assertEquals("Test Article Title", domain.title)
        assertEquals("<p>Article content</p>", domain.htmlContent)
        assertEquals("https://en.wikipedia.org/wiki/Test", domain.url)
        assertEquals(1234567890L, domain.timestamp)
        assertEquals(true, domain.isBookmarked)
    }

    @Test
    fun `toDomain preserves all fields`() {
        // Given
        val entity = TestData.createNewsArticleEntity(
            id = "id-1",
            title = "Title 1",
            htmlContent = "Content 1",
            url = "url-1",
            timestamp = 111L,
            isBookmarked = false
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.htmlContent, domain.htmlContent)
        assertEquals(entity.url, domain.url)
        assertEquals(entity.timestamp, domain.timestamp)
        assertEquals(entity.isBookmarked, domain.isBookmarked)
    }

    @Test
    fun `toDomain with unbookmarked article`() {
        // Given
        val entity = TestData.createNewsArticleEntity(isBookmarked = false)

        // When
        val domain = entity.toDomain()

        // Then
        assertFalse(domain.isBookmarked)
    }

    @Test
    fun `toDomain with bookmarked article`() {
        // Given
        val entity = TestData.createNewsArticleEntity(isBookmarked = true)

        // When
        val domain = entity.toDomain()

        // Then
        assertTrue(domain.isBookmarked)
    }

    @Test
    fun `toEntity converts domain to entity correctly`() {
        // Given
        val article = NewsArticle(
            id = "domain-id",
            title = "Domain Title",
            htmlContent = "<div>Domain Content</div>",
            url = "https://domain.url",
            timestamp = 555555L,
            isBookmarked = true
        )
        val sectionHeader = "Domain Section"

        // When
        val entity = article.toEntity(sectionHeader)

        // Then
        assertEquals("domain-id", entity.id)
        assertEquals("Domain Section", entity.sectionHeader)
        assertEquals("Domain Title", entity.title)
        assertEquals("<div>Domain Content</div>", entity.htmlContent)
        assertEquals("https://domain.url", entity.url)
        assertEquals(555555L, entity.timestamp)
        assertEquals(true, entity.isBookmarked)
    }

    @Test
    fun `toEntity preserves all domain fields`() {
        // Given
        val article = TestData.createNewsArticle(
            id = "id-2",
            title = "Title 2",
            htmlContent = "Content 2",
            url = "url-2",
            timestamp = 222L,
            isBookmarked = false
        )
        val sectionHeader = "Test Section"

        // When
        val entity = article.toEntity(sectionHeader)

        // Then
        assertEquals(article.id, entity.id)
        assertEquals(sectionHeader, entity.sectionHeader)
        assertEquals(article.title, entity.title)
        assertEquals(article.htmlContent, entity.htmlContent)
        assertEquals(article.url, entity.url)
        assertEquals(article.timestamp, entity.timestamp)
        assertEquals(article.isBookmarked, entity.isBookmarked)
    }

    @Test
    fun `toEntity sets correct sectionHeader`() {
        // Given
        val article = TestData.createNewsArticle()

        // When
        val entity1 = article.toEntity("Section A")
        val entity2 = article.toEntity("Section B")

        // Then
        assertEquals("Section A", entity1.sectionHeader)
        assertEquals("Section B", entity2.sectionHeader)
    }

    @Test
    fun `toEntity with unbookmarked article`() {
        // Given
        val article = TestData.createNewsArticle(isBookmarked = false)

        // When
        val entity = article.toEntity("Test")

        // Then
        assertFalse(entity.isBookmarked)
    }

    @Test
    fun `toEntity with bookmarked article`() {
        // Given
        val article = TestData.createNewsArticle(isBookmarked = true)

        // When
        val entity = article.toEntity("Test")

        // Then
        assertTrue(entity.isBookmarked)
    }

    @Test
    fun `round trip conversion preserves data`() {
        // Given
        val originalEntity = TestData.createNewsArticleEntity(
            id = "round-trip-id",
            sectionHeader = "Round Trip Section",
            title = "Round Trip Title",
            htmlContent = "<p>Round trip content</p>",
            url = "https://roundtrip.url",
            timestamp = 999999L,
            isBookmarked = true
        )

        // When - convert to domain and back
        val domain = originalEntity.toDomain()
        val backToEntity = domain.toEntity(originalEntity.sectionHeader)

        // Then - all fields should match (except cachedAt which is set to current time)
        assertEquals(originalEntity.id, backToEntity.id)
        assertEquals(originalEntity.sectionHeader, backToEntity.sectionHeader)
        assertEquals(originalEntity.title, backToEntity.title)
        assertEquals(originalEntity.htmlContent, backToEntity.htmlContent)
        assertEquals(originalEntity.url, backToEntity.url)
        assertEquals(originalEntity.timestamp, backToEntity.timestamp)
        assertEquals(originalEntity.isBookmarked, backToEntity.isBookmarked)
    }

    @Test
    fun `toEntity cachedAt is set to current time`() {
        // Given
        val article = TestData.createNewsArticle()
        val beforeTime = System.currentTimeMillis()

        // When
        val entity = article.toEntity("Test")

        val afterTime = System.currentTimeMillis()

        // Then
        assertTrue(entity.cachedAt >= beforeTime)
        assertTrue(entity.cachedAt <= afterTime)
    }

    @Test
    fun `NewsArticleEntity data class properties`() {
        // Given
        val entity = TestData.createNewsArticleEntity(id = "test")

        // When/Then - verify it's a proper data class
        val copy = entity.copy(title = "New Title")
        assertEquals("test", copy.id)
        assertEquals("New Title", copy.title)

        // Test equality
        val same = entity.copy()
        assertEquals(entity, same)
        assertEquals(entity.hashCode(), same.hashCode())
    }

    @Test
    fun `multiple toDomain conversions produce equal results`() {
        // Given
        val entity = TestData.createNewsArticleEntity()

        // When
        val domain1 = entity.toDomain()
        val domain2 = entity.toDomain()

        // Then
        assertEquals(domain1, domain2)
    }

    @Test
    fun `toEntity with different sections creates different entities`() {
        // Given
        val article = TestData.createNewsArticle(id = "same-id")

        // When
        val entity1 = article.toEntity("Section X")
        val entity2 = article.toEntity("Section Y")

        // Then - entities should differ only in sectionHeader
        assertEquals(entity1.id, entity2.id)
        assertEquals("Section X", entity1.sectionHeader)
        assertEquals("Section Y", entity2.sectionHeader)
    }

    @Test
    fun `toDomain handles empty strings correctly`() {
        // Given
        val entity = NewsArticleEntity(
            id = "",
            sectionHeader = "",
            title = "",
            htmlContent = "",
            url = "",
            timestamp = 0L,
            cachedAt = 0L,
            isBookmarked = false
        )

        // When
        val domain = entity.toDomain()

        // Then - should not crash, should preserve empty strings
        assertEquals("", domain.id)
        assertEquals("", domain.title)
        assertEquals("", domain.htmlContent)
        assertEquals("", domain.url)
        assertEquals(0L, domain.timestamp)
    }

    @Test
    fun `toEntity handles empty section header`() {
        // Given
        val article = TestData.createNewsArticle()

        // When
        val entity = article.toEntity("")

        // Then
        assertEquals("", entity.sectionHeader)
    }
}
