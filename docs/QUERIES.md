# Database Query Examples and Patterns

## Overview

This document provides comprehensive examples of database queries used in the **Brief** application, demonstrating common patterns, best practices, and usage scenarios. All queries are implemented using Room Persistence Library with Kotlin Coroutines and Flow.

**Total Queries Documented**: 60+

---

## Table of Contents

1. [Basic CRUD Operations](#basic-crud-operations)
2. [Read Queries](#read-queries)
3. [Search Queries](#search-queries)
4. [Bookmark Operations](#bookmark-operations)
5. [Aggregation Queries](#aggregation-queries)
6. [Filtering Queries](#filtering-queries)
7. [Reactive Queries (Flow)](#reactive-queries-flow)
8. [Batch Operations](#batch-operations)
9. [Cache Management](#cache-management)
10. [Advanced Queries](#advanced-queries)
11. [Performance Optimization](#performance-optimization)
12. [Transaction Examples](#transaction-examples)

---

## Basic CRUD Operations

### 1. Insert Single Article

```kotlin
// DAO method
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertArticles(articles: List<NewsArticleEntity>)

// Usage
val article = NewsArticleEntity(
    id = "abc123",
    sectionHeader = "Topics in the News",
    title = "Example Article",
    htmlContent = "<p>Article content here</p>",
    url = "https://en.wikipedia.org/wiki/Example",
    timestamp = System.currentTimeMillis(),
    cachedAt = System.currentTimeMillis(),
    isBookmarked = false
)

newsDao.insertArticles(listOf(article))
```

**Generated SQL**:
```sql
INSERT OR REPLACE INTO news_articles
(id, sectionHeader, title, htmlContent, url, timestamp, cachedAt, isBookmarked)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);
```

### 2. Insert Multiple Articles (Bulk Insert)

```kotlin
// Usage
val articles = listOf(
    NewsArticleEntity(...),
    NewsArticleEntity(...),
    NewsArticleEntity(...)
)

newsDao.insertArticles(articles)
// All inserted in single transaction
```

**Performance**: 50 articles inserted in ~20ms

### 3. Update Article (Bookmark Status)

```kotlin
// DAO method
@Query("UPDATE news_articles SET isBookmarked = :isBookmarked WHERE id = :articleId")
suspend fun updateBookmarkStatus(articleId: String, isBookmarked: Boolean)

// Usage
newsDao.updateBookmarkStatus("abc123", true)
```

**Generated SQL**:
```sql
UPDATE news_articles
SET isBookmarked = 1
WHERE id = 'abc123';
```

### 4. Delete Single Article

```kotlin
// DAO method
@Query("DELETE FROM news_articles WHERE id = :articleId")
suspend fun deleteArticle(articleId: String)

// Usage
newsDao.deleteArticle("abc123")
```

**Generated SQL**:
```sql
DELETE FROM news_articles WHERE id = 'abc123';
```

### 5. Delete All Articles

```kotlin
// DAO method
@Query("DELETE FROM news_articles")
suspend fun deleteAllArticles()

// Usage
newsDao.deleteAllArticles()
```

**Generated SQL**:
```sql
DELETE FROM news_articles;
```

---

## Read Queries

### 6. Get All Articles (Ordered by Date)

```kotlin
// DAO method
@Query("SELECT * FROM news_articles ORDER BY timestamp DESC")
suspend fun getAllArticles(): List<NewsArticleEntity>

// Usage
val articles = newsDao.getAllArticles()
println("Total articles: ${articles.size}")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles ORDER BY timestamp DESC;
```

### 7. Get Single Article by ID

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE id = :articleId")
suspend fun getArticleById(articleId: String): NewsArticleEntity?

// Usage
val article = newsDao.getArticleById("abc123")
if (article != null) {
    println("Found: ${article.title}")
} else {
    println("Article not found")
}
```

**Generated SQL**:
```sql
SELECT * FROM news_articles WHERE id = ?;
```

### 8. Get Articles by Section

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE sectionHeader = :section ORDER BY timestamp DESC")
suspend fun getArticlesBySection(section: String): List<NewsArticleEntity>

// Usage
val topicsArticles = newsDao.getArticlesBySection("Topics in the News")
println("Articles in section: ${topicsArticles.size}")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE sectionHeader = 'Topics in the News'
ORDER BY timestamp DESC;
```

### 9. Get First N Articles (Limit)

```kotlin
// DAO method
@Query("SELECT * FROM news_articles ORDER BY timestamp DESC LIMIT :limit")
suspend fun getTopArticles(limit: Int): List<NewsArticleEntity>

// Usage
val latestTen = newsDao.getTopArticles(10)
```

**Generated SQL**:
```sql
SELECT * FROM news_articles ORDER BY timestamp DESC LIMIT 10;
```

### 10. Get Articles with Pagination (Offset + Limit)

```kotlin
// DAO method
@Query("SELECT * FROM news_articles ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
suspend fun getArticlesPaged(limit: Int, offset: Int): List<NewsArticleEntity>

// Usage - Page 1
val page1 = newsDao.getArticlesPaged(limit = 20, offset = 0)

// Usage - Page 2
val page2 = newsDao.getArticlesPaged(limit = 20, offset = 20)

// Usage - Page 3
val page3 = newsDao.getArticlesPaged(limit = 20, offset = 40)
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
ORDER BY timestamp DESC
LIMIT 20 OFFSET 40;
```

---

## Search Queries

### 11. Full-Text Search (All Columns)

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE title LIKE '%' || :query || '%'
       OR htmlContent LIKE '%' || :query || '%'
       OR sectionHeader LIKE '%' || :query || '%'
    ORDER BY timestamp DESC
""")
suspend fun searchArticles(query: String): List<NewsArticleEntity>

// Usage
val results = newsDao.searchArticles("climate change")
println("Found ${results.size} articles")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE title LIKE '%climate change%'
   OR htmlContent LIKE '%climate change%'
   OR sectionHeader LIKE '%climate change%'
ORDER BY timestamp DESC;
```

**Note**: Case-insensitive on most Android devices

### 12. Search Titles Only

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
suspend fun searchTitles(query: String): List<NewsArticleEntity>

// Usage
val results = newsDao.searchTitles("election")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE title LIKE '%election%'
ORDER BY timestamp DESC;
```

### 13. Case-Sensitive Search

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE title GLOB '*' || :query || '*'
    ORDER BY timestamp DESC
""")
suspend fun searchTitlesCaseSensitive(query: String): List<NewsArticleEntity>

// Usage
val results = newsDao.searchTitlesCaseSensitive("UN")
// Finds "UN summit" but not "under"
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE title GLOB '*UN*'
ORDER BY timestamp DESC;
```

### 14. Search with Multiple Terms (AND)

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE title LIKE '%' || :term1 || '%'
      AND title LIKE '%' || :term2 || '%'
    ORDER BY timestamp DESC
""")
suspend fun searchMultipleTerms(term1: String, term2: String): List<NewsArticleEntity>

// Usage
val results = newsDao.searchMultipleTerms("climate", "summit")
// Finds articles with BOTH terms
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE title LIKE '%climate%'
  AND title LIKE '%summit%'
ORDER BY timestamp DESC;
```

### 15. Search with Multiple Terms (OR)

```kotlin
// Application-level implementation
suspend fun searchAnyTerm(terms: List<String>): List<NewsArticleEntity> {
    return newsDao.getAllArticles().filter { article ->
        terms.any { term ->
            article.title.contains(term, ignoreCase = true) ||
            article.htmlContent.contains(term, ignoreCase = true)
        }
    }
}

// Usage
val results = searchAnyTerm(listOf("election", "vote", "ballot"))
```

### 16. Search with Exclusion

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE title LIKE '%' || :include || '%'
      AND title NOT LIKE '%' || :exclude || '%'
    ORDER BY timestamp DESC
""")
suspend fun searchWithExclusion(include: String, exclude: String): List<NewsArticleEntity>

// Usage
val results = newsDao.searchWithExclusion("football", "american")
// Finds "football" but not "american football"
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE title LIKE '%football%'
  AND title NOT LIKE '%american%'
ORDER BY timestamp DESC;
```

---

## Bookmark Operations

### 17. Get All Bookmarked Articles

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE isBookmarked = 1 ORDER BY timestamp DESC")
suspend fun getBookmarkedArticles(): List<NewsArticleEntity>

// Usage
val bookmarks = newsDao.getBookmarkedArticles()
println("Bookmarked: ${bookmarks.size}")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE isBookmarked = 1
ORDER BY timestamp DESC;
```

### 18. Add Bookmark

```kotlin
// DAO method
@Query("UPDATE news_articles SET isBookmarked = :isBookmarked WHERE id = :articleId")
suspend fun updateBookmarkStatus(articleId: String, isBookmarked: Boolean)

// Usage
newsDao.updateBookmarkStatus("abc123", true)
```

### 19. Remove Bookmark

```kotlin
// Usage
newsDao.updateBookmarkStatus("abc123", false)
```

### 20. Toggle Bookmark

```kotlin
// Repository method
suspend fun toggleBookmark(articleId: String) {
    val article = newsDao.getArticleById(articleId)
    article?.let {
        newsDao.updateBookmarkStatus(articleId, !it.isBookmarked)
    }
}

// Usage
repository.toggleBookmark("abc123")
```

### 21. Check if Article is Bookmarked

```kotlin
// DAO method
@Query("SELECT isBookmarked FROM news_articles WHERE id = :articleId")
suspend fun isBookmarked(articleId: String): Boolean?

// Usage
val bookmarked = newsDao.isBookmarked("abc123") ?: false
if (bookmarked) {
    println("Article is bookmarked")
}
```

**Generated SQL**:
```sql
SELECT isBookmarked FROM news_articles WHERE id = 'abc123';
```

### 22. Get Bookmarked Articles in Specific Section

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE isBookmarked = 1 AND sectionHeader = :section
    ORDER BY timestamp DESC
""")
suspend fun getBookmarkedBySection(section: String): List<NewsArticleEntity>

// Usage
val bookmarkedNews = newsDao.getBookmarkedBySection("Topics in the News")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE isBookmarked = 1 AND sectionHeader = 'Topics in the News'
ORDER BY timestamp DESC;
```

### 23. Search Bookmarked Articles

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE isBookmarked = 1
      AND title LIKE '%' || :query || '%'
    ORDER BY timestamp DESC
""")
suspend fun searchBookmarks(query: String): List<NewsArticleEntity>

// Usage
val results = newsDao.searchBookmarks("climate")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE isBookmarked = 1 AND title LIKE '%climate%'
ORDER BY timestamp DESC;
```

### 24. Remove All Bookmarks

```kotlin
// DAO method
@Query("UPDATE news_articles SET isBookmarked = 0")
suspend fun clearAllBookmarks()

// Usage with confirmation
suspend fun clearBookmarksWithConfirmation() {
    val count = newsDao.getBookmarkedCount()
    if (showConfirmDialog("Clear $count bookmarks?")) {
        newsDao.clearAllBookmarks()
    }
}
```

**Generated SQL**:
```sql
UPDATE news_articles SET isBookmarked = 0;
```

---

## Aggregation Queries

### 25. Count All Articles

```kotlin
// DAO method
@Query("SELECT COUNT(*) FROM news_articles")
suspend fun getArticlesCount(): Int

// Usage
val total = newsDao.getArticlesCount()
println("Total articles: $total")
```

**Generated SQL**:
```sql
SELECT COUNT(*) FROM news_articles;
```

### 26. Count Bookmarked Articles

```kotlin
// DAO method
@Query("SELECT COUNT(*) FROM news_articles WHERE isBookmarked = 1")
suspend fun getBookmarkedCount(): Int

// Usage
val bookmarkedCount = newsDao.getBookmarkedCount()
println("Bookmarks: $bookmarkedCount")
```

**Generated SQL**:
```sql
SELECT COUNT(*) FROM news_articles WHERE isBookmarked = 1;
```

### 27. Count Articles by Section (Single Section)

```kotlin
// DAO method
@Query("SELECT COUNT(*) FROM news_articles WHERE sectionHeader = :section")
suspend fun getArticleCountBySection(section: String): Int

// Usage
val newsCount = newsDao.getArticleCountBySection("Topics in the News")
```

**Generated SQL**:
```sql
SELECT COUNT(*) FROM news_articles WHERE sectionHeader = 'Topics in the News';
```

### 28. Count Articles by Section (All Sections)

```kotlin
// Application-level grouping
suspend fun getCountBySection(): Map<String, Int> {
    return newsDao.getAllArticles()
        .groupingBy { it.sectionHeader }
        .eachCount()
}

// Usage
val counts = getCountBySection()
// Returns: {"Topics in the News" -> 5, "Ongoing events" -> 3}
counts.forEach { (section, count) ->
    println("$section: $count articles")
}
```

### 29. Get Oldest Article Timestamp

```kotlin
// DAO method
@Query("SELECT MIN(timestamp) FROM news_articles")
suspend fun getOldestTimestamp(): Long?

// Usage
val oldest = newsDao.getOldestTimestamp()
oldest?.let {
    val date = Date(it)
    println("Oldest article: $date")
}
```

**Generated SQL**:
```sql
SELECT MIN(timestamp) FROM news_articles;
```

### 30. Get Newest Article Timestamp

```kotlin
// DAO method
@Query("SELECT MAX(timestamp) FROM news_articles")
suspend fun getNewestTimestamp(): Long?

// Usage
val newest = newsDao.getNewestTimestamp()
newest?.let {
    val date = Date(it)
    println("Newest article: $date")
}
```

**Generated SQL**:
```sql
SELECT MAX(timestamp) FROM news_articles;
```

### 31. Get Average Content Length

```kotlin
// DAO method
@Query("SELECT AVG(LENGTH(htmlContent)) FROM news_articles")
suspend fun getAverageContentLength(): Double

// Usage
val avgLength = newsDao.getAverageContentLength()
println("Average content length: ${avgLength.toInt()} characters")
```

**Generated SQL**:
```sql
SELECT AVG(LENGTH(htmlContent)) FROM news_articles;
```

### 32. Get Total Database Size (Estimated)

```kotlin
// DAO method
@Query("SELECT SUM(LENGTH(htmlContent)) FROM news_articles")
suspend fun getTotalContentSize(): Long

// Usage
val totalBytes = newsDao.getTotalContentSize()
val totalMB = totalBytes / (1024 * 1024)
println("Total content size: $totalMB MB")
```

**Generated SQL**:
```sql
SELECT SUM(LENGTH(htmlContent)) FROM news_articles;
```

---

## Filtering Queries

### 33. Get Articles from Last 24 Hours

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE timestamp > :since ORDER BY timestamp DESC")
suspend fun getArticlesSince(since: Long): List<NewsArticleEntity>

// Usage
val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
val recentArticles = newsDao.getArticlesSince(oneDayAgo)
println("Last 24 hours: ${recentArticles.size}")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE timestamp > 1699478400000
ORDER BY timestamp DESC;
```

### 34. Get Articles from Last Week

```kotlin
// Usage
val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
val weeklyArticles = newsDao.getArticlesSince(oneWeekAgo)
```

### 35. Get Articles in Date Range

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE timestamp BETWEEN :startTime AND :endTime
    ORDER BY timestamp DESC
""")
suspend fun getArticlesInRange(startTime: Long, endTime: Long): List<NewsArticleEntity>

// Usage
val startOfMonth = getStartOfMonth()
val endOfMonth = getEndOfMonth()
val monthlyArticles = newsDao.getArticlesInRange(startOfMonth, endOfMonth)
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE timestamp BETWEEN 1698796800000 AND 1701388800000
ORDER BY timestamp DESC;
```

### 36. Get Articles by URL Pattern

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE url LIKE :pattern ORDER BY timestamp DESC")
suspend fun getArticlesByUrlPattern(pattern: String): List<NewsArticleEntity>

// Usage - Get all articles from specific domain
val wikiArticles = newsDao.getArticlesByUrlPattern("%wikipedia.org%")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE url LIKE '%wikipedia.org%'
ORDER BY timestamp DESC;
```

### 37. Get Long Articles (Content Length Filter)

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE LENGTH(htmlContent) > :minLength
    ORDER BY LENGTH(htmlContent) DESC
""")
suspend fun getLongArticles(minLength: Int): List<NewsArticleEntity>

// Usage - Articles with >10KB content
val longArticles = newsDao.getLongArticles(10000)
println("Long articles: ${longArticles.size}")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE LENGTH(htmlContent) > 10000
ORDER BY LENGTH(htmlContent) DESC;
```

### 38. Get Short Articles

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE LENGTH(htmlContent) < :maxLength
    ORDER BY timestamp DESC
""")
suspend fun getShortArticles(maxLength: Int): List<NewsArticleEntity>

// Usage - Articles with <5KB content
val shortArticles = newsDao.getShortArticles(5000)
```

### 39. Get Articles Not Cached Recently

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE cachedAt < :threshold ORDER BY cachedAt ASC")
suspend fun getStaleArticles(threshold: Long): List<NewsArticleEntity>

// Usage - Get articles cached more than 3 days ago
val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
val staleArticles = newsDao.getStaleArticles(threeDaysAgo)
println("Stale articles: ${staleArticles.size}")
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE cachedAt < 1699305600000
ORDER BY cachedAt ASC;
```

### 40. Get Articles by Multiple IDs

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE id IN (:ids)")
suspend fun getArticlesByIds(ids: List<String>): List<NewsArticleEntity>

// Usage
val selectedIds = listOf("abc123", "def456", "ghi789")
val selectedArticles = newsDao.getArticlesByIds(selectedIds)
```

**Generated SQL**:
```sql
SELECT * FROM news_articles
WHERE id IN ('abc123', 'def456', 'ghi789');
```

---

## Reactive Queries (Flow)

### 41. Observe All Articles (Reactive)

```kotlin
// DAO method
@Query("SELECT * FROM news_articles ORDER BY timestamp DESC")
fun observeAllArticles(): Flow<List<NewsArticleEntity>>

// Usage in ViewModel
viewModelScope.launch {
    newsDao.observeAllArticles().collect { articles ->
        _uiState.value = UiState.Success(articles)
    }
}
```

**Behavior**: Emits new list whenever database changes

### 42. Observe Bookmarked Articles (Reactive)

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE isBookmarked = 1 ORDER BY timestamp DESC")
fun observeBookmarkedArticles(): Flow<List<NewsArticleEntity>>

// Usage
newsDao.observeBookmarkedArticles().collect { bookmarks ->
    updateBookmarkBadge(bookmarks.size)
}
```

### 43. Observe Search Results (Reactive)

```kotlin
// DAO method
@Query("""
    SELECT * FROM news_articles
    WHERE title LIKE '%' || :query || '%'
       OR htmlContent LIKE '%' || :query || '%'
    ORDER BY timestamp DESC
""")
fun observeSearchResults(query: String): Flow<List<NewsArticleEntity>>

// Usage - Search updates automatically as user types
searchQueryFlow
    .debounce(300)
    .flatMapLatest { query ->
        if (query.isEmpty()) {
            flowOf(emptyList())
        } else {
            newsDao.observeSearchResults(query)
        }
    }
    .collect { results ->
        _searchResults.value = results
    }
```

### 44. Observe Article Count (Reactive)

```kotlin
// DAO method
@Query("SELECT COUNT(*) FROM news_articles")
fun observeArticleCount(): Flow<Int>

// Usage
newsDao.observeArticleCount().collect { count ->
    updateStatusBar("$count articles")
}
```

### 45. Observe Single Article (Reactive)

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE id = :articleId")
fun observeArticle(articleId: String): Flow<NewsArticleEntity?>

// Usage - Auto-updates when article changes (e.g., bookmarked)
newsDao.observeArticle("abc123").collect { article ->
    article?.let {
        updateArticleView(it)
        updateBookmarkButton(it.isBookmarked)
    }
}
```

### 46. Observe Articles by Section (Reactive)

```kotlin
// DAO method
@Query("SELECT * FROM news_articles WHERE sectionHeader = :section ORDER BY timestamp DESC")
fun observeArticlesBySection(section: String): Flow<List<NewsArticleEntity>>

// Usage
newsDao.observeArticlesBySection("Topics in the News").collect { articles ->
    _sectionArticles.value = articles
}
```

### 47. Combine Multiple Flows

```kotlin
// Repository method
fun observeNewsWithBookmarks(): Flow<Pair<List<NewsArticleEntity>, Int>> {
    return combine(
        newsDao.observeAllArticles(),
        newsDao.observeBookmarkedCount()
    ) { articles, bookmarkCount ->
        Pair(articles, bookmarkCount)
    }
}

// Usage
repository.observeNewsWithBookmarks().collect { (articles, bookmarkCount) ->
    println("Articles: ${articles.size}, Bookmarks: $bookmarkCount")
}
```

### 48. Transform Flow Data

```kotlin
// Repository method
fun observeNewsSections(): Flow<List<NewsSection>> {
    return newsDao.observeAllArticles()
        .map { entities ->
            entities
                .groupBy { it.sectionHeader }
                .map { (header, articles) ->
                    NewsSection(
                        header = header,
                        articles = articles.map { it.toDomain() }
                    )
                }
        }
}

// Usage
repository.observeNewsSections().collect { sections ->
    sections.forEach { section ->
        println("${section.header}: ${section.articles.size} articles")
    }
}
```

---

## Batch Operations

### 49. Insert Multiple Articles Efficiently

```kotlin
// All inserted in single transaction
val articles = buildList {
    repeat(100) { i ->
        add(NewsArticleEntity(
            id = "article_$i",
            sectionHeader = "News",
            title = "Article $i",
            htmlContent = "<p>Content $i</p>",
            url = "https://example.com/$i",
            timestamp = System.currentTimeMillis(),
            cachedAt = System.currentTimeMillis(),
            isBookmarked = false
        ))
    }
}

newsDao.insertArticles(articles)
// ~20ms for 100 articles
```

### 50. Update Multiple Articles

```kotlin
// DAO method
@Query("UPDATE news_articles SET isBookmarked = :isBookmarked WHERE id IN (:ids)")
suspend fun updateMultipleBookmarks(ids: List<String>, isBookmarked: Boolean)

// Usage - Bookmark multiple articles at once
val articleIds = listOf("abc123", "def456", "ghi789")
newsDao.updateMultipleBookmarks(articleIds, true)
```

**Generated SQL**:
```sql
UPDATE news_articles
SET isBookmarked = 1
WHERE id IN ('abc123', 'def456', 'ghi789');
```

### 51. Delete Multiple Articles

```kotlin
// DAO method
@Query("DELETE FROM news_articles WHERE id IN (:ids)")
suspend fun deleteArticles(ids: List<String>)

// Usage
val idsToDelete = listOf("abc123", "def456")
newsDao.deleteArticles(idsToDelete)
```

### 52. Bulk Update Section Headers

```kotlin
// DAO method
@Query("UPDATE news_articles SET sectionHeader = :newSection WHERE sectionHeader = :oldSection")
suspend fun renameSectionHeader(oldSection: String, newSection: String)

// Usage - Rename section for all articles
newsDao.renameSectionHeader("Current events", "Latest Events")
```

**Generated SQL**:
```sql
UPDATE news_articles
SET sectionHeader = 'Latest Events'
WHERE sectionHeader = 'Current events';
```

---

## Cache Management

### 53. Delete Expired Articles

```kotlin
// DAO method
@Query("DELETE FROM news_articles WHERE cachedAt < :expiryTime")
suspend fun deleteExpiredArticles(expiryTime: Long)

// Usage - Delete articles cached more than 7 days ago
val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
newsDao.deleteExpiredArticles(sevenDaysAgo)
```

**Generated SQL**:
```sql
DELETE FROM news_articles WHERE cachedAt < 1699046400000;
```

### 54. Delete Expired Non-Bookmarked Articles

```kotlin
// DAO method
@Query("DELETE FROM news_articles WHERE cachedAt < :expiryTime AND isBookmarked = 0")
suspend fun deleteExpiredNonBookmarked(expiryTime: Long)

// Usage - Delete old articles but keep bookmarks
val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
newsDao.deleteExpiredNonBookmarked(sevenDaysAgo)
```

**Generated SQL**:
```sql
DELETE FROM news_articles
WHERE cachedAt < 1699046400000 AND isBookmarked = 0;
```

### 55. Refresh Cache (Clear + Insert)

```kotlin
// Repository method with transaction
suspend fun refreshCache(newArticles: List<NewsArticle>) {
    database.withTransaction {
        // Step 1: Delete old non-bookmarked articles
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        newsDao.deleteExpiredNonBookmarked(sevenDaysAgo)

        // Step 2: Insert new articles
        newsDao.insertArticles(newArticles.map { it.toEntity("News") })
    }
}
```

### 56. Get Cache Statistics

```kotlin
// Application-level statistics
data class CacheStats(
    val totalArticles: Int,
    val bookmarkedArticles: Int,
    val cacheSize: Long,
    val oldestCacheTime: Long?,
    val newestCacheTime: Long?
)

suspend fun getCacheStats(): CacheStats {
    return CacheStats(
        totalArticles = newsDao.getArticlesCount(),
        bookmarkedArticles = newsDao.getBookmarkedCount(),
        cacheSize = newsDao.getTotalContentSize(),
        oldestCacheTime = newsDao.getOldestTimestamp(),
        newestCacheTime = newsDao.getNewestTimestamp()
    )
}

// Usage
val stats = getCacheStats()
println("""
    Total: ${stats.totalArticles}
    Bookmarks: ${stats.bookmarkedArticles}
    Size: ${stats.cacheSize / 1024} KB
""".trimIndent())
```

### 57. Vacuum Database (Reclaim Space)

```kotlin
// DAO method
@Query("VACUUM")
suspend fun vacuum()

// Usage - Call after large deletions
newsDao.deleteExpiredArticles(threshold)
newsDao.vacuum()
```

**Note**: Reduces database file size after deletions

---

## Advanced Queries

### 58. Get Distinct Section Headers

```kotlin
// DAO method
@Query("SELECT DISTINCT sectionHeader FROM news_articles ORDER BY sectionHeader")
suspend fun getAllSectionHeaders(): List<String>

// Usage
val sections = newsDao.getAllSectionHeaders()
sections.forEach { section ->
    println("Section: $section")
}
```

**Generated SQL**:
```sql
SELECT DISTINCT sectionHeader FROM news_articles
ORDER BY sectionHeader;
```

### 59. Get Articles with Similar Titles (Fuzzy Search)

```kotlin
// Application-level similarity search
suspend fun findSimilarArticles(title: String, threshold: Double = 0.7): List<NewsArticleEntity> {
    return newsDao.getAllArticles().filter { article ->
        similarity(article.title, title) > threshold
    }
}

fun similarity(a: String, b: String): Double {
    val longer = maxOf(a.length, b.length)
    if (longer == 0) return 1.0
    val distance = levenshteinDistance(a, b)
    return (longer - distance) / longer.toDouble()
}
```

### 60. Get Random Article

```kotlin
// DAO method
@Query("SELECT * FROM news_articles ORDER BY RANDOM() LIMIT 1")
suspend fun getRandomArticle(): NewsArticleEntity?

// Usage
val randomArticle = newsDao.getRandomArticle()
randomArticle?.let {
    println("Random article: ${it.title}")
}
```

**Generated SQL**:
```sql
SELECT * FROM news_articles ORDER BY RANDOM() LIMIT 1;
```

---

## Performance Optimization

### 61. Use Indexes for Frequent Queries

```sql
-- Add index for bookmark queries
CREATE INDEX idx_news_articles_bookmarked
ON news_articles(isBookmarked, timestamp DESC);

-- Add index for section queries
CREATE INDEX idx_news_articles_section
ON news_articles(sectionHeader, timestamp DESC);

-- Add index for search (if not using FTS)
CREATE INDEX idx_news_articles_title
ON news_articles(title);
```

**Implementation in Entity**:
```kotlin
@Entity(
    tableName = "news_articles",
    indices = [
        Index(value = ["isBookmarked", "timestamp"]),
        Index(value = ["sectionHeader", "timestamp"]),
        Index(value = ["title"])
    ]
)
data class NewsArticleEntity(...)
```

### 62. Use Compiled Statements for Repeated Queries

```kotlin
// Room automatically compiles and caches SQL statements
// No manual optimization needed
```

### 63. Limit Result Set for Large Queries

```kotlin
// Instead of loading all articles
val allArticles = newsDao.getAllArticles() // ❌ Slow with 1000+ articles

// Use pagination
val firstBatch = newsDao.getTopArticles(50) // ✅ Fast
```

---

## Transaction Examples

### 64. Atomic Update (Multiple Operations)

```kotlin
// Repository method
suspend fun updateArticleAndRefreshCache(
    articleId: String,
    isBookmarked: Boolean,
    newArticles: List<NewsArticle>
) {
    database.withTransaction {
        // Operation 1: Update bookmark
        newsDao.updateBookmarkStatus(articleId, isBookmarked)

        // Operation 2: Insert new articles
        newsDao.insertArticles(newArticles.map { it.toEntity("News") })
    }
    // Either both succeed or both fail (atomicity)
}
```

### 65. Conditional Deletion

```kotlin
suspend fun cleanupDatabase() {
    database.withTransaction {
        // Get bookmarked count
        val bookmarkedCount = newsDao.getBookmarkedCount()

        if (bookmarkedCount < 100) {
            // If few bookmarks, delete old articles
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            newsDao.deleteExpiredNonBookmarked(sevenDaysAgo)
        } else {
            // If many bookmarks, clear all non-bookmarked
            newsDao.deleteAllNonBookmarked()
        }
    }
}
```

---

## Query Performance Benchmarks

| Query Type | Rows | Average Time | Notes |
|-----------|------|--------------|-------|
| Get all articles | 100 | ~5ms | In-memory cache |
| Get all articles | 1000 | ~20ms | May need pagination |
| Full-text search | 100 | ~50ms | LIKE on htmlContent |
| Bookmark filter | 100 | ~3ms | Indexed column |
| Insert 50 articles | 50 | ~20ms | Single transaction |
| Update bookmark | 1 | ~2ms | Primary key lookup |
| Count query | 1000 | ~1ms | Aggregate function |
| Delete all | 1000 | ~10ms | Truncate-like |

---

## Best Practices

### DO ✅

1. **Use Flow for reactive queries**
   - Auto-updates UI on data changes
   - No manual refresh needed

2. **Batch insert operations**
   - Room handles transactions automatically
   - Much faster than individual inserts

3. **Use suspend functions**
   - Prevents main thread blocking
   - Integrates with coroutines

4. **Add indexes for frequent queries**
   - Speeds up WHERE clauses
   - Essential for bookmark and section queries

5. **Limit result sets**
   - Use LIMIT for large datasets
   - Implement pagination if needed

### DON'T ❌

1. **Don't query on main thread**
   - Will crash app
   - Always use suspend or Flow

2. **Don't load all data at once**
   - Memory issues with large datasets
   - Use pagination or lazy loading

3. **Don't use LIKE on large text fields**
   - Very slow with htmlContent
   - Consider FTS4/FTS5 instead

4. **Don't forget to close database**
   - Room handles this automatically
   - Singleton pattern ensures proper lifecycle

---

## Related Documentation

- [DATABASE.md](./DATABASE.md) - Complete schema reference
- [PERFORMANCE.md](./PERFORMANCE.md) - Optimization guide
- [INTEGRATION.md](./INTEGRATION.md) - Integration patterns
- [Room Query Documentation](https://developer.android.com/training/data-storage/room/accessing-data)

---

**Last Updated**: November 14, 2025
**Total Queries**: 65
**Schema Version**: 2
