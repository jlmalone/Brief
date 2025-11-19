package com.techventus.wikipedianews.widget

import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.model.repository.NewsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NewsWidgetRepository @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend fun getLatestArticles(limit: Int = 5): List<NewsArticle> {
        return try {
            newsRepository.observeNews()
                .first()
                .flatMap { it.articles }
                .take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
