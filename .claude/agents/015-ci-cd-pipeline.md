# Micro-Agent 015: CI/CD Pipeline Setup

## Completion Check
```bash
[ -f .claude/completed/015 ] && echo "✅ Already completed" && exit 0
```

## Task: GitHub Actions CI/CD Pipeline

Create a comprehensive CI/CD pipeline for the Brief app using GitHub Actions to automate testing, building, and quality checks.

## Requirements

### 1. Create `.github/workflows/ci.yml`
```yaml
name: Android CI

on:
  push:
    branches: [ master, develop ]
  pull_request:
    branches: [ master, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run unit tests
        run: ./gradlew test

      - name: Upload test reports
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-reports
          path: app/build/reports/tests/

      - name: Run ktlint
        run: ./gradlew ktlintCheck

      - name: Run detekt
        run: ./gradlew detekt

      - name: Upload detekt report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: detekt-report
          path: app/build/reports/detekt/

  build:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
```

### 2. Create `.github/workflows/release.yml`
```yaml
name: Release Build

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build release APK
        run: ./gradlew assembleRelease

      - name: Upload release APK
        uses: actions/upload-artifact@v4
        with:
          name: app-release
          path: app/build/outputs/apk/release/*.apk

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v1
        with:
          files: app/build/outputs/apk/release/*.apk
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### 3. Create `.github/workflows/pr-checks.yml`
```yaml
name: PR Checks

on:
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  pr-validation:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Compile check
        run: ./gradlew compileDebugKotlin

      - name: Lint check
        run: ./gradlew lint

      - name: Test coverage
        run: ./gradlew jacocoTestReport

      - name: Comment PR with coverage
        uses: madrapps/jacoco-report@v1.6.1
        if: github.event_name == 'pull_request'
        with:
          paths: ${{ github.workspace }}/app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
          token: ${{ secrets.GITHUB_TOKEN }}
          min-coverage-overall: 70
          min-coverage-changed-files: 80
```

### 4. Update `app/build.gradle.kts`
Add JaCoCo for test coverage:
```kotlin
plugins {
    // existing plugins...
    jacoco
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/inject/**/*.*"
    )

    val debugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom(files("${projectDir}/src/main/java"))
    executionData.setFrom(fileTree(buildDir) {
        include("jacoco/testDebugUnitTest.exec")
    })
}
```

### 5. Create `.github/dependabot.yml`
```yaml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
    labels:
      - "dependencies"
```

## Verification
After creating all files:
1. Commit and push to a branch
2. Create a PR to trigger workflows
3. Verify all checks pass
4. Confirm artifacts are uploaded

## Completion Marker
```bash
mkdir -p .claude/completed
echo "CI/CD Pipeline configured on $(date)" > .claude/completed/015
```

## Files Created
- `.github/workflows/ci.yml`
- `.github/workflows/release.yml`
- `.github/workflows/pr-checks.yml`
- `.github/dependabot.yml`
- Updated `app/build.gradle.kts`
