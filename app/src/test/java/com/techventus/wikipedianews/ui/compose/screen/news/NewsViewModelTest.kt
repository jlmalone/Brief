package com.techventus.wikipedianews.ui.compose.screen.news

import app.cash.turbine.test
import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.model.domain.NewsSection
import com.techventus.wikipedianews.model.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NewsViewModel.
 *
 * Tests state management, user actions, and repository interactions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    private lateinit var repository: NewsRepository
    private lateinit var viewModel: NewsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        // Given
        coEvery { repository.observeNews() } returns flowOf(emptyList())
        coEvery { repository.refreshNews() } returns Result.success(Unit)

        // When
        viewModel = NewsViewModel(repository)

        // Then
        viewModel.uiState.test {
            assertEquals(NewsUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `observeNews with data updates state to Success`() = runTest {
        // Given
        val testSections = listOf(
            NewsSection(
                header = "Test Section",
                articles = listOf(
                    NewsArticle(
                        id = "1",
                        title = "Test Article",
                        htmlContent = "<p>Test</p>",
                        url = "https://example.com",
                        timestamp = 123456L
                    )
                )
            )
        )
        coEvery { repository.observeNews() } returns flowOf(testSections)
        coEvery { repository.refreshNews() } returns Result.success(Unit)

        // When
        viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is NewsUiState.Success)
            assertEquals(testSections, (state as NewsUiState.Success).sections)
        }
    }

    @Test
    fun `observeNews with empty data shows Empty state`() = runTest {
        // Given
        coEvery { repository.observeNews() } returns flowOf(emptyList())
        coEvery { repository.refreshNews() } returns Result.failure(Exception("No data"))

        // When
        viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            assertEquals(NewsUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `refresh success updates state`() = runTest {
        // Given
        val testSections = listOf(
            NewsSection("Test", listOf(NewsArticle("1", "Test", "Content", "url", 123L)))
        )
        coEvery { repository.observeNews() } returns flowOf(testSections)
        coEvery { repository.refreshNews() } returns Result.success(Unit)

        viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        coVerify { repository.refreshNews() }
    }

    @Test
    fun `refresh failure with no cache shows Error state`() = runTest {
        // Given
        coEvery { repository.observeNews() } returns flowOf(emptyList())
        coEvery { repository.refreshNews() } returns Result.failure(Exception("Network error"))
        coEvery { repository.getCachedNewsCount() } returns 0

        viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is NewsUiState.Error)
            assertTrue((state as NewsUiState.Error).message.contains("Network error"))
        }
    }

    @Test
    fun `refresh failure with cache keeps showing cached data`() = runTest {
        // Given
        val cachedSections = listOf(
            NewsSection("Cached", listOf(NewsArticle("1", "Cached", "Old", "url", 123L)))
        )
        coEvery { repository.observeNews() } returns flowOf(cachedSections)
        coEvery { repository.refreshNews() } returns Result.failure(Exception("Network error"))
        coEvery { repository.getCachedNewsCount() } returns 5

        viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is NewsUiState.Success)
        }
    }

    @Test
    fun `retry calls refresh`() = runTest {
        // Given
        coEvery { repository.observeNews() } returns flowOf(emptyList())
        coEvery { repository.refreshNews() } returns Result.success(Unit)

        viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        coVerify(atLeast = 2) { repository.refreshNews() } // init + retry
    }

    @Test
    fun `clearCache calls repository clearCache`() = runTest {
        // Given
        coEvery { repository.observeNews() } returns flowOf(emptyList())
        coEvery { repository.refreshNews() } returns Result.success(Unit)
        coEvery { repository.clearCache() } returns Unit

        viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.clearCache()
        advanceUntilIdle()

        // Then
        coVerify { repository.clearCache() }
    }
}
