// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.2") // Keep the latest AGP version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") // Match Kotlin version in module
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}