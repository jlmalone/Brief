package com.techventus.wikipedianews.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.techventus.wikipedianews.model.domain.NewsArticle

/**
 * Room entity for storing news articles in the local database.
 */
@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey
    val id: String,
    val sectionHeader: String,
    val title: String,
    val htmlContent: String,
    val url: String,
    val timestamp: Long,
    val cachedAt: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)

/**
 * Extension function to convert entity to domain model.
 */
fun NewsArticleEntity.toDomain(): NewsArticle {
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
 * Extension function to convert domain model to entity.
 */
fun NewsArticle.toEntity(sectionHeader: String): NewsArticleEntity {
    return NewsArticleEntity(
        id = id,
        sectionHeader = sectionHeader,
        title = title,
        htmlContent = htmlContent,
        url = url,
        timestamp = timestamp,
        isBookmarked = isBookmarked
    )
}
