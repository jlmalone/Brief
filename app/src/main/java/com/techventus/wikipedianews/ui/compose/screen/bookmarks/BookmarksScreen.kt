package com.techventus.wikipedianews.ui.compose.screen.bookmarks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.ui.compose.component.ErrorView
import com.techventus.wikipedianews.ui.compose.component.LoadingIndicator
import com.techventus.wikipedianews.ui.compose.component.NewsArticleCard
import com.techventus.wikipedianews.ui.compose.component.SectionHeader

/**
 * Bookmarks screen - displays user's bookmarked articles.
 *
 * Shows all bookmarked articles grouped by section.
 * Allows removing individual bookmarks or clearing all.
 */
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showClearAllDialog by remember { mutableStateOf(false) }

    BookmarksScreenContent(
        uiState = uiState,
        onArticleClick = { article ->
            // Open article URL in browser
            if (article.url.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                context.startActivity(intent)
            }
        },
        onBookmarkToggle = { articleId, _ ->
            // Always remove bookmark (since we're on bookmarks screen)
            viewModel.removeBookmark(articleId)
        },
        onClearAll = { showClearAllDialog = true }
    )

    // Clear all confirmation dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All Bookmarks?") },
            text = { Text("This will remove all bookmarked articles. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllBookmarks()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Bookmarks screen content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksScreenContent(
    uiState: BookmarksUiState,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (String, Boolean) -> Unit,
    onClearAll: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks") },
                colors = TopAppBarDefaults.topAppBarColors(),
                actions = {
                    // Show clear all button only when there are bookmarks
                    if (uiState is BookmarksUiState.Success) {
                        IconButton(onClick = onClearAll) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear all bookmarks",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is BookmarksUiState.Loading -> {
                    LoadingIndicator(message = "Loading bookmarks...")
                }

                is BookmarksUiState.Success -> {
                    BookmarksContent(
                        sections = uiState.sections,
                        onArticleClick = onArticleClick,
                        onBookmarkToggle = onBookmarkToggle
                    )
                }

                is BookmarksUiState.Empty -> {
                    EmptyBookmarksView()
                }

                is BookmarksUiState.Error -> {
                    ErrorView(
                        message = uiState.message,
                        onRetry = null // No retry for bookmarks
                    )
                }
            }
        }
    }
}

/**
 * Bookmarks content list.
 */
@Composable
private fun BookmarksContent(
    sections: List<com.techventus.wikipedianews.model.domain.NewsSection>,
    onArticleClick: (NewsArticle) -> Unit,
    onBookmarkToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        sections.forEach { section ->
            // Section header
            item(key = "header_${section.header}") {
                SectionHeader(text = section.header)
            }

            // Section articles
            items(
                items = section.articles,
                key = { article -> article.id }
            ) { article ->
                NewsArticleCard(
                    article = article,
                    onArticleClick = onArticleClick,
                    onBookmarkToggle = onBookmarkToggle
                )
            }
        }
    }
}

/**
 * Empty state view for bookmarks.
 */
@Composable
private fun EmptyBookmarksView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "📚",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "No Bookmarks Yet",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Bookmark articles from the News tab to read them later",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
