plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.techventus.wikipedianews"
    compileSdk = 35

    useLibrary("org.apache.http.legacy")

    defaultConfig {
        applicationId = "com.techventus.wikipedianews"
        minSdk = 28
        targetSdk = 35
        versionCode = 7
        versionName = "1.6"
        multiDexEnabled = true
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("dev") {
            dimension = "version"
            applicationIdSuffix = ".dev"
            buildConfigField("boolean", "USE_HARDCODED_DATA", "true")
        }
        create("real") {
            dimension = "version"
            buildConfigField("boolean", "USE_HARDCODED_DATA", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.20") // Add Kotlin stdlib
    implementation("org.jsoup:jsoup:1.17.2")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    // implementation("androidx.multidex:multidex:2.0.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("org.apache.commons:commons-lang3:3.14.0")

    testImplementation("junit:junit:4.13.2")
}