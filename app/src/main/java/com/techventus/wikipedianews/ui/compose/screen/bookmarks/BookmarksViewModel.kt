package com.techventus.wikipedianews.ui.compose.screen.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techventus.wikipedianews.model.domain.NewsSection
import com.techventus.wikipedianews.model.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Bookmarks screen.
 *
 * Observes bookmarked articles from repository and provides
 * bookmark management actions.
 */
@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookmarksUiState>(BookmarksUiState.Loading)
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init {
        Timber.d("BookmarksViewModel initialized")
        observeBookmarks()
    }

    /**
     * Observe bookmarked articles from repository.
     */
    private fun observeBookmarks() {
        viewModelScope.launch {
            repository.observeBookmarkedNews()
                .catch { exception ->
                    Timber.e(exception, "Error observing bookmarks")
                    _uiState.value = BookmarksUiState.Error(
                        message = exception.message ?: "Failed to load bookmarks"
                    )
                }
                .collect { sections ->
                    if (sections.isNotEmpty()) {
                        Timber.d("Received ${sections.size} bookmarked sections")
                        _uiState.value = BookmarksUiState.Success(sections)
                    } else {
                        _uiState.value = BookmarksUiState.Empty
                    }
                }
        }
    }

    /**
     * Remove bookmark from an article.
     */
    fun removeBookmark(articleId: String) {
        viewModelScope.launch {
            try {
                Timber.d("Removing bookmark for $articleId")
                repository.toggleBookmark(articleId, false)
                // UI will update automatically via observeBookmarks()
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove bookmark")
            }
        }
    }

    /**
     * Clear all bookmarks (with confirmation from UI).
     */
    fun clearAllBookmarks() {
        viewModelScope.launch {
            try {
                Timber.d("Clearing all bookmarks")
                // Get all bookmarked articles and unbookmark them
                val state = _uiState.value
                if (state is BookmarksUiState.Success) {
                    state.sections.forEach { section ->
                        section.articles.forEach { article ->
                            repository.toggleBookmark(article.id, false)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear all bookmarks")
            }
        }
    }
}

/**
 * UI state for Bookmarks screen.
 */
sealed interface BookmarksUiState {
    /**
     * Loading state - fetching bookmarks.
     */
    data object Loading : BookmarksUiState

    /**
     * Success state - bookmarks available.
     */
    data class Success(val sections: List<NewsSection>) : BookmarksUiState

    /**
     * Empty state - no bookmarks.
     */
    data object Empty : BookmarksUiState

    /**
     * Error state - something went wrong.
     */
    data class Error(val message: String) : BookmarksUiState
}
