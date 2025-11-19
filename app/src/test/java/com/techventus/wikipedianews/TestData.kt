package com.techventus.wikipedianews

import com.techventus.wikipedianews.model.database.entity.NewsArticleEntity
import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.model.domain.NewsSection

/**
 * Test data factory for creating consistent test fixtures.
 *
 * Centralizes test data creation to ensure consistency across all test files
 * and make tests easier to maintain.
 */
object TestData {

    /**
     * Create a test NewsArticle with optional overrides.
     */
    fun createNewsArticle(
        id: String = "test-article-1",
        title: String = "Test Article Title",
        htmlContent: String = "<p>Test article content</p>",
        url: String = "https://en.wikipedia.org/wiki/Test",
        timestamp: Long = 1234567890L,
        isBookmarked: Boolean = false
    ): NewsArticle {
        return NewsArticle(
            id = id,
            title = title,
            htmlContent = htmlContent,
            url = url,
            timestamp = timestamp,
            isBookmarked = isBookmarked
        )
    }

    /**
     * Create a test NewsSection with optional overrides.
     */
    fun createNewsSection(
        header: String = "Test Section",
        articles: List<NewsArticle> = listOf(createNewsArticle())
    ): NewsSection {
        return NewsSection(
            header = header,
            articles = articles
        )
    }

    /**
     * Create a test NewsArticleEntity with optional overrides.
     */
    fun createNewsArticleEntity(
        id: String = "test-article-1",
        sectionHeader: String = "Test Section",
        title: String = "Test Article Title",
        htmlContent: String = "<p>Test article content</p>",
        url: String = "https://en.wikipedia.org/wiki/Test",
        timestamp: Long = 1234567890L,
        cachedAt: Long = 1234567900L,
        isBookmarked: Boolean = false
    ): NewsArticleEntity {
        return NewsArticleEntity(
            id = id,
            sectionHeader = sectionHeader,
            title = title,
            htmlContent = htmlContent,
            url = url,
            timestamp = timestamp,
            cachedAt = cachedAt,
            isBookmarked = isBookmarked
        )
    }

    /**
     * Create multiple test articles with sequential IDs.
     */
    fun createMultipleArticles(count: Int = 3): List<NewsArticle> {
        return (1..count).map { index ->
            createNewsArticle(
                id = "test-article-$index",
                title = "Test Article $index",
                timestamp = 1234567890L + index
            )
        }
    }

    /**
     * Create multiple test entities with sequential IDs.
     */
    fun createMultipleEntities(
        count: Int = 3,
        sectionHeader: String = "Test Section"
    ): List<NewsArticleEntity> {
        return (1..count).map { index ->
            createNewsArticleEntity(
                id = "test-article-$index",
                sectionHeader = sectionHeader,
                title = "Test Article $index",
                timestamp = 1234567890L + index,
                cachedAt = 1234567900L + index
            )
        }
    }

    /**
     * Create multiple test sections with articles.
     */
    fun createMultipleSections(sectionCount: Int = 2, articlesPerSection: Int = 2): List<NewsSection> {
        return (1..sectionCount).map { sectionIndex ->
            NewsSection(
                header = "Section $sectionIndex",
                articles = (1..articlesPerSection).map { articleIndex ->
                    createNewsArticle(
                        id = "section-$sectionIndex-article-$articleIndex",
                        title = "Article $articleIndex in Section $sectionIndex"
                    )
                }
            )
        }
    }

    /**
     * Create entities grouped by different sections.
     */
    fun createEntitiesInMultipleSections(
        sections: List<String> = listOf("Section A", "Section B")
    ): List<NewsArticleEntity> {
        return sections.flatMapIndexed { sectionIndex, sectionHeader ->
            (1..2).map { articleIndex ->
                createNewsArticleEntity(
                    id = "section-$sectionIndex-article-$articleIndex",
                    sectionHeader = sectionHeader,
                    title = "Article $articleIndex in $sectionHeader"
                )
            }
        }
    }
}
