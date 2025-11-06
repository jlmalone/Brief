package com.techventus.wikipedianews.ui.compose.screen.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techventus.wikipedianews.model.domain.NewsSection
import com.techventus.wikipedianews.model.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for News screen.
 *
 * Following android-template pattern:
 * - Uses StateFlow for reactive UI state
 * - Observes repository Flow for automatic updates
 * - Handles loading, success, and error states
 * - Provides user actions (refresh, retry)
 */
@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        Timber.d("NewsViewModel initialized")
        observeNews()
        // Auto-refresh on init if no cached data
        refresh()
    }

    /**
     * Observe news from repository.
     * Automatically updates UI when cache changes.
     */
    private fun observeNews() {
        viewModelScope.launch {
            repository.observeNews()
                .catch { exception ->
                    Timber.e(exception, "Error observing news")
                    _uiState.value = NewsUiState.Error(
                        message = exception.message ?: "Failed to load news"
                    )
                }
                .collect { sections ->
                    if (sections.isNotEmpty()) {
                        Timber.d("Received ${sections.size} sections from repository")
                        _uiState.value = NewsUiState.Success(sections)
                    } else if (_uiState.value !is NewsUiState.Loading) {
                        // Only show empty if not already loading
                        _uiState.value = NewsUiState.Empty
                    }
                }
        }
    }

    /**
     * Refresh news from remote source.
     * Shows loading state during refresh.
     */
    fun refresh() {
        viewModelScope.launch {
            Timber.d("Refreshing news")
            _uiState.value = NewsUiState.Loading

            repository.refreshNews()
                .onSuccess {
                    Timber.d("Refresh successful")
                    // UI will update automatically via observeNews()
                }
                .onFailure { exception ->
                    Timber.e(exception, "Refresh failed")

                    // Check if we have cached data to show
                    val cachedCount = repository.getCachedNewsCount()
                    if (cachedCount > 0) {
                        Timber.d("Showing cached data after failed refresh")
                        // observeNews() will emit cached data
                    } else {
                        _uiState.value = NewsUiState.Error(
                            message = exception.message ?: "Failed to refresh news",
                            canRetry = true
                        )
                    }
                }
        }
    }

    /**
     * Retry after error.
     */
    fun retry() {
        Timber.d("Retrying")
        refresh()
    }

    /**
     * Clear cached news.
     */
    fun clearCache() {
        viewModelScope.launch {
            Timber.d("Clearing cache")
            repository.clearCache()
            _uiState.value = NewsUiState.Empty
        }
    }

    /**
     * Toggle bookmark status for an article.
     */
    fun toggleBookmark(articleId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            try {
                Timber.d("Toggling bookmark for $articleId to $isBookmarked")
                repository.toggleBookmark(articleId, isBookmarked)
                // UI will update automatically via observeNews()
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle bookmark")
            }
        }
    }
}

/**
 * UI state for News screen.
 * Sealed interface ensures exhaustive when statements.
 */
sealed interface NewsUiState {
    /**
     * Loading state - fetching data.
     */
    data object Loading : NewsUiState

    /**
     * Success state - data available.
     */
    data class Success(val sections: List<NewsSection>) : NewsUiState

    /**
     * Empty state - no data available.
     */
    data object Empty : NewsUiState

    /**
     * Error state - something went wrong.
     */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : NewsUiState
}
