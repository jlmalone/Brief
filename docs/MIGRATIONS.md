# Database Migration Documentation

## Overview

This document describes the database schema evolution, migration strategy, version history, and rollback procedures for the **Brief** application. The database uses Room Persistence Library with SQLite as the underlying engine.

---

## Migration Strategy

### Current Approach: Destructive Migration ⚠️

**Status**: Development Only - Not Production Ready

```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
    .fallbackToDestructiveMigration() // ⚠️ Deletes all data on schema changes
    .build()
```

**Behavior**:
- On schema version mismatch, Room **drops all tables** and recreates them
- **All user data is lost** (bookmarks, cached articles, etc.)
- No migration code required
- Simple for rapid development

**Impact**:
- ❌ **Data Loss**: Users lose all bookmarked articles
- ❌ **Poor UX**: App becomes empty after updates
- ❌ **Not Production-Ready**: Violates data persistence expectations
- ✅ **Fast Development**: No migration code to write/test

---

## Production Migration Strategy

### Recommended Approach: Incremental Migrations

**Implementation**: Define migration paths between versions

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add isBookmarked column with default value
        database.execSQL(
            "ALTER TABLE news_articles ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Future migration logic here
    }
}

// Apply migrations
Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .build()
```

**Benefits**:
- ✅ **Data Preservation**: Existing data retained
- ✅ **User Trust**: Bookmarks and preferences persist
- ✅ **Graceful Updates**: Seamless app updates
- ✅ **Testable**: Migration logic can be unit tested

---

## Version History

### Schema Version 2 (Current)

**Release Date**: November 2024
**Git Commit**: `f72e743` - "Phase 7: Bookmarks/Favorites Feature"
**Migration**: Destructive (v1 → v2)

#### Changes

**Added**:
- `isBookmarked` column to `news_articles` table
  - Type: `INTEGER` (Boolean: 0=false, 1=true)
  - Constraint: `NOT NULL`
  - Default: `0` (false)

**DAO Updates**:
- Added `observeBookmarkedArticles()` - Flow<List<NewsArticleEntity>>
- Added `getBookmarkedArticles()` - suspend function
- Added `getBookmarkedCount()` - suspend function
- Added `updateBookmarkStatus(articleId, isBookmarked)` - suspend function

**Migration SQL** (what should have been used):
```sql
-- Add new column with default value
ALTER TABLE news_articles
ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0;
```

**Actual Behavior**:
```sql
-- ⚠️ Destructive migration: All data lost
DROP TABLE IF EXISTS news_articles;

CREATE TABLE news_articles (
    id TEXT PRIMARY KEY NOT NULL,
    sectionHeader TEXT NOT NULL,
    title TEXT NOT NULL,
    htmlContent TEXT NOT NULL,
    url TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    cachedAt INTEGER NOT NULL,
    isBookmarked INTEGER NOT NULL DEFAULT 0
);
```

**Impact**:
- Users lost all cached articles
- No bookmarks existed in v1, so no bookmark data lost
- Fresh install experience for all users after update

---

### Schema Version 1 (Initial)

**Release Date**: October 2024
**Git Commit**: `d71fd7b` - "Phase 2: Offline-First Caching with Room"
**Migration**: Initial schema creation

#### Schema Definition

```sql
CREATE TABLE news_articles (
    id TEXT PRIMARY KEY NOT NULL,
    sectionHeader TEXT NOT NULL,
    title TEXT NOT NULL,
    htmlContent TEXT NOT NULL,
    url TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    cachedAt INTEGER NOT NULL
);
```

**Initial Features**:
- Offline-first caching
- Article persistence
- Section-based grouping
- Timestamp-based sorting
- Cache expiration support

**DAO Methods** (v1):
- `observeAllArticles()`
- `getAllArticles()`
- `insertArticles()`
- `deleteAllArticles()`
- `deleteExpiredArticles()`

---

## Proper Migration Implementation Guide

### Step 1: Define Migration Objects

Create `app/src/main/java/com/techventus/wikipedianews/model/database/migrations/DatabaseMigrations.kt`:

```kotlin
package com.techventus.wikipedianews.model.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    /**
     * Migration from version 1 to version 2
     * Adds bookmark functionality
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add isBookmarked column with default value
            database.execSQL(
                """
                ALTER TABLE news_articles
                ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
            )
        }
    }

    /**
     * Example: Migration from version 2 to version 3
     * Adds reading history tracking
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create new reading_history table
            database.execSQL(
                """
                CREATE TABLE reading_history (
                    id TEXT PRIMARY KEY NOT NULL,
                    articleId TEXT NOT NULL,
                    readAt INTEGER NOT NULL,
                    scrollPosition INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(articleId) REFERENCES news_articles(id)
                        ON DELETE CASCADE
                )
                """.trimIndent()
            )

            // Create index for fast lookups
            database.execSQL(
                """
                CREATE INDEX idx_reading_history_article
                ON reading_history(articleId)
                """.trimIndent()
            )
        }
    }

    /**
     * Example: Migration from version 3 to version 4
     * Adds full-text search support
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create FTS4 virtual table
            database.execSQL(
                """
                CREATE VIRTUAL TABLE articles_fts USING fts4(
                    content='news_articles',
                    title,
                    htmlContent,
                    sectionHeader
                )
                """.trimIndent()
            )

            // Populate FTS table with existing data
            database.execSQL(
                """
                INSERT INTO articles_fts(rowid, title, htmlContent, sectionHeader)
                SELECT rowid, title, htmlContent, sectionHeader
                FROM news_articles
                """.trimIndent()
            )

            // Create triggers to keep FTS in sync
            database.execSQL(
                """
                CREATE TRIGGER articles_fts_insert AFTER INSERT ON news_articles
                BEGIN
                    INSERT INTO articles_fts(rowid, title, htmlContent, sectionHeader)
                    VALUES (new.rowid, new.title, new.htmlContent, new.sectionHeader);
                END
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TRIGGER articles_fts_delete AFTER DELETE ON news_articles
                BEGIN
                    DELETE FROM articles_fts WHERE rowid = old.rowid;
                END
                """.trimIndent()
            )

            database.execSQL(
                """
                CREATE TRIGGER articles_fts_update AFTER UPDATE ON news_articles
                BEGIN
                    UPDATE articles_fts
                    SET title = new.title,
                        htmlContent = new.htmlContent,
                        sectionHeader = new.sectionHeader
                    WHERE rowid = new.rowid;
                END
                """.trimIndent()
            )
        }
    }

    /**
     * All migrations in chronological order
     */
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4
    )
}
```

### Step 2: Update DatabaseModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS) // ✅ Add migrations
        // .fallbackToDestructiveMigration() // ❌ Remove for production
        .build()
    }

    @Provides
    fun provideNewsDao(database: AppDatabase): NewsDao {
        return database.newsDao()
    }
}
```

### Step 3: Test Migrations

Create `app/src/androidTest/java/com/techventus/wikipedianews/database/MigrationTest.kt`:

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration_test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate1To2_containsCorrectData() {
        // Create v1 database
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                INSERT INTO news_articles
                (id, sectionHeader, title, htmlContent, url, timestamp, cachedAt)
                VALUES
                ('test1', 'News', 'Test Article', '<p>Content</p>',
                 'https://example.com', 1699564800000, 1699564800000)
                """.trimIndent()
            )
            close()
        }

        // Run migration
        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            2,
            true,
            DatabaseMigrations.MIGRATION_1_2
        )

        // Verify data
        db.query("SELECT * FROM news_articles").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            cursor.moveToFirst()

            val idIndex = cursor.getColumnIndex("id")
            val titleIndex = cursor.getColumnIndex("title")
            val bookmarkIndex = cursor.getColumnIndex("isBookmarked")

            assertThat(cursor.getString(idIndex)).isEqualTo("test1")
            assertThat(cursor.getString(titleIndex)).isEqualTo("Test Article")
            assertThat(cursor.getInt(bookmarkIndex)).isEqualTo(0) // Default value
        }
    }

    @Test
    fun migrate2To3_createsReadingHistoryTable() {
        // Start with v2 database
        helper.createDatabase(TEST_DB, 2).apply {
            // Insert test data
            close()
        }

        // Run migration to v3
        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            3,
            true,
            DatabaseMigrations.MIGRATION_2_3
        )

        // Verify reading_history table exists
        db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='reading_history'"
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }
    }

    @Test
    fun migrateAll_successfullyMigrates1To4() {
        // Create v1 database
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert v1 data
            close()
        }

        // Run all migrations
        helper.runMigrationsAndValidate(
            TEST_DB,
            4,
            true,
            DatabaseMigrations.MIGRATION_1_2,
            DatabaseMigrations.MIGRATION_2_3,
            DatabaseMigrations.MIGRATION_3_4
        )

        // Verify final schema is correct
        // Room will validate schema automatically
    }
}
```

---

## Rollback Procedures

### Understanding Rollbacks

**Key Principle**: SQLite does not support schema rollbacks. Once a migration runs, it cannot be automatically reversed.

### Rollback Strategies

#### Strategy 1: Destructive Rollback (Data Loss)

**Use Case**: Critical bugs in new schema version

```kotlin
// Emergency rollback configuration
Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
    .fallbackToDestructiveMigrationOnDowngrade() // Allows downgrade
    .build()
```

**Behavior**:
- App version downgrade triggers database recreation
- **All data is lost**
- Use only in emergencies

**Steps**:
1. Release hotfix with old schema version
2. Database recreated on app update
3. Users lose all local data

#### Strategy 2: Reverse Migration (Data Preserved)

**Use Case**: Planned rollback with data retention

```kotlin
val MIGRATION_3_2 = object : Migration(3, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Reverse changes from MIGRATION_2_3
        database.execSQL("DROP TABLE reading_history")
    }
}

Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_2)
    .build()
```

**Limitations**:
- Cannot reverse data transformations (e.g., column deletion)
- Complex migrations may not be reversible
- Requires planning during migration design

#### Strategy 3: Backup and Restore

**Use Case**: Safe rollback with full data recovery

**Pre-Migration Backup**:
```kotlin
fun backupDatabase(context: Context): File {
    val currentDb = context.getDatabasePath(AppDatabase.DATABASE_NAME)
    val backupFile = File(context.filesDir, "brief_backup_v${BuildConfig.VERSION_CODE}.db")
    currentDb.copyTo(backupFile, overwrite = true)
    return backupFile
}
```

**Restore on Failure**:
```kotlin
fun restoreDatabase(context: Context, backupFile: File) {
    val currentDb = context.getDatabasePath(AppDatabase.DATABASE_NAME)
    // Close all database connections first
    AppDatabase.getInstance(context).close()
    backupFile.copyTo(currentDb, overwrite = true)
}
```

**Automated Backup Strategy**:
```kotlin
@Provides
@Singleton
fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    // Backup before migration
    val callback = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // Perform backup on first open after update
        }
    }

    return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
        .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        .addCallback(callback)
        .build()
}
```

---

## Data Migration Scripts

### Script 1: Export Data Before Migration

```kotlin
/**
 * Export all articles to JSON for backup
 */
suspend fun exportArticlesToJson(context: Context): File {
    val dao = AppDatabase.getInstance(context).newsDao()
    val articles = dao.getAllArticles()

    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val jsonString = json.encodeToString(articles)
    val exportFile = File(context.filesDir, "articles_export_${System.currentTimeMillis()}.json")
    exportFile.writeText(jsonString)

    return exportFile
}
```

### Script 2: Import Data After Migration

```kotlin
/**
 * Import articles from JSON backup
 */
suspend fun importArticlesFromJson(context: Context, jsonFile: File) {
    val dao = AppDatabase.getInstance(context).newsDao()
    val json = Json { ignoreUnknownKeys = true }

    val jsonString = jsonFile.readText()
    val articles = json.decodeFromString<List<NewsArticleEntity>>(jsonString)

    dao.insertArticles(articles)
}
```

### Script 3: Transform Data During Migration

```kotlin
/**
 * Example: Migrate from single author to multiple authors
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create new authors table
        database.execSQL(
            """
            CREATE TABLE authors (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Step 2: Create article-author junction table
        database.execSQL(
            """
            CREATE TABLE article_authors (
                articleId TEXT NOT NULL,
                authorId TEXT NOT NULL,
                PRIMARY KEY(articleId, authorId),
                FOREIGN KEY(articleId) REFERENCES news_articles(id) ON DELETE CASCADE,
                FOREIGN KEY(authorId) REFERENCES authors(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // Step 3: Migrate existing author data (if any)
        // This would require custom logic based on old schema
    }
}
```

---

## Migration Testing Checklist

### Pre-Migration Testing

- [ ] Export schema JSON for current version
- [ ] Write unit tests for migration logic
- [ ] Test migration on emulator with realistic data
- [ ] Test migration on physical device
- [ ] Verify all queries work after migration
- [ ] Check app startup time after migration
- [ ] Test rollback procedure (if applicable)

### Post-Migration Validation

- [ ] Verify all data preserved correctly
- [ ] Check all indexes created
- [ ] Validate foreign key constraints
- [ ] Test all DAO methods
- [ ] Verify app performance unchanged
- [ ] Check database file size reasonable
- [ ] Review crash analytics for migration errors

### Automated Testing

```kotlin
@RunWith(Parameterized::class)
class AllMigrationsTest(
    private val startVersion: Int,
    private val endVersion: Int
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): List<Array<Int>> {
            return listOf(
                arrayOf(1, 2),
                arrayOf(2, 3),
                arrayOf(3, 4),
                arrayOf(1, 4) // Full migration path
            )
        }
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun testMigration() {
        helper.createDatabase(TEST_DB, startVersion).close()

        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            endVersion,
            true,
            *getMigrationsToTest()
        )

        // Schema validation happens automatically
        db.close()
    }

    private fun getMigrationsToTest(): Array<Migration> {
        return DatabaseMigrations.ALL_MIGRATIONS.filter {
            it.startVersion >= startVersion && it.endVersion <= endVersion
        }.toTypedArray()
    }
}
```

---

## Migration Best Practices

### DO ✅

1. **Always write migrations for production**
   - Never use `fallbackToDestructiveMigration()` in production
   - Preserve user data at all costs

2. **Test migrations thoroughly**
   - Unit test each migration
   - Test migration paths (v1→v2, v1→v3, etc.)
   - Test on devices with real data

3. **Use ALTER TABLE when possible**
   - Adding columns: `ALTER TABLE ADD COLUMN`
   - Simple and fast
   - No data copying required

4. **Version schema exports**
   - Commit schema JSON files to git
   - Tag releases with schema version
   - Maintain version history

5. **Document all changes**
   - Update this MIGRATIONS.md file
   - Include rationale for changes
   - Note any breaking changes

6. **Handle edge cases**
   - Null values
   - Default values
   - Data type conversions
   - Constraint violations

7. **Provide rollback plan**
   - Document how to reverse migration
   - Implement backup/restore
   - Test downgrade scenarios

### DON'T ❌

1. **Don't use destructive migration in production**
   - Users lose all data
   - Poor user experience
   - Damages trust

2. **Don't skip migration versions**
   - Always provide incremental migrations
   - Example: v1→v2, v2→v3 (not v1→v3)
   - Room will chain migrations automatically

3. **Don't modify old migrations**
   - Once released, migrations are immutable
   - Create new migration instead
   - Old code may still reference old migrations

4. **Don't forget to test edge cases**
   - Empty databases
   - Large databases (1000+ rows)
   - Corrupted data
   - Concurrent access during migration

5. **Don't ignore migration errors**
   - Log all migration failures
   - Send errors to analytics
   - Provide fallback UX

---

## Schema Export Configuration

### Gradle Configuration

Add to `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        // ... other config

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }
}
```

### Expected Directory Structure

```
app/
├── schemas/
│   └── com.techventus.wikipedianews.model.database.AppDatabase/
│       ├── 1.json  // Schema version 1
│       ├── 2.json  // Schema version 2
│       ├── 3.json  // Schema version 3
│       └── 4.json  // Schema version 4
└── src/
    ├── androidTest/
    │   └── assets/  // Schemas copied here for testing
    └── main/
```

### Schema JSON Example (Version 2)

```json
{
  "formatVersion": 1,
  "database": {
    "version": 2,
    "identityHash": "abc123...",
    "entities": [
      {
        "tableName": "news_articles",
        "createSql": "CREATE TABLE IF NOT EXISTS `news_articles` (...)",
        "fields": [
          {
            "fieldPath": "id",
            "columnName": "id",
            "affinity": "TEXT",
            "notNull": true
          },
          {
            "fieldPath": "isBookmarked",
            "columnName": "isBookmarked",
            "affinity": "INTEGER",
            "notNull": true,
            "defaultValue": "0"
          }
        ],
        "primaryKey": {
          "columnNames": ["id"],
          "autoGenerate": false
        },
        "indices": [],
        "foreignKeys": []
      }
    ],
    "setupQueries": [
      "CREATE TABLE IF NOT EXISTS room_master_table (...)"
    ]
  }
}
```

---

## Monitoring Migration Success

### Analytics Events

```kotlin
class MigrationCallback : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)

        // Log successful migration
        val version = db.version
        Analytics.logEvent("database_opened", mapOf(
            "version" to version,
            "timestamp" to System.currentTimeMillis()
        ))
    }

    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
        super.onDestructiveMigration(db)

        // Log destructive migration (should never happen in production)
        Analytics.logEvent("database_destructive_migration", mapOf(
            "version" to db.version,
            "timestamp" to System.currentTimeMillis()
        ))

        // Alert: This should trigger immediate investigation
        CrashlyticsLogger.logError("Destructive migration occurred in production!")
    }
}
```

### Error Handling

```kotlin
try {
    val database = Room.databaseBuilder(...)
        .addMigrations(...)
        .build()
} catch (e: IllegalStateException) {
    // Migration failed
    CrashlyticsLogger.logException(e)

    // Fallback: Offer to reset database
    showDatabaseResetDialog()
}
```

---

## Future Migration Planning

### Planned Migrations (Roadmap)

#### Version 3: Reading History
- Add `reading_history` table
- Track read articles
- Store scroll position
- Enable "continue reading" feature

#### Version 4: Full-Text Search
- Add FTS4 virtual table
- Improve search performance
- Enable advanced search queries

#### Version 5: User Preferences
- Add `user_preferences` table
- Store theme settings
- Save notification preferences
- Enable customization

#### Version 6: Sync Support
- Add `sync_status` column
- Track last sync timestamp
- Enable cloud backup
- Support multi-device sync

---

## Troubleshooting Migration Issues

### Issue: "Migration path not found"

**Error**:
```
java.lang.IllegalStateException: A migration from 1 to 3 was required but not found.
```

**Solution**:
- Ensure all incremental migrations exist (1→2, 2→3)
- Add missing migrations or use `fallbackToDestructiveMigration()`

### Issue: "Migration didn't properly handle column"

**Error**:
```
TableInfo{...} expected but was TableInfo{...}
```

**Solution**:
- Verify migration SQL matches expected schema
- Check column names, types, and constraints
- Compare with exported schema JSON

### Issue: "Database disk image is malformed"

**Error**:
```
android.database.sqlite.SQLiteDatabaseCorruptException
```

**Solution**:
- Database file corrupted during migration
- Restore from backup
- Implement integrity checks before migration

---

## Related Documentation

- [DATABASE.md](./DATABASE.md) - Complete schema documentation
- [QUERIES.md](./QUERIES.md) - Example queries and patterns
- [PERFORMANCE.md](./PERFORMANCE.md) - Optimization guide
- [Room Migration Documentation](https://developer.android.com/training/data-storage/room/migrating-db-versions)

---

**Last Updated**: November 14, 2025
**Current Schema Version**: 2
**Migration Strategy**: Destructive (Development) - Needs Production Implementation
