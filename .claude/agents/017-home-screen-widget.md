# Micro-Agent 017: Home Screen Widget

## Completion Check
```bash
[ -f .claude/completed/017 ] && echo "✅ Already completed" && exit 0
```

## Task: Implement Android Home Screen Widget

Create a Glance-based home screen widget that displays the latest news articles from Wikipedia.

## Requirements

### 1. Add Glance Dependencies

Update `gradle/libs.versions.toml`:
```toml
[versions]
# ... existing versions ...
glance = "1.1.1"

[libraries]
# ... existing libraries ...
androidx-glance-appwidget = { module = "androidx.glance:glance-appwidget", version.ref = "glance" }
androidx-glance-material3 = { module = "androidx.glance:glance-material3", version.ref = "glance" }
```

Update `app/build.gradle.kts`:
```kotlin
dependencies {
    // ... existing dependencies ...
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
}
```

### 2. Create Widget Data Repository

`app/src/main/java/com/techventus/wikipedianews/widget/NewsWidgetRepository.kt`:
```kotlin
package com.techventus.wikipedianews.widget

import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.model.repository.NewsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NewsWidgetRepository @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend fun getLatestArticles(limit: Int = 5): List<NewsArticle> {
        return try {
            newsRepository.observeNews()
                .first()
                .flatMap { it.articles }
                .take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
```

### 3. Create Widget Receiver

`app/src/main/java/com/techventus/wikipedianews/widget/NewsWidgetReceiver.kt`:
```kotlin
package com.techventus.wikipedianews.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class NewsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NewsWidget()
}
```

### 4. Create Widget UI

`app/src/main/java/com/techventus/wikipedianews/widget/NewsWidget.kt`:
```kotlin
package com.techventus.wikipedianews.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.model.domain.NewsArticle
import com.techventus.wikipedianews.ui.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow

class NewsWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NewsWidgetEntryPoint {
        fun newsWidgetRepository(): NewsWidgetRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get repository via Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            NewsWidgetEntryPoint::class.java
        )
        val repository = entryPoint.newsWidgetRepository()

        // Fetch latest articles
        val articles = repository.getLatestArticles(limit = 5)

        provideContent {
            GlanceTheme {
                NewsWidgetContent(articles)
            }
        }
    }
}

@Composable
fun NewsWidgetContent(articles: List<NewsArticle>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_launcher_foreground),
                contentDescription = "Brief logo",
                modifier = GlanceModifier.size(32.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "Brief News",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onBackground
                )
            )
        }

        if (articles.isEmpty()) {
            // Empty state
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No news available\nTap to refresh",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant
                    ),
                    modifier = GlanceModifier.clickable(
                        actionStartActivity<MainActivity>()
                    )
                )
            }
        } else {
            // Article list
            LazyColumn {
                items(articles) { article ->
                    NewsArticleItem(article)
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun NewsArticleItem(article: NewsArticle) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(8.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Text(
            text = article.title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurface
            ),
            maxLines = 2
        )

        if (article.section.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = article.section,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}
```

### 5. Create Widget Worker for Updates

`app/src/main/java/com/techventus/wikipedianews/widget/NewsWidgetWorker.kt`:
```kotlin
package com.techventus.wikipedianews.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NewsWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Update all widgets
            NewsWidget().updateAll(context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

### 6. Schedule Widget Updates

Update `app/src/main/java/com/techventus/wikipedianews/App.kt`:
```kotlin
import androidx.work.*
import java.util.concurrent.TimeUnit

class App : Application(), Configuration.Provider {
    // ... existing code ...

    override fun onCreate() {
        super.onCreate()
        // ... existing initialization ...

        scheduleWidgetUpdates()
    }

    private fun scheduleWidgetUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val widgetUpdateWork = PeriodicWorkRequestBuilder<NewsWidgetWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "widget_update",
                ExistingPeriodicWorkPolicy.KEEP,
                widgetUpdateWork
            )
    }
}
```

### 7. Update AndroidManifest.xml

Add widget receiver:
```xml
<receiver
    android:name=".widget.NewsWidgetReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/news_widget_info" />
</receiver>
```

### 8. Create Widget Configuration

`app/src/main/res/xml/news_widget_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/widget_description"
    android:initialLayout="@layout/news_widget_loading"
    android:minWidth="180dp"
    android:minHeight="180dp"
    android:previewImage="@drawable/news_widget_preview"
    android:resizeMode="horizontal|vertical"
    android:targetCellWidth="3"
    android:targetCellHeight="3"
    android:updatePeriodMillis="3600000"
    android:widgetCategory="home_screen" />
```

### 9. Add String Resources

Update `app/src/main/res/values/strings.xml`:
```xml
<string name="widget_description">Shows latest news from Wikipedia</string>
```

### 10. Create Loading Layout (Fallback)

`app/src/main/res/layout/news_widget_loading.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Loading news..."
        android:textSize="14sp" />
</LinearLayout>
```

## Verification Steps

1. Build and install the app
2. Long-press on home screen
3. Find "Brief News" in widgets list
4. Add widget to home screen
5. Verify widget shows latest articles
6. Verify tapping widget opens app
7. Verify widget updates periodically

## Expected Behavior
- Widget displays 5 latest news articles
- Updates every hour automatically
- Tapping widget opens main app
- Shows "Loading" state initially
- Handles empty state gracefully

## Completion Marker
```bash
mkdir -p .claude/completed
echo "Home screen widget implemented on $(date)" > .claude/completed/017
```

## Files Created
- `app/src/main/java/com/techventus/wikipedianews/widget/NewsWidgetRepository.kt`
- `app/src/main/java/com/techventus/wikipedianews/widget/NewsWidgetReceiver.kt`
- `app/src/main/java/com/techventus/wikipedianews/widget/NewsWidget.kt`
- `app/src/main/java/com/techventus/wikipedianews/widget/NewsWidgetWorker.kt`
- `app/src/main/res/xml/news_widget_info.xml`
- `app/src/main/res/layout/news_widget_loading.xml`
- Updated: `app/src/main/AndroidManifest.xml`
- Updated: `app/src/main/java/com/techventus/wikipedianews/App.kt`
- Updated: `gradle/libs.versions.toml`
- Updated: `app/build.gradle.kts`
