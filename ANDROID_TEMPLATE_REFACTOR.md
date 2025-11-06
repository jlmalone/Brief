# Brief App Refactor - Android Template Architecture

**Based on**: [jeffdcamp/android-template](https://github.com/jeffdcamp/android-template)
**Date**: November 6, 2025
**Status**: Planning → Implementation

---

## Architecture Overview

This document outlines the complete refactoring of Brief using the proven architecture from jeffdcamp's android-template project.

### Key Architectural Principles

1. **Feature-based organization** - Group by feature, not technical layer
2. **Compose-first UI** - 100% Jetpack Compose, zero XML layouts
3. **Modular DI** - Separate Hilt modules by concern
4. **ViewModel per screen** - Each screen has dedicated ViewModel
5. **Reactive data flow** - StateFlow/Flow for all data streams
6. **Repository pattern** - Abstract data sources
7. **Material3 design** - Modern Material You theming

---

## Package Structure (Target)

```
com/techventus/wikipedianews/
├── analytics/              # Event tracking (future)
│   └── AnalyticsManager.kt
│
├── inject/                 # Hilt DI modules
│   ├── AppModule.kt
│   ├── CoroutinesModule.kt
│   ├── DatabaseModule.kt
│   └── NetworkModule.kt
│
├── model/                  # Data layer
│   ├── config/            # App configuration
│   │   └── AppPreferences.kt
│   ├── database/          # Room database
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   └── NewsDao.kt
│   │   └── entity/
│   │       └── NewsArticleEntity.kt
│   ├── datastore/         # DataStore preferences
│   │   └── PreferencesDataSource.kt
│   ├── domain/            # Domain models
│   │   ├── NewsArticle.kt
│   │   └── NewsSection.kt
│   ├── repository/        # Repository interfaces & implementations
│   │   ├── NewsRepository.kt
│   │   └── NewsRepositoryImpl.kt
│   ├── datasource/        # Data sources
│   │   ├── NewsRemoteDataSource.kt
│   │   └── NewsLocalDataSource.kt
│   └── parser/            # HTML parsing
│       └── WikipediaNewsParser.kt
│
├── startup/               # App initialization
│   └── Initializers.kt
│
├── ui/                    # UI layer
│   ├── compose/           # Compose screens
│   │   ├── screen/
│   │   │   ├── main/
│   │   │   │   ├── MainScreen.kt
│   │   │   │   └── MainViewModel.kt
│   │   │   ├── news/
│   │   │   │   ├── NewsScreen.kt
│   │   │   │   └── NewsViewModel.kt
│   │   │   └── settings/
│   │   │       ├── SettingsScreen.kt
│   │   │       └── SettingsViewModel.kt
│   │   └── component/      # Reusable components
│   │       ├── NewsArticleCard.kt
│   │       ├── SectionHeader.kt
│   │       ├── LoadingIndicator.kt
│   │       └── ErrorView.kt
│   ├── navigation/         # Navigation
│   │   ├── BriefNavHost.kt
│   │   ├── NavigationDestinations.kt
│   │   └── NavigationExtensions.kt
│   ├── theme/              # Material3 theme
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Shapes.kt
│   └── MainActivity.kt     # Main Activity
│
├── util/                   # Utilities
│   ├── DateTimeUtil.kt
│   ├── Extensions.kt
│   └── NetworkMonitor.kt
│
├── work/                   # Background work
│   └── NewsSyncWorker.kt
│
└── App.kt                  # Application class

```

---

## Component Mapping (Old → New)

### Application Layer
| Old | New | Changes |
|-----|-----|---------|
| `WikiApplication.kt` | `App.kt` | Minimal @HiltAndroidApp, no singleton |
| Logger.kt | Timber | Use standard Timber library |
| Toaster.kt | SnackbarManager | Compose-based snackbars |

### Data Layer
| Old | New | Changes |
|-----|-----|---------|
| N/A | `model/database/` | Add Room database |
| N/A | `model/repository/` | Add Repository pattern |
| N/A | `model/datasource/` | Separate remote/local sources |
| `WikiData.kt` | `model/domain/NewsArticle.kt` | Proper domain model |
| HTML parsing in Fragment | `model/parser/WikipediaNewsParser.kt` | Dedicated parser class |
| `PreferencesManager.kt` | `model/datastore/PreferencesDataSource.kt` | Migrate to DataStore |

### UI Layer
| Old | New | Changes |
|-----|-----|---------|
| `WikiActivity.kt` | `ui/MainActivity.kt` | ComponentActivity with Compose |
| `WikiNewsFragment.kt` | `ui/compose/screen/news/NewsScreen.kt` | Compose screen |
| N/A | `ui/compose/screen/news/NewsViewModel.kt` | Add ViewModel |
| `WikiAdapter.kt` | `ui/compose/component/NewsArticleCard.kt` | Composable |
| `LoadingViewFlipper.kt` | `ui/compose/component/LoadingIndicator.kt` | Composable |
| XML layouts | Delete all | 100% Compose |

### DI Layer
| Old | New | Changes |
|-----|-----|---------|
| `di/AppModule.kt` | `inject/AppModule.kt` | Rename directory |
| `di/NetworkModule.kt` | `inject/NetworkModule.kt` | Rename, enhance |
| N/A | `inject/CoroutinesModule.kt` | Add coroutine dispatchers |
| N/A | `inject/DatabaseModule.kt` | Add Room dependencies |

---

## Implementation Plan

### Phase 1: Foundation & Structure (1-2 hours)
- [ ] Create new branch `refactor/android-template-architecture`
- [ ] Create new package structure
- [ ] Update App.kt to minimal @HiltAndroidApp
- [ ] Move DI modules to `inject/` package
- [ ] Create placeholder files for all layers

### Phase 2: Data Layer (2-3 hours)
- [ ] Create domain models (`NewsArticle`, `NewsSection`)
- [ ] Create Room entities and DAOs
- [ ] Migrate `WikipediaNewsParser` from Fragment
- [ ] Create `NewsRemoteDataSource` with Retrofit
- [ ] Create `NewsLocalDataSource` with Room
- [ ] Implement `NewsRepository` with offline-first logic
- [ ] Migrate PreferencesManager to DataStore

### Phase 3: UI Layer - Compose (3-4 hours)
- [ ] Create Material3 theme (colors, typography, shapes)
- [ ] Create `NewsViewModel` with StateFlow
- [ ] Create `NewsScreen` composable
- [ ] Create reusable components (ArticleCard, Header, etc.)
- [ ] Create `BriefNavHost` with Navigation Compose
- [ ] Update `MainActivity` to use Compose
- [ ] Delete all XML layouts
- [ ] Delete old Fragment/Adapter classes

### Phase 4: Background Work & Polish (1-2 hours)
- [ ] Create `NewsSyncWorker` for background updates
- [ ] Add proper error handling
- [ ] Add loading states
- [ ] Add pull-to-refresh
- [ ] Add offline indicator
- [ ] Migrate to Timber logging fully

### Phase 5: Testing & Documentation (1-2 hours)
- [ ] Add ViewModel unit tests
- [ ] Add Repository tests
- [ ] Add Parser tests
- [ ] Update ARCHITECTURAL_REVIEW.md
- [ ] Update ROADMAP.md
- [ ] Add code documentation

**Total Estimated Time**: 8-13 hours

---

## Detailed Component Specifications

### App.kt
```kotlin
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // App initialization handled by startup library
    }
}
```

### MainActivity.kt
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BriefTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BriefNavHost()
                }
            }
        }
    }
}
```

### NewsViewModel.kt
```kotlin
@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        observeNews()
    }

    private fun observeNews() {
        viewModelScope.launch {
            repository.getNewsFlow()
                .map { NewsUiState.Success(it) }
                .catch { NewsUiState.Error(it.message ?: "Unknown error") }
                .collect { _uiState.value = it }
        }
    }

    fun refresh() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = NewsUiState.Loading
            repository.refreshNews()
                .onFailure { _uiState.value = NewsUiState.Error(it.message ?: "Failed") }
        }
    }
}

sealed interface NewsUiState {
    data object Loading : NewsUiState
    data class Success(val sections: List<NewsSection>) : NewsUiState
    data class Error(val message: String) : NewsUiState
}
```

### NewsRepository.kt
```kotlin
interface NewsRepository {
    fun getNewsFlow(): Flow<List<NewsSection>>
    suspend fun refreshNews(): Result<Unit>
}

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NewsRemoteDataSource,
    private val localDataSource: NewsLocalDataSource,
    private val parser: WikipediaNewsParser,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NewsRepository {

    override fun getNewsFlow(): Flow<List<NewsSection>> =
        localDataSource.observeNews()
            .map { entities -> entities.toSections() }
            .flowOn(ioDispatcher)

    override suspend fun refreshNews(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val html = remoteDataSource.fetchCurrentEvents()
            val sections = parser.parse(html)
            localDataSource.saveNews(sections.toEntities())
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh news")
            Result.failure(e)
        }
    }
}
```

### NewsScreen.kt
```kotlin
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NewsScreenContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onArticleClick = { /* navigate */ }
    )
}

@Composable
private fun NewsScreenContent(
    uiState: NewsUiState,
    onRefresh: () -> Unit,
    onArticleClick: (NewsArticle) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState is NewsUiState.Loading,
        onRefresh = onRefresh
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        when (uiState) {
            is NewsUiState.Loading -> LoadingIndicator()
            is NewsUiState.Success -> NewsContent(uiState.sections, onArticleClick)
            is NewsUiState.Error -> ErrorView(uiState.message, onRefresh)
        }

        PullRefreshIndicator(
            refreshing = uiState is NewsUiState.Loading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
```

---

## Key Dependencies (from android-template)

```kotlin
// Already added in libs.versions.toml:
✅ Jetpack Compose + Material3
✅ Hilt
✅ Room
✅ Retrofit/OkHttp
✅ Coroutines + Flow
✅ WorkManager
✅ Timber
✅ DataStore

// Consider adding:
- Kermit logging (alternative to Timber)
- Ktor Client (alternative to Retrofit)
- kotlinx-datetime (for date handling)
- Kover (code coverage)
```

---

## Migration Strategy

### Strangler Fig Pattern
1. Create new architecture alongside old
2. Gradually migrate features
3. Keep app functional throughout
4. Delete old code when new is proven

### Feature Flags
```kotlin
object FeatureFlags {
    val useNewArchitecture = true  // Enable new architecture
}
```

---

## Testing Strategy

### Unit Tests
- ViewModel tests (MockK + Turbine)
- Repository tests
- Parser tests
- UseCase tests (if added)

### UI Tests
- Compose UI tests
- Screenshot tests
- Navigation tests

### Integration Tests
- Database migration tests
- Repository integration tests

---

## Success Criteria

✅ Zero XML layouts
✅ All screens in Compose
✅ ViewModels for all screens
✅ Repository pattern implemented
✅ Room database working
✅ Offline-first functional
✅ Hilt DI throughout
✅ Material3 theming
✅ 80%+ code coverage
✅ All tests passing
✅ Zero memory leaks

---

## Risk Mitigation

1. **Create separate branch** - Don't modify main work
2. **Incremental commits** - Commit after each layer
3. **Keep old code** - Delete only when new works
4. **Test thoroughly** - Unit + UI tests for each component
5. **Document decisions** - Update docs as we go

---

## Next Steps

1. Create new branch
2. Start with Phase 1 (Foundation)
3. Commit frequently
4. Test each layer before moving to next
5. Update documentation

**Let's begin!** 🚀
