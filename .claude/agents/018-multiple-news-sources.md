# Micro-Agent 018: Multiple News Sources

## Completion Check
```bash
[ -f .claude/completed/018 ] && echo "✅ Already completed" && exit 0
```

## Task: Support Multiple Wikipedia News Sources

Extend the app to support multiple Wikipedia sections and languages, allowing users to customize their news feed.

## Requirements

### 1. Create News Source Domain Model

`app/src/main/java/com/techventus/wikipedianews/model/domain/NewsSource.kt`:
```kotlin
package com.techventus.wikipedianews.model.domain

enum class NewsSourceType {
    CURRENT_EVENTS,      // Portal:Current_events
    IN_THE_NEWS,         // Main_Page - "In the news" section
    ON_THIS_DAY,         // Main_Page - "On this day" section
    RECENT_DEATHS,       // Portal:Current_events - "Recent deaths" only
    ONGOING_EVENTS       // Portal:Current_events - "Ongoing" only
}

enum class WikipediaLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    JAPANESE("ja", "日本語"),
    CHINESE("zh", "中文"),
    RUSSIAN("ru", "Русский"),
    PORTUGUESE("pt", "Português"),
    ITALIAN("it", "Italiano"),
    ARABIC("ar", "العربية")
}

data class NewsSource(
    val type: NewsSourceType,
    val language: WikipediaLanguage,
    val isEnabled: Boolean = true
) {
    val displayName: String
        get() = "${type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }} (${language.displayName})"

    val url: String
        get() = when (type) {
            NewsSourceType.CURRENT_EVENTS -> "https://${language.code}.wikipedia.org/wiki/Portal:Current_events"
            NewsSourceType.IN_THE_NEWS -> "https://${language.code}.wikipedia.org/wiki/Main_Page"
            NewsSourceType.ON_THIS_DAY -> "https://${language.code}.wikipedia.org/wiki/Main_Page"
            NewsSourceType.RECENT_DEATHS -> "https://${language.code}.wikipedia.org/wiki/Portal:Current_events"
            NewsSourceType.ONGOING_EVENTS -> "https://${language.code}.wikipedia.org/wiki/Portal:Current_events"
        }
}
```

### 2. Update Database Schema

Update `app/src/main/java/com/techventus/wikipedianews/model/database/entity/NewsArticleEntity.kt`:
```kotlin
@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey val url: String,
    val title: String,
    val htmlContent: String,
    val plainText: String,
    val sectionHeader: String,
    val timestamp: Long,
    val isBookmarked: Boolean = false,
    val sourceType: String = "CURRENT_EVENTS",  // NEW
    val sourceLanguage: String = "en",          // NEW
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toNewsArticle(): NewsArticle = NewsArticle(
        url = url,
        title = title,
        htmlContent = htmlContent,
        plainText = plainText,
        section = sectionHeader,
        timestamp = timestamp,
        isBookmarked = isBookmarked,
        sourceType = sourceType,        // NEW
        sourceLanguage = sourceLanguage // NEW
    )
}
```

Update `app/src/main/java/com/techventus/wikipedianews/model/domain/NewsArticle.kt`:
```kotlin
data class NewsArticle(
    val url: String,
    val title: String,
    val htmlContent: String,
    val plainText: String,
    val section: String,
    val timestamp: Long,
    val isBookmarked: Boolean = false,
    val sourceType: String = "CURRENT_EVENTS",  // NEW
    val sourceLanguage: String = "en"           // NEW
) {
    val sourceBadge: String
        get() = NewsSourceType.valueOf(sourceType).name.take(3).uppercase()
}
```

Update `app/src/main/java/com/techventus/wikipedianews/model/database/AppDatabase.kt`:
```kotlin
@Database(
    entities = [NewsArticleEntity::class],
    version = 3,  // Bump version
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao

    companion object {
        const val DATABASE_NAME = "brief_database"

        // Migration from version 2 to 3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE news_articles ADD COLUMN sourceType TEXT NOT NULL DEFAULT 'CURRENT_EVENTS'"
                )
                database.execSQL(
                    "ALTER TABLE news_articles ADD COLUMN sourceLanguage TEXT NOT NULL DEFAULT 'en'"
                )
            }
        }
    }
}
```

Update `app/src/main/java/com/techventus/wikipedianews/inject/DatabaseModule.kt`:
```kotlin
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
    Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    )
        .addMigrations(AppDatabase.MIGRATION_2_3)  // Add migration
        .build()
```

### 3. Create News Source Preferences

Update `app/src/main/java/com/techventus/wikipedianews/model/datastore/UserPreferencesDataStore.kt`:
```kotlin
// Add to UserPreferencesDataStore class

private val ENABLED_SOURCES = stringSetPreferencesKey("enabled_sources")
private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")

val enabledSources: Flow<Set<String>> = dataStore.data
    .map { it[ENABLED_SOURCES] ?: setOf("CURRENT_EVENTS_en") }

val selectedLanguage: Flow<String> = dataStore.data
    .map { it[SELECTED_LANGUAGE] ?: "en" }

suspend fun setEnabledSources(sources: Set<String>) {
    dataStore.edit { it[ENABLED_SOURCES] = sources }
}

suspend fun setSelectedLanguage(language: String) {
    dataStore.edit { it[SELECTED_LANGUAGE] = language }
}
```

### 4. Update Repository to Handle Multiple Sources

Update `app/src/main/java/com/techventus/wikipedianews/model/repository/NewsRepository.kt`:
```kotlin
interface NewsRepository {
    fun observeNews(): Flow<List<NewsSection>>
    suspend fun refreshNews(sources: List<NewsSource> = defaultSources): Result<Unit>
    suspend fun getCachedNewsCount(): Int
    suspend fun clearCache()
    suspend fun toggleBookmark(articleUrl: String)
    fun observeBookmarkedArticles(): Flow<List<NewsSection>>
    fun searchArticles(query: String): Flow<List<NewsSection>>

    companion object {
        val defaultSources = listOf(
            NewsSource(NewsSourceType.CURRENT_EVENTS, WikipediaLanguage.ENGLISH)
        )
    }
}
```

Update `app/src/main/java/com/techventus/wikipedianews/model/repository/NewsRepositoryImpl.kt`:
```kotlin
override suspend fun refreshNews(sources: List<NewsSource>): Result<Unit> =
    withContext(ioDispatcher) {
        try {
            val allArticles = sources
                .filter { it.isEnabled }
                .flatMap { source ->
                    fetchArticlesForSource(source)
                }

            if (allArticles.isNotEmpty()) {
                localDataSource.saveArticles(allArticles)
                Result.success(Unit)
            } else {
                Result.failure(Exception("No articles found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

private suspend fun fetchArticlesForSource(source: NewsSource): List<NewsArticleEntity> {
    return try {
        val html = remoteDataSource.fetchHtml(source.url)
        val sections = parser.parse(html, source.type)

        sections.flatMap { section ->
            section.articles.map { article ->
                NewsArticleEntity(
                    url = article.url,
                    title = article.title,
                    htmlContent = article.htmlContent,
                    plainText = article.plainText,
                    sectionHeader = article.section,
                    timestamp = article.timestamp,
                    sourceType = source.type.name,
                    sourceLanguage = source.language.code
                )
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}
```

### 5. Update NewsViewModel

Update `app/src/main/java/com/techventus/wikipedianews/ui/compose/screen/news/NewsViewModel.kt`:
```kotlin
@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    // ... existing code ...

    private val enabledSources: StateFlow<Set<String>> = userPreferencesDataStore.enabledSources
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = setOf("CURRENT_EVENTS_en")
        )

    fun refresh() {
        viewModelScope.launch {
            val sources = enabledSources.value.map { sourceKey ->
                val parts = sourceKey.split("_")
                val type = NewsSourceType.valueOf(parts.dropLast(1).joinToString("_"))
                val lang = WikipediaLanguage.entries.find { it.code == parts.last() }
                    ?: WikipediaLanguage.ENGLISH
                NewsSource(type, lang)
            }

            newsRepository.refreshNews(sources)
        }
    }
}
```

### 6. Create Source Selection Screen

`app/src/main/java/com/techventus/wikipedianews/ui/compose/screen/sources/SourcesViewModel.kt`:
```kotlin
package com.techventus.wikipedianews.ui.compose.screen.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techventus.wikipedianews.model.datastore.UserPreferencesDataStore
import com.techventus.wikipedianews.model.domain.NewsSource
import com.techventus.wikipedianews.model.domain.NewsSourceType
import com.techventus.wikipedianews.model.domain.WikipediaLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _selectedLanguage = MutableStateFlow(WikipediaLanguage.ENGLISH)
    val selectedLanguage: StateFlow<WikipediaLanguage> = _selectedLanguage.asStateFlow()

    val enabledSources: StateFlow<Set<String>> = userPreferencesDataStore.enabledSources
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val availableSources: StateFlow<List<NewsSource>> = combine(
        selectedLanguage,
        enabledSources
    ) { language, enabled ->
        NewsSourceType.entries.map { type ->
            val key = "${type.name}_${language.code}"
            NewsSource(type, language, isEnabled = key in enabled)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectLanguage(language: WikipediaLanguage) {
        _selectedLanguage.value = language
    }

    fun toggleSource(source: NewsSource) {
        viewModelScope.launch {
            val key = "${source.type.name}_${source.language.code}"
            val current = enabledSources.value.toMutableSet()

            if (key in current) {
                current.remove(key)
            } else {
                current.add(key)
            }

            userPreferencesDataStore.setEnabledSources(current)
        }
    }
}
```

`app/src/main/java/com/techventus/wikipedianews/ui/compose/screen/sources/SourcesScreen.kt`:
```kotlin
package com.techventus.wikipedianews.ui.compose.screen.sources

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.techventus.wikipedianews.model.domain.NewsSource
import com.techventus.wikipedianews.model.domain.WikipediaLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    onNavigateBack: () -> Unit,
    viewModel: SourcesViewModel = hiltViewModel()
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val availableSources by viewModel.availableSources.collectAsState()

    Scaffold(
        topAppBar = {
            TopAppBar(
                title = { Text("News Sources") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Language selector
            Text("Language", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WikipediaLanguage.entries) { language ->
                    FilterChip(
                        selected = language == selectedLanguage,
                        onClick = { viewModel.selectLanguage(language) },
                        label = { Text(language.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Source type selector
            Text("Sources", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableSources) { source ->
                    SourceItem(
                        source = source,
                        onToggle = { viewModel.toggleSource(source) }
                    )
                }
            }
        }
    }
}

@Composable
fun SourceItem(
    source: NewsSource,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Switch(
                checked = source.isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
```

### 7. Add Sources Screen to Navigation

Update `app/src/main/java/com/techventus/wikipedianews/ui/navigation/NavGraph.kt`:
```kotlin
object Routes {
    const val NEWS = "news"
    const val BOOKMARKS = "bookmarks"
    const val SETTINGS = "settings"
    const val SOURCES = "sources"  // NEW
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.NEWS,
        modifier = modifier
    ) {
        composable(Routes.NEWS) {
            NewsScreen()
        }
        composable(Routes.BOOKMARKS) {
            BookmarksScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToSources = { navController.navigate(Routes.SOURCES) }
            )
        }
        composable(Routes.SOURCES) {
            SourcesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

### 8. Add Sources Button to Settings

Update SettingsScreen to add navigation to sources:
```kotlin
// In SettingsScreen.kt
Card(
    modifier = Modifier.fillMaxWidth(),
    onClick = onNavigateToSources
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("News Sources", style = MaterialTheme.typography.bodyLarge)
            Text("Select sources and languages", style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Navigate")
    }
}
```

## Verification Steps

1. Build and run the app
2. Navigate to Settings → News Sources
3. Select different languages and source types
4. Enable multiple sources
5. Return to news screen and refresh
6. Verify articles from multiple sources appear
7. Verify source badges on articles
8. Test with different language combinations

## Expected Results
- Users can select multiple news sources
- Articles show source badges (e.g., "CUR", "ITN", "OTD")
- Different Wikipedia languages supported
- Settings persist across app restarts
- Smooth integration with existing features

## Completion Marker
```bash
mkdir -p .claude/completed
echo "Multiple news sources implemented on $(date)" > .claude/completed/018
```

## Files Created
- `app/src/main/java/com/techventus/wikipedianews/model/domain/NewsSource.kt`
- `app/src/main/java/com/techventus/wikipedianews/ui/compose/screen/sources/SourcesViewModel.kt`
- `app/src/main/java/com/techventus/wikipedianews/ui/compose/screen/sources/SourcesScreen.kt`

## Files Modified
- `app/src/main/java/com/techventus/wikipedianews/model/database/entity/NewsArticleEntity.kt`
- `app/src/main/java/com/techventus/wikipedianews/model/domain/NewsArticle.kt`
- `app/src/main/java/com/techventus/wikipedianews/model/database/AppDatabase.kt`
- `app/src/main/java/com/techventus/wikipedianews/inject/DatabaseModule.kt`
- `app/src/main/java/com/techventus/wikipedianews/model/datastore/UserPreferencesDataStore.kt`
- `app/src/main/java/com/techventus/wikipedianews/model/repository/NewsRepository.kt`
- `app/src/main/java/com/techventus/wikipedianews/model/repository/NewsRepositoryImpl.kt`
- `app/src/main/java/com/techventus/wikipedianews/ui/compose/screen/news/NewsViewModel.kt`
- `app/src/main/java/com/techventus/wikipedianews/ui/navigation/NavGraph.kt`
- `app/src/main/java/com/techventus/wikipedianews/ui/compose/screen/settings/SettingsScreen.kt`
