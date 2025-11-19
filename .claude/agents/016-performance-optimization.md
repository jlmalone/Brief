# Micro-Agent 016: Performance Optimization

## Completion Check
```bash
[ -f .claude/completed/016 ] && echo "✅ Already completed" && exit 0
```

## Task: Performance Optimization and Build Hardening

Optimize the Brief app for production with R8/ProGuard, resource shrinking, and performance improvements.

## Requirements

### 1. Update `app/build.gradle.kts` - Enable R8 and Optimizations

```kotlin
android {
    // ... existing config ...

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") // For now, use debug signing
        }

        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    // Enable baseline profiles for better startup performance
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"

        // Optimization flags
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }

    // Enable resource optimization
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/previous-compilation-data.bin"
        }
    }
}
```

### 2. Create/Update `app/proguard-rules.pro`

```proguard
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Android/Sdk/tools/proguard/proguard-android.txt

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Retrofit interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep data classes and their properties
-keepclassmembers class com.techventus.wikipedianews.model.** {
    <fields>;
    <init>(...);
}

# Keep Parcelize
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Remove logging in release
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimization flags
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

### 3. Create `app/src/main/res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- Allow Wikipedia domains -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">wikipedia.org</domain>
        <domain includeSubdomains="true">wikimedia.org</domain>
    </domain-config>
</network-security-config>
```

### 4. Update `app/src/main/AndroidManifest.xml`

Add network security config:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
```

### 5. Create `app/src/debug/kotlin/com/techventus/wikipedianews/DebugApp.kt`

For debug-only initialization (StrictMode, LeakCanary):
```kotlin
package com.techventus.wikipedianews

import android.os.StrictMode
import timber.log.Timber

class DebugApp : App() {
    override fun onCreate() {
        super.onCreate()

        // Enable StrictMode in debug builds
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        Timber.plant(Timber.DebugTree())
        Timber.d("Debug mode enabled with StrictMode")
    }
}
```

### 6. Update `app/src/debug/AndroidManifest.xml`

Create this file to use DebugApp in debug builds:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".DebugApp"
        android:tools:replace="android:name"
        xmlns:tools="http://schemas.android.com/tools" />
</manifest>
```

### 7. Add Performance Monitoring Dependencies

Update `gradle/libs.versions.toml`:
```toml
[versions]
# ... existing versions ...
firebase-bom = "33.5.1"
leakcanary = "2.14"

[libraries]
# ... existing libraries ...
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics-ktx" }
firebase-analytics = { module = "com.google.firebase:firebase-analytics-ktx" }
firebase-perf = { module = "com.google.firebase:firebase-perf-ktx" }
leakcanary = { module = "com.squareup.leakcanary:leakcanary-android", version.ref = "leakcanary" }
```

Update `app/build.gradle.kts`:
```kotlin
dependencies {
    // ... existing dependencies ...

    // Performance monitoring (optional - requires Firebase setup)
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.firebase.crashlytics)
    // implementation(libs.firebase.analytics)
    // implementation(libs.firebase.perf)

    // Memory leak detection (debug only)
    debugImplementation(libs.leakcanary)
}
```

## Verification Steps

1. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

2. Check APK size reduction:
   ```bash
   ls -lh app/build/outputs/apk/release/*.apk
   ```

3. Verify ProGuard mapping file created:
   ```bash
   ls -lh app/build/outputs/mapping/release/
   ```

4. Test release build on device
5. Verify no crashes with minification enabled
6. Run performance profiler to confirm improvements

## Expected Results
- APK size reduced by ~30-40%
- Faster app startup time
- Better memory management
- Secure network configuration
- Debug tools enabled in debug builds only

## Completion Marker
```bash
mkdir -p .claude/completed
echo "Performance optimization completed on $(date)" > .claude/completed/016
```

## Files Created/Modified
- `app/build.gradle.kts` (updated)
- `app/proguard-rules.pro` (created/updated)
- `app/src/main/res/xml/network_security_config.xml` (created)
- `app/src/main/AndroidManifest.xml` (updated)
- `app/src/debug/kotlin/com/techventus/wikipedianews/DebugApp.kt` (created)
- `app/src/debug/AndroidManifest.xml` (created)
- `gradle/libs.versions.toml` (updated)
