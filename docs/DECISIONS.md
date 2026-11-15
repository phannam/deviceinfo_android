# Decisions

## Current Decisions (what exists today)

### ADR-CURRENT-001: UI Uses XML Layouts With ViewBinding

- Status: Current
- Decision observed: Layouts `activity_main`, `fragment_info_list`,
  `item_info_row`, and menu `menu_main`. Lists use `RecyclerView` +
  `ListAdapter` with stable ids. Material 3 theme
  (`Theme.Material3.DayNight.NoActionBar`), `MaterialToolbar`, `TabLayout` +
  `ViewPager2`.

### ADR-CURRENT-002: Strict MVVM With Hilt

- Status: Current
- Decision observed: Fragment -> `@HiltViewModel` -> repository interface ->
  data source interface. Hilt modules in `di/` bind implementations with
  `@Binds`; `DispatcherModule` provides one IO `CoroutineDispatcher`.
  Hilt annotation processing uses KSP (`com.google.devtools.ksp`) with AGP
  built-in Kotlin.

### ADR-CURRENT-003: No Storage

- Status: Current
- Decision observed: No database, SharedPreferences, or files. The selected
  tab is kept in `SavedStateHandle` only.

### ADR-CURRENT-004: No Networking

- Status: Current
- Decision observed: There is no HTTP client and no `INTERNET` permission.

### ADR-CURRENT-005: Coroutines And StateFlow

- Status: Current
- Decision observed: Repositories expose `suspend` functions that switch to
  the injected IO dispatcher. ViewModels expose
  `StateFlow<InfoListUiState>` built with `stateIn(WhileSubscribed(5000))`;
  `MainViewModel` emits one-time effects through a `Channel`.

### ADR-CURRENT-006: Single Activity With ViewPager2 Tabs

- Status: Current
- Decision observed: `MainActivity` hosts `ViewPager2` with a
  `FragmentStateAdapter`; tab switches never touch the back stack. The
  selected tab is restored from `MainViewModel`.

### ADR-CURRENT-007: Share Through The System Share Sheet

- Status: Current
- Decision observed: `Intent.createChooser` with `ACTION_SEND` `text/plain`,
  subject and text only; no recipient, no package query, no `<queries>`.
  The report is built from `ReportSection`s and rendered by
  `ShareReportRenderer`.

### ADR-CURRENT-008: Build Toolchain

- Status: Current
- Decision observed: AGP 9.4.1, Gradle 9.9.1 wrapper, Kotlin DSL build
  scripts, `gradle/libs.versions.toml`, Java 17, minSdk 26,
  compileSdk/targetSdk 37 (`release(...)` DSL with `minorApiLevel`),
  namespace and applicationId `vn.npstudio.deviceinfo`, `buildConfig` enabled
  for `VERSION_NAME` in the share report. Release builds enable R8 through
  `optimization { enable = true }`.
- Notes: Android Studio supports AGP up to 9.4.1 at the time of writing; do
  not raise AGP beyond what the owner's Android Studio supports.

## Target Decisions (what we are moving to)

### ADR-TARGET-001: Keep XML, Add ViewBinding, Do Not Move To Compose

- Status: Accepted
- Decision: Keep XML layouts for all new and refactored UI and use ViewBinding.
  Replace `ListView`/`ArrayAdapter` with `RecyclerView`/`ListAdapter` when a
  list screen is refactored.
- Why: The UI is small and already XML-based. A toolkit rewrite adds risk
  without user value.
- Rejected alternatives: Jetpack Compose, mixed Compose/XML screens, Kotlin
  synthetics, and new `findViewById` usage for app-owned layouts.
- Migration path: Done in Phase 1.

### ADR-TARGET-002: Kotlin Only

- Status: Accepted
- Decision: All app and test code is Kotlin, using the Kotlin support
  built into AGP 9.
- Why: Kotlin is required for the target stack (coroutines, `StateFlow`, Hilt
  idioms) and is the Android default.
- Rejected alternatives: Staying on Java; mixed Java/Kotlin source.
- Migration path: Done. All Java files, including the vendored AOSP tab views,
  were converted to Kotlin.

### ADR-TARGET-003: Move To Strict MVVM

- Status: Accepted
- Decision: Activity/Fragment renders UI only; the ViewModel owns UI state and
  calls the repository only; the repository coordinates data sources; data
  sources own platform access (`Build`, `DisplayMetrics`/`WindowManager`,
  `PackageManager`).
- Why: It removes the static `AppManager`, makes values testable on the JVM,
  and lets display values refresh on configuration change.
- Rejected alternatives: MVC, MVP, keeping `AppManager` as a global service,
  and ViewModels calling platform APIs directly.
- Migration path: Done in Phase 1; `AppManager` was deleted.

### ADR-TARGET-004: Use Hilt For Dependency Injection

- Status: Accepted
- Decision: Hilt is the DI standard for new and refactored code.
- Why: It matches the owner's other NPStudio apps and supports the MVVM
  boundaries without manual factories.
- Rejected alternatives: Manual singletons/service locators, Dagger without
  Hilt, and Koin.
- Migration path: Done in Phase 1 with KSP. If KSP ever stops working with
  AGP built-in Kotlin, fall back to `com.android.legacy-kapt` as described in
  `docs/CONVENTIONS.md`.

### ADR-TARGET-005: Use Coroutines For Async Work

- Status: Accepted
- Decision: Use Kotlin coroutines. UI-triggered work starts in
  `viewModelScope`; repositories/data sources expose `suspend` functions or
  `Flow`; use an injected `CoroutineDispatcher` for off-main work.
- Why: Data collection should not run on the main thread in
  `Application.onCreate()`.
- Rejected alternatives: AsyncTask, RxJava, raw threads, `Handler`,
  `GlobalScope`, and `runBlocking`.

### ADR-TARGET-006: Use StateFlow For UI State

- Status: Accepted
- Decision: ViewModels expose read-only `StateFlow` UI state. One-time UI
  actions (toasts, launching the share chooser) use `SharedFlow` or `Channel`.
  User-visible messages emitted by ViewModels are `UiText`. Shared contracts
  (`UiEffect`, `LoadingMode`, `UiText`, `collectWithLifecycle`) live in
  `vn.npstudio.deviceinfo.common` (see `docs/MODULE_MAP.md`).
- Rejected alternatives: `LiveData`, `MutableLiveData`, `SingleLiveEvent`,
  exposing `MutableStateFlow`, and resolving Android strings inside ViewModels.

### ADR-TARGET-007: Single Activity, FragmentManager-Owned Tabs

- Status: Accepted
- Decision: Keep `MainActivity` as the only Activity for the tabs, hosted by
  `ViewPager2` + `FragmentStateAdapter`; tab switches must not grow the back
  stack. Selected tab state lives in the ViewModel. Full-screen flows such as
  the Screen Test may use a dedicated Activity (spec 001). Adopt Jetpack
  Navigation only if the app gains real multi-screen navigation.
- Why: Tabs do not justify a NavGraph.
- Rejected alternatives: Adding a custom router; one Activity per tab.

### ADR-TARGET-008: Keep Minimum SDK 26

- Status: Accepted
- Decision: Keep minSdk 26 from `gradle/libs.versions.toml`.
- Why: It matches the owner's other apps and removes pre-Oreo branches.
- Migration path: Done; no pre-26 branches remain.

### ADR-TARGET-009: No Storage Or Networking Unless A Feature Needs It

- Status: Accepted
- Decision: The app stays offline and stateless. If a feature needs persisted
  settings, use SharedPreferences (or DataStore if the owner approves) behind a
  local data source. If a feature needs HTTP, use Retrofit with `suspend`
  functions behind a remote data source and add `docs/API_CONTRACTS.md` in the
  same task.
- Rejected alternatives: Room for settings-only data, Volley, callback-based
  Retrofit, and RxJava adapters.

## Do Not Suggest

- No Jetpack Compose
- No RxJava
- No AsyncTask
- No Volley
- No Java code

## Gaps & Recommendations

- Release signing, versioning policy, and Play publishing are not set up in
  this repository.
