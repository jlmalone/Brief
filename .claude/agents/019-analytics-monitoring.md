# Micro-Agent 019: Analytics & Monitoring

## Completion Check
```bash
[ -f .claude/completed/019 ] && echo "✅ Already completed" && exit 0
```

## Task: Firebase Analytics and Crashlytics Integration

Integrate Firebase Analytics for usage tracking and Crashlytics for crash reporting to improve app quality and user experience.

## Prerequisites

**IMPORTANT**: This agent requires Firebase project setup. Before implementing:
1. Create Firebase project at https://console.firebase.google.com
2. Register Android app with package name: `com.techventus.wikipedianews`
3. Download `google-services.json` to `app/` directory
4. Keep `google-services.json` out of git (add to `.gitignore`)

## Requirements

### 1. Update `.gitignore`

Add Firebase configuration files to `.gitignore`:
```gitignore
# Firebase
google-services.json
crashlytics.properties
crashlytics-build.properties
```

### 2. Add Firebase Dependencies

Update `build.gradle.kts` (project level):
```kotlin
plugins {
    // ... existing plugins ...
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}
```

Update `gradle/libs.versions.toml`:
```toml
[versions]
# ... existing versions ...
firebase-bom = "33.5.1"
play-services-analytics = "18.1.0"

[libraries]
# ... existing libraries ...
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-analytics = { module = "com.google.firebase:firebase-analytics-ktx" }
firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics-ktx" }
firebase-perf = { module = "com.google.firebase:firebase-perf-ktx" }
firebase-config = { module = "com.google.firebase:firebase-config-ktx" }
```

Update `app/build.gradle.kts`:
```kotlin
plugins {
    // ... existing plugins ...
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    // ... existing dependencies ...

    // Firebase BoM
    implementation(platform(libs.firebase.bom))

    // Firebase services
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.config)
}
```

### 3. Create Analytics Manager

`app/src/main/java/com/techventus/wikipedianews/analytics/AnalyticsManager.kt`:
```kotlin
package com.techventus.wikipedianews.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val analytics: FirebaseAnalytics = Firebase.analytics
    private val crashlytics = Firebase.crashlytics

    // Screen tracking
    fun logScreenView(screenName: String, screenClass: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // Article interactions
    fun logArticleView(articleUrl: String, articleTitle: String, source: String) {
        val bundle = Bundle().apply {
            putString("article_url", articleUrl)
            putString("article_title", articleTitle)
            putString("source", source)
        }
        analytics.logEvent("article_view", bundle)
    }

    fun logArticleShare(articleUrl: String, articleTitle: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "article")
            putString(FirebaseAnalytics.Param.ITEM_ID, articleUrl)
            putString(FirebaseAnalytics.Param.ITEM_NAME, articleTitle)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    fun logArticleBookmark(articleUrl: String, isBookmarked: Boolean) {
        val bundle = Bundle().apply {
            putString("article_url", articleUrl)
            putBoolean("is_bookmarked", isBookmarked)
        }
        analytics.logEvent("article_bookmark", bundle)
    }

    // Search tracking
    fun logSearch(query: String, resultsCount: Int) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
            putInt("results_count", resultsCount)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }

    // Settings changes
    fun logSettingChange(settingName: String, value: String) {
        val bundle = Bundle().apply {
            putString("setting_name", settingName)
            putString("setting_value", value)
        }
        analytics.logEvent("setting_changed", bundle)
    }

    // News refresh
    fun logNewsRefresh(source: String, articlesCount: Int, success: Boolean) {
        val bundle = Bundle().apply {
            putString("source", source)
            putInt("articles_count", articlesCount)
            putBoolean("success", success)
        }
        analytics.logEvent("news_refresh", bundle)
    }

    // Background sync
    fun logBackgroundSync(articlesAdded: Int, duration: Long) {
        val bundle = Bundle().apply {
            putInt("articles_added", articlesAdded)
            putLong("duration_ms", duration)
        }
        analytics.logEvent("background_sync", bundle)
    }

    // Widget usage
    fun logWidgetAdded() {
        analytics.logEvent("widget_added", Bundle())
    }

    fun logWidgetClick(articleUrl: String) {
        val bundle = Bundle().apply {
            putString("article_url", articleUrl)
        }
        analytics.logEvent("widget_article_click", bundle)
    }

    // User properties
    fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }

    // Crash reporting
    fun logException(throwable: Throwable, context: String? = null) {
        crashlytics.recordException(throwable)
        context?.let {
            crashlytics.log("Context: $it")
        }
    }

    fun setUserId(userId: String) {
        analytics.setUserId(userId)
        crashlytics.setUserId(userId)
    }

    fun logMessage(message: String) {
        crashlytics.log(message)
    }

    // Enable/disable analytics (respect user privacy)
    fun setAnalyticsEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    fun setCrashlyticsEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }
}

// Extension function for easy screen tracking
fun AnalyticsManager.trackScreen(screen: Screen) {
    logScreenView(screen.route, screen.javaClass.simpleName)
}

enum class Screen(val route: String) {
    NEWS("news"),
    BOOKMARKS("bookmarks"),
    SETTINGS("settings"),
    SOURCES("sources")
}
```

### 4. Integrate Analytics in ViewModels

Update `app/src/main/java/com/techventus/wikipedianews/ui/compose/screen/news/NewsViewModel.kt`:
```kotlin
@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val analyticsManager: AnalyticsManager  // NEW
) : ViewModel() {

    // ... existing code ...

    init {
        analyticsManager.trackScreen(Screen.NEWS)
    }

    fun refresh() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val initialCount = newsRepository.getCachedNewsCount()

            newsRepository.refreshNews().fold(
                onSuccess = {
                    val newCount = newsRepository.getCachedNewsCount()
                    analyticsManager.logNewsRefresh(
                        source = "manual",
                        articlesCount = newCount - initialCount,
                        success = true
                    )
                },
                onFailure = { error ->
                    analyticsManager.logNewsRefresh(
                        source = "manual",
                        articlesCount = 0,
                        success = false
                    )
                    analyticsManager.logException(error, "NewsViewModel.refresh")
                }
            )
        }
    }

    fun toggleBookmark(article: NewsArticle) {
        viewModelScope.launch {
            newsRepository.toggleBookmark(article.url)
            analyticsManager.logArticleBookmark(article.url, !article.isBookmarked)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty()) {
            // Track search after a short delay to avoid logging every keystroke
            viewModelScope.launch {
                delay(1000)
                if (_searchQuery.value == query) {
                    val results = (uiState.value as? UiState.Success)?.sections
                        ?.flatMap { it.articles }?.size ?: 0
                    analyticsManager.logSearch(query, results)
                }
            }
        }
    }
}
```

Update other ViewModels similarly (BookmarksViewModel, SettingsViewModel, SourcesViewModel)

### 5. Track Article Views in UI

Update `app/src/main/java/com/techventus/wikipedianews/ui/compose/component/NewsArticleCard.kt`:
```kotlin
@Composable
fun NewsArticleCard(
    article: NewsArticle,
    onBookmarkClick: () -> Unit,
    analyticsManager: AnalyticsManager = hiltViewModel<NewsViewModel>().analyticsManager  // Inject
) {
    // ... existing UI code ...

    Button(
        onClick = {
            analyticsManager.logArticleView(
                articleUrl = article.url,
                articleTitle = article.title,
                source = article.sourceType
            )
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
        }
    ) {
        // ... existing button content ...
    }

    // Share button
    IconButton(
        onClick = {
            analyticsManager.logArticleShare(article.url, article.title)
            val intent = Intent.createChooser(shareIntent, null)
            context.startActivity(intent)
        }
    ) {
        // ... existing share icon ...
    }
}
```

### 6. Update Settings for Privacy

Update `app/src/main/java/com/techventus/wikipedianews/ui/compose/screen/settings/SettingsScreen.kt`:

Add analytics toggle:
```kotlin
Card(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Analytics", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Help improve the app by sharing usage data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = analyticsEnabled,
            onCheckedChange = {
                viewModel.setAnalyticsEnabled(it)
                analyticsManager.setAnalyticsEnabled(it)
            }
        )
    }
}
```

### 7. Track Background Sync

Update `app/src/main/java/com/techventus/wikipedianews/work/NewsSyncWorker.kt`:
```kotlin
@HiltWorker
class NewsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val newsRepository: NewsRepository,
    private val notificationManager: com.techventus.wikipedianews.notification.NotificationManager,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val analyticsManager: AnalyticsManager  // NEW
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val startTime = System.currentTimeMillis()
        val initialCount = newsRepository.getCachedNewsCount()

        return try {
            newsRepository.refreshNews().getOrThrow()

            val newCount = newsRepository.getCachedNewsCount()
            val articlesAdded = newCount - initialCount
            val duration = System.currentTimeMillis() - startTime

            analyticsManager.logBackgroundSync(articlesAdded, duration)

            // ... existing notification code ...

            Result.success()
        } catch (e: Exception) {
            analyticsManager.logException(e, "NewsSyncWorker.doWork")

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
```

### 8. Create Analytics Event Documentation

`docs/ANALYTICS.md`:
```markdown
# Analytics Events

## Screen Views
- `screen_view`: User navigates to a screen
  - `screen_name`: "news", "bookmarks", "settings", "sources"
  - `screen_class`: ViewModel class name

## Article Interactions
- `article_view`: User opens an article
  - `article_url`: URL of the article
  - `article_title`: Title of the article
  - `source`: Source type (CURRENT_EVENTS, etc.)

- `article_bookmark`: User bookmarks/unbookmarks an article
  - `article_url`: URL of the article
  - `is_bookmarked`: true/false

- `share`: User shares an article
  - `content_type`: "article"
  - `item_id`: Article URL
  - `item_name`: Article title

## Search
- `search`: User performs a search
  - `search_term`: Search query
  - `results_count`: Number of results

## Settings
- `setting_changed`: User changes a setting
  - `setting_name`: Name of the setting
  - `setting_value`: New value

## News Refresh
- `news_refresh`: News data is refreshed
  - `source`: "manual", "background", "pull_to_refresh"
  - `articles_count`: Number of new articles
  - `success`: true/false

## Background Sync
- `background_sync`: Background sync completed
  - `articles_added`: Number of new articles
  - `duration_ms`: Sync duration in milliseconds

## Widget
- `widget_added`: User adds widget to home screen
- `widget_article_click`: User clicks article in widget
  - `article_url`: URL of the article

## User Properties
- `preferred_language`: User's selected Wikipedia language
- `enabled_sources_count`: Number of enabled news sources
- `theme_mode`: "light", "dark", "system"
```

### 9. Update App Initialization

Update `app/src/main/java/com/techventus/wikipedianews/App.kt`:
```kotlin
@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            analyticsManager.logMessage("App started - version ${BuildConfig.VERSION_NAME}")
        } catch (e: Exception) {
            // Firebase not configured - log but don't crash
            timber.log.Timber.w(e, "Firebase initialization failed")
        }

        // ... existing initialization ...
    }

    // ... existing code ...
}
```

## Verification Steps

1. **Setup**:
   - Create Firebase project
   - Download `google-services.json`
   - Place in `app/` directory

2. **Build & Test**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Verify Firebase Integration**:
   - Run app on device/emulator
   - Navigate through screens
   - Perform various actions (search, bookmark, refresh)
   - Check Firebase Console for events (may take 24 hours)

4. **Test Crashlytics**:
   - Force a test crash
   - Verify crash appears in Firebase Console

5. **Test Privacy**:
   - Disable analytics in settings
   - Verify events stop being sent

## Expected Results
- All user interactions tracked
- Crashes reported to Firebase
- Analytics can be disabled by user
- No PII collected
- Debug logging available

## Important Notes

**Privacy**:
- No personally identifiable information (PII) should be logged
- Users can opt-out via settings
- Comply with GDPR/privacy regulations

**Testing**:
- Use Firebase Test Lab for automated testing
- Review analytics dashboard regularly
- Set up alerts for crash rates

## Completion Marker
```bash
mkdir -p .claude/completed
echo "Analytics & monitoring integrated on $(date)" > .claude/completed/019
```

## Files Created
- `app/src/main/java/com/techventus/wikipedianews/analytics/AnalyticsManager.kt`
- `docs/ANALYTICS.md`

## Files Modified
- `.gitignore`
- `build.gradle.kts` (project)
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/java/com/techventus/wikipedianews/App.kt`
- All ViewModels
- `NewsArticleCard.kt`
- `SettingsScreen.kt`
- `NewsSyncWorker.kt`

## Prerequisites Checklist
- [ ] Firebase project created
- [ ] Android app registered in Firebase
- [ ] `google-services.json` downloaded
- [ ] `google-services.json` placed in `app/` directory
- [ ] `google-services.json` added to `.gitignore`
