package com.techventus.wikipedianews.model.repository

import com.techventus.wikipedianews.inject.IoDispatcher
import com.techventus.wikipedianews.model.datasource.NewsLocalDataSource
import com.techventus.wikipedianews.model.datasource.NewsRemoteDataSource
import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.model.domain.NewsSection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NewsRepositoryImpl.
 *
 * Tests offline-first behavior, caching, and error handling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewsRepositoryImplTest {

    private lateinit var remoteDataSource: NewsRemoteDataSource
    private lateinit var localDataSource: NewsLocalDataSource
    private lateinit var repository: NewsRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private val testSections = listOf(
        NewsSection(
            header = "Test Section",
            articles = listOf(
                NewsArticle(
                    id = "1",
                    title = "Test Article",
                    htmlContent = "<p>Test content</p>",
                    url = "https://test.com",
                    timestamp = 123456L
                )
            )
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        remoteDataSource = mockk(relaxed = true)
        localDataSource = mockk(relaxed = true)
        repository = NewsRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeNews returns flow from local data source`() = runTest {
        // Given
        coEvery { localDataSource.observeNews() } returns flowOf(testSections)

        // When
        val result = repository.observeNews().first()

        // Then
        assertEquals(testSections, result)
        coVerify { localDataSource.observeNews() }
    }

    @Test
    fun `refreshNews fetches from remote and saves to local`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchCurrentEvents() } returns testSections
        coEvery { localDataSource.clearAll() } returns Unit
        coEvery { localDataSource.saveNews(any()) } returns Unit

        // When
        val result = repository.refreshNews()

        // Then
        assertTrue(result.isSuccess)
        coVerify { remoteDataSource.fetchCurrentEvents() }
        coVerify { localDataSource.clearAll() }
        coVerify { localDataSource.saveNews(testSections) }
    }

    @Test
    fun `refreshNews with empty data returns failure`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchCurrentEvents() } returns emptyList()

        // When
        val result = repository.refreshNews()

        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { localDataSource.saveNews(any()) }
    }

    @Test
    fun `refreshNews with network error returns failure`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchCurrentEvents() } throws Exception("Network error")

        // When
        val result = repository.refreshNews()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
        coVerify(exactly = 0) { localDataSource.saveNews(any()) }
    }

    @Test
    fun `forceRefresh returns fresh data on success`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchCurrentEvents() } returns testSections
        coEvery { localDataSource.clearAll() } returns Unit
        coEvery { localDataSource.saveNews(any()) } returns Unit

        // When
        val result = repository.forceRefresh()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testSections, result.getOrNull())
        coVerify { remoteDataSource.fetchCurrentEvents() }
        coVerify { localDataSource.saveNews(testSections) }
    }

    @Test
    fun `forceRefresh falls back to cache on error`() = runTest {
        // Given
        val cachedSections = listOf(
            NewsSection("Cached", listOf(NewsArticle("1", "Old", "Content", "url", 123L)))
        )
        coEvery { remoteDataSource.fetchCurrentEvents() } throws Exception("Network error")
        coEvery { localDataSource.getAllNews() } returns cachedSections

        // When
        val result = repository.forceRefresh()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cachedSections, result.getOrNull())
    }

    @Test
    fun `forceRefresh with no cache returns failure`() = runTest {
        // Given
        coEvery { remoteDataSource.fetchCurrentEvents() } throws Exception("Network error")
        coEvery { localDataSource.getAllNews() } returns emptyList()

        // When
        val result = repository.forceRefresh()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `getCachedNewsCount returns count from local source`() = runTest {
        // Given
        coEvery { localDataSource.getCount() } returns 42

        // When
        val count = repository.getCachedNewsCount()

        // Then
        assertEquals(42, count)
        coVerify { localDataSource.getCount() }
    }

    @Test
    fun `clearCache calls local data source clearAll`() = runTest {
        // Given
        coEvery { localDataSource.clearAll() } returns Unit

        // When
        repository.clearCache()

        // Then
        coVerify { localDataSource.clearAll() }
    }
}
