# Analytics Events

## Overview

Brief app uses Firebase Analytics for usage tracking and Crashlytics for crash reporting to improve app quality and user experience.

## Firebase Setup Required

Before analytics will work, you must:

1. Create Firebase project at https://console.firebase.google.com
2. Register Android app with package name: `com.techventus.wikipedianews`
3. Download `google-services.json` to `app/` directory
4. Update `build.gradle.kts` (project level) to add Firebase plugins:
   ```kotlin
   plugins {
       id("com.google.gms.google-services") version "4.4.2" apply false
       id("com.google.firebase.crashlytics") version "3.0.2" apply false
   }
   ```
5. Update `gradle/libs.versions.toml` with Firebase dependencies
6. Update `app/build.gradle.kts` to apply Firebase plugins and add dependencies
7. Uncomment Firebase code in `AnalyticsManager.kt`

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

## Privacy

- No personally identifiable information (PII) is logged
- Users can opt-out via settings
- Complies with GDPR/privacy regulations
- Analytics collection is disabled by default until user opts in

## Development

Until Firebase is configured, AnalyticsManager logs events to Android Logcat with tag "AnalyticsManager" for development and testing purposes.
