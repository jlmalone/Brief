package com.techventus.wikipedianews.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics Manager for tracking app usage and crashes
 *
 * NOTE: Requires Firebase setup to function. Until google-services.json is added:
 * 1. Create Firebase project at https://console.firebase.google.com
 * 2. Register Android app with package: com.techventus.wikipedianews
 * 3. Download google-services.json to app/ directory
 * 4. Uncomment Firebase dependencies in build.gradle.kts
 * 5. Add Firebase plugins to build.gradle.kts
 *
 * For now, this implementation logs events locally for development.
 */
@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "AnalyticsManager"

    // Screen tracking
    fun logScreenView(screenName: String, screenClass: String) {
        Log.d(TAG, "Screen View: $screenName ($screenClass)")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
        //     putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        //     putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        // })
    }

    // Article interactions
    fun logArticleView(articleUrl: String, articleTitle: String, source: String) {
        Log.d(TAG, "Article View: $articleTitle (source: $source)")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent("article_view", Bundle().apply {
        //     putString("article_url", articleUrl)
        //     putString("article_title", articleTitle)
        //     putString("source", source)
        // })
    }

    fun logArticleShare(articleUrl: String, articleTitle: String) {
        Log.d(TAG, "Article Share: $articleTitle")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent(FirebaseAnalytics.Event.SHARE, Bundle().apply {
        //     putString(FirebaseAnalytics.Param.CONTENT_TYPE, "article")
        //     putString(FirebaseAnalytics.Param.ITEM_ID, articleUrl)
        //     putString(FirebaseAnalytics.Param.ITEM_NAME, articleTitle)
        // })
    }

    fun logArticleBookmark(articleUrl: String, isBookmarked: Boolean) {
        Log.d(TAG, "Article Bookmark: $articleUrl (bookmarked: $isBookmarked)")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent("article_bookmark", Bundle().apply {
        //     putString("article_url", articleUrl)
        //     putBoolean("is_bookmarked", isBookmarked)
        // })
    }

    // Search tracking
    fun logSearch(query: String, resultsCount: Int) {
        Log.d(TAG, "Search: $query (results: $resultsCount)")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent(FirebaseAnalytics.Event.SEARCH, Bundle().apply {
        //     putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
        //     putInt("results_count", resultsCount)
        // })
    }

    // Settings changes
    fun logSettingChange(settingName: String, value: String) {
        Log.d(TAG, "Setting Change: $settingName = $value")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent("setting_changed", Bundle().apply {
        //     putString("setting_name", settingName)
        //     putString("setting_value", value)
        // })
    }

    // News refresh
    fun logNewsRefresh(source: String, articlesCount: Int, success: Boolean) {
        Log.d(TAG, "News Refresh: source=$source, count=$articlesCount, success=$success")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent("news_refresh", Bundle().apply {
        //     putString("source", source)
        //     putInt("articles_count", articlesCount)
        //     putBoolean("success", success)
        // })
    }

    // Background sync
    fun logBackgroundSync(articlesAdded: Int, duration: Long) {
        Log.d(TAG, "Background Sync: added=$articlesAdded, duration=${duration}ms")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent("background_sync", Bundle().apply {
        //     putInt("articles_added", articlesAdded)
        //     putLong("duration_ms", duration)
        // })
    }

    // Widget usage
    fun logWidgetAdded() {
        Log.d(TAG, "Widget Added")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent("widget_added", Bundle())
    }

    fun logWidgetClick(articleUrl: String) {
        Log.d(TAG, "Widget Article Click: $articleUrl")
        // TODO: Uncomment when Firebase is configured
        // analytics.logEvent("widget_article_click", Bundle().apply {
        //     putString("article_url", articleUrl)
        // })
    }

    // User properties
    fun setUserProperty(name: String, value: String) {
        Log.d(TAG, "User Property: $name = $value")
        // TODO: Uncomment when Firebase is configured
        // analytics.setUserProperty(name, value)
    }

    // Crash reporting
    fun logException(throwable: Throwable, context: String? = null) {
        Log.e(TAG, "Exception: ${context ?: "Unknown context"}", throwable)
        // TODO: Uncomment when Firebase is configured
        // crashlytics.recordException(throwable)
        // context?.let { crashlytics.log("Context: $it") }
    }

    fun setUserId(userId: String) {
        Log.d(TAG, "User ID: $userId")
        // TODO: Uncomment when Firebase is configured
        // analytics.setUserId(userId)
        // crashlytics.setUserId(userId)
    }

    fun logMessage(message: String) {
        Log.d(TAG, "Message: $message")
        // TODO: Uncomment when Firebase is configured
        // crashlytics.log(message)
    }

    // Enable/disable analytics (respect user privacy)
    fun setAnalyticsEnabled(enabled: Boolean) {
        Log.d(TAG, "Analytics Enabled: $enabled")
        // TODO: Uncomment when Firebase is configured
        // analytics.setAnalyticsCollectionEnabled(enabled)
    }

    fun setCrashlyticsEnabled(enabled: Boolean) {
        Log.d(TAG, "Crashlytics Enabled: $enabled")
        // TODO: Uncomment when Firebase is configured
        // crashlytics.setCrashlyticsCollectionEnabled(enabled)
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
