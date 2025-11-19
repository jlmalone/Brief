package com.techventus.wikipedianews.ui.compose.screen.bookmarks

import app.cash.turbine.test
import com.techventus.wikipedianews.TestData
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
 * Unit tests for BookmarksViewModel.
 *
 * Tests:
 * - Initial state
 * - Bookmark observation and state updates
 * - Remove bookmark action
 * - Clear all bookmarks action
 * - Error handling
 * - Empty state handling
 *
 * Uses Turbine for Flow testing and MockK for repository mocking.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookmarksViewModelTest {

    private lateinit var repository: NewsRepository
    private lateinit var viewModel: BookmarksViewModel
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
        coEvery { repository.observeBookmarkedNews() } returns flowOf(emptyList())

        // When
        viewModel = BookmarksViewModel(repository)

        // Then
        viewModel.uiState.test {
            assertEquals(BookmarksUiState.Loading, awaitItem())
        }
    }

    @Test
    fun `observeBookmarks with data shows Success state`() = runTest {
        // Given
        val bookmarkedSections = listOf(
            TestData.createNewsSection(
                header = "Bookmarked Section",
                articles = listOf(
                    TestData.createNewsArticle(id = "1", title = "Bookmarked Article", isBookmarked = true)
                )
            )
        )
        coEvery { repository.observeBookmarkedNews() } returns flowOf(bookmarkedSections)

        // When
        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is BookmarksUiState.Success)
            assertEquals(1, (state as BookmarksUiState.Success).sections.size)
            assertEquals("Bookmarked Section", state.sections[0].header)
            assertEquals("Bookmarked Article", state.sections[0].articles[0].title)
        }
    }

    @Test
    fun `observeBookmarks with multiple sections shows all`() = runTest {
        // Given
        val sections = listOf(
            TestData.createNewsSection(header = "Section 1", articles = TestData.createMultipleArticles(2)),
            TestData.createNewsSection(header = "Section 2", articles = TestData.createMultipleArticles(3))
        )
        coEvery { repository.observeBookmarkedNews() } returns flowOf(sections)

        // When
        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem() as BookmarksUiState.Success
            assertEquals(2, state.sections.size)
            assertEquals(2, state.sections[0].articles.size)
            assertEquals(3, state.sections[1].articles.size)
        }
    }

    @Test
    fun `observeBookmarks with empty data shows Empty state`() = runTest {
        // Given
        coEvery { repository.observeBookmarkedNews() } returns flowOf(emptyList())

        // When
        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            assertEquals(BookmarksUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `observeBookmarks error shows Error state`() = runTest {
        // Given
        coEvery { repository.observeBookmarkedNews() } returns flowOf {
            throw RuntimeException("Database error")
        }

        // When
        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is BookmarksUiState.Error)
            assertTrue((state as BookmarksUiState.Error).message.contains("Database error"))
        }
    }

    @Test
    fun `observeBookmarks updates when bookmarks change`() = runTest {
        // Given - Flow that emits multiple times
        val firstEmission = listOf(TestData.createNewsSection())
        val secondEmission = emptyList()

        coEvery { repository.observeBookmarkedNews() } returns flowOf(firstEmission, secondEmission)

        // When
        viewModel = BookmarksViewModel(repository)

        // Then
        viewModel.uiState.test {
            // Initial Loading
            assertEquals(BookmarksUiState.Loading, awaitItem())

            // First emission - Success with data
            val firstState = awaitItem()
            assertTrue(firstState is BookmarksUiState.Success)

            // Second emission - Empty
            assertEquals(BookmarksUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `removeBookmark calls repository toggleBookmark with false`() = runTest {
        // Given
        val sections = listOf(TestData.createNewsSection())
        coEvery { repository.observeBookmarkedNews() } returns flowOf(sections)
        coEvery { repository.toggleBookmark(any(), any()) } returns Unit

        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.removeBookmark("article-123")
        advanceUntilIdle()

        // Then
        coVerify { repository.toggleBookmark("article-123", false) }
    }

    @Test
    fun `removeBookmark with multiple articles removes only specified one`() = runTest {
        // Given
        val sections = listOf(TestData.createNewsSection())
        coEvery { repository.observeBookmarkedNews() } returns flowOf(sections)
        coEvery { repository.toggleBookmark(any(), any()) } returns Unit

        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.removeBookmark("specific-article-id")
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { repository.toggleBookmark("specific-article-id", false) }
        coVerify(exactly = 0) { repository.toggleBookmark(match { it != "specific-article-id" }, any()) }
    }

    @Test
    fun `removeBookmark handles error gracefully`() = runTest {
        // Given
        val sections = listOf(TestData.createNewsSection())
        coEvery { repository.observeBookmarkedNews() } returns flowOf(sections)
        coEvery { repository.toggleBookmark(any(), any()) } throws RuntimeException("Toggle failed")

        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // When - should not crash
        viewModel.removeBookmark("article-123")
        advanceUntilIdle()

        // Then - verify it was attempted
        coVerify { repository.toggleBookmark("article-123", false) }
        // State should remain Success (error is logged, not propagated to UI)
        viewModel.uiState.test {
            assertTrue(awaitItem() is BookmarksUiState.Success)
        }
    }

    @Test
    fun `clearAllBookmarks unbookmarks all articles in all sections`() = runTest {
        // Given
        val sections = listOf(
            TestData.createNewsSection(
                header = "Section 1",
                articles = listOf(
                    TestData.createNewsArticle(id = "article-1", isBookmarked = true),
                    TestData.createNewsArticle(id = "article-2", isBookmarked = true)
                )
            ),
            TestData.createNewsSection(
                header = "Section 2",
                articles = listOf(
                    TestData.createNewsArticle(id = "article-3", isBookmarked = true)
                )
            )
        )

        // Emit sections, then empty list after clearing
        coEvery { repository.observeBookmarkedNews() } returns flowOf(sections, emptyList())
        coEvery { repository.toggleBookmark(any(), any()) } returns Unit

        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.clearAllBookmarks()
        advanceUntilIdle()

        // Then - should unbookmark all 3 articles
        coVerify { repository.toggleBookmark("article-1", false) }
        coVerify { repository.toggleBookmark("article-2", false) }
        coVerify { repository.toggleBookmark("article-3", false) }
    }

    @Test
    fun `clearAllBookmarks when state is not Success does nothing`() = runTest {
        // Given - Empty state
        coEvery { repository.observeBookmarkedNews() } returns flowOf(emptyList())

        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.clearAllBookmarks()
        advanceUntilIdle()

        // Then - toggleBookmark should not be called
        coVerify(exactly = 0) { repository.toggleBookmark(any(), any()) }
    }

    @Test
    fun `clearAllBookmarks when state is Loading does nothing`() = runTest {
        // Given
        coEvery { repository.observeBookmarkedNews() } returns flowOf(emptyList())

        viewModel = BookmarksViewModel(repository)
        // Don't advance - keep in Loading state

        // When
        viewModel.clearAllBookmarks()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { repository.toggleBookmark(any(), any()) }
    }

    @Test
    fun `clearAllBookmarks handles partial failures`() = runTest {
        // Given
        val sections = listOf(
            TestData.createNewsSection(
                articles = listOf(
                    TestData.createNewsArticle(id = "article-1"),
                    TestData.createNewsArticle(id = "article-2")
                )
            )
        )

        coEvery { repository.observeBookmarkedNews() } returns flowOf(sections)
        coEvery { repository.toggleBookmark("article-1", false) } throws RuntimeException("Failed")
        coEvery { repository.toggleBookmark("article-2", false) } returns Unit

        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // When - should not crash despite error
        viewModel.clearAllBookmarks()
        advanceUntilIdle()

        // Then - both should be attempted
        coVerify { repository.toggleBookmark("article-1", false) }
        coVerify { repository.toggleBookmark("article-2", false) }
    }

    @Test
    fun `clearAllBookmarks with single article works correctly`() = runTest {
        // Given
        val sections = listOf(
            TestData.createNewsSection(
                articles = listOf(
                    TestData.createNewsArticle(id = "only-article", isBookmarked = true)
                )
            )
        )

        coEvery { repository.observeBookmarkedNews() } returns flowOf(sections, emptyList())
        coEvery { repository.toggleBookmark(any(), any()) } returns Unit

        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.clearAllBookmarks()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { repository.toggleBookmark("only-article", false) }
    }

    @Test
    fun `ViewModel initialization calls observeBookmarkedNews`() = runTest {
        // Given
        coEvery { repository.observeBookmarkedNews() } returns flowOf(emptyList())

        // When
        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // Then
        coVerify { repository.observeBookmarkedNews() }
    }

    @Test
    fun `state transitions from Loading to Success to Empty correctly`() = runTest {
        // Given - simulate unbookmarking the last item
        val withData = listOf(TestData.createNewsSection())
        val empty = emptyList()

        coEvery { repository.observeBookmarkedNews() } returns flowOf(withData, empty)

        // When
        viewModel = BookmarksViewModel(repository)

        // Then
        viewModel.uiState.test {
            assertEquals(BookmarksUiState.Loading, awaitItem())
            assertTrue(awaitItem() is BookmarksUiState.Success)
            assertEquals(BookmarksUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `Error state contains meaningful error message`() = runTest {
        // Given
        val specificError = RuntimeException("Connection lost to database")
        coEvery { repository.observeBookmarkedNews() } returns flowOf {
            throw specificError
        }

        // When
        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem() as BookmarksUiState.Error
            assertEquals("Connection lost to database", state.message)
        }
    }

    @Test
    fun `Error state handles null error message`() = runTest {
        // Given
        val errorWithoutMessage = RuntimeException(null as String?)
        coEvery { repository.observeBookmarkedNews() } returns flowOf {
            throw errorWithoutMessage
        }

        // When
        viewModel = BookmarksViewModel(repository)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem() as BookmarksUiState.Error
            assertEquals("Failed to load bookmarks", state.message)
        }
    }
}
