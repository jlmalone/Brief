package com.techventus.wikipedianews.ui.compose.screen.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.ui.compose.component.ErrorView
import com.techventus.wikipedianews.ui.compose.component.LoadingIndicator
import com.techventus.wikipedianews.ui.compose.component.NewsArticleCard
import com.techventus.wikipedianews.ui.compose.component.SectionHeader

/**
 * News screen - main screen of the app.
 *
 * Displays news sections with articles.
 * Following android-template pattern for Compose screens.
 */
@Composable
fun NewsScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val context = LocalContext.current

    NewsScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        isSearching = isSearching,
        onRefresh = viewModel::refresh,
        onArticleClick = { article ->
            // Open article URL in browser
            if (article.url.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                context.startActivity(intent)
            }
        },
        onRetry = viewModel::retry,
        onNavigateToSettings = onNavigateToSettings,
        onBookmarkToggle = viewModel::toggleBookmark,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onToggleSearch = viewModel::toggleSearch,
        onClearSearch = viewModel::clearSearch
    )
}

/**
 * News screen content.
 * Separated for easier testing and preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsScreenContent(
    uiState: NewsUiState,
    searchQuery: String,
    isSearching: Boolean,
    onRefresh: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit,
    onRetry: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBookmarkToggle: (String, Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onClearSearch: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            if (isSearching) {
                // Search bar
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search articles...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            onToggleSearch()
                            keyboardController?.hide()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search"
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors()
                )
            } else {
                // Normal top bar
                TopAppBar(
                    title = { Text("Brief - Current Events") },
                    colors = TopAppBarDefaults.topAppBarColors(),
                    actions = {
                        IconButton(onClick = onToggleSearch) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (uiState is NewsUiState.Success) {
                FloatingActionButton(
                    onClick = onRefresh
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState is NewsUiState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is NewsUiState.Loading -> {
                    LoadingIndicator()
                }

                is NewsUiState.Success -> {
                    NewsContent(
                        sections = uiState.sections,
                        onArticleClick = onArticleClick,
                        onBookmarkToggle = onBookmarkToggle
                    )
                }

                is NewsUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isSearching && searchQuery.isNotEmpty()) {
                                "No results found for \"$searchQuery\""
                            } else {
                                "No news available. Pull to refresh."
                            },
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is NewsUiState.Error -> {
                    ErrorView(
                        message = uiState.message,
                        onRetry = if (uiState.canRetry) onRetry else null
                    )
                }
            }
        }
    }
}

/**
 * News content list.
 */
@Composable
private fun NewsContent(
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
