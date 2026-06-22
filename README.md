# Brief

A free Android news reader. Brief pulls the day's headlines from Wikipedia's
[Current events portal](https://en.wikipedia.org/wiki/Portal:Current_events), caches them for offline
reading, and keeps them current in the background.

[On Google Play](https://play.google.com/store/apps/details?id=com.techventus.wikipedianews)

## Features

- Browse current-events headlines grouped by day, in a Jetpack Compose UI
- Offline reading: articles are cached locally (Room), so the last sync is always available
- Bookmarks: save articles to read later
- Search across fetched articles
- Background sync with optional new-headline notifications (WorkManager)
- Light and dark themes

## Stack

Kotlin, Jetpack Compose, Hilt, Room, Retrofit, WorkManager, Navigation Compose. minSdk 28,
targetSdk 36, current version 2.0.

## Build

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and run the `app` configuration.

## Sources

Brief currently has a single feed: Wikipedia's Current events portal. That keeps it neutral and free
of API keys, but it is also the main limitation. Adding more feeds, and choosing trustworthy ones, is
the open product question and is not yet decided.
