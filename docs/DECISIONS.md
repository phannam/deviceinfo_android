# Decisions

## Current Decisions (what exists today)

### ADR-CURRENT-001: UI Uses XML Layouts With ViewBinding

- Status: Current
- Context: The app has four layouts (`activity_main`, `fragment_listitems`,
  `item_info`, `view_tab`) and one menu (`menu_main`).
- Decision observed: UI is XML-based and uses ViewBinding (`viewBinding = true`).
  Lists use `ListView` with an `ArrayAdapter` that caches the row binding in
  the view tag. Only the unused vendored `SlidingTabLayout` still calls
  `findViewById` with caller-provided ids.
- Notes: Jetpack Compose is not part of the app.

### ADR-CURRENT-002: Architecture Is Activity/Fragment Plus A Static Singleton

- Status: Current
- Context: `MainActivity` (via `TabActivity` and `BaseActivity`) hosts
  `DeviceFragment` and `DisplayFragment` (via `ListFragment` and
  `BaseFragment`).
- Decision observed: There is no ViewModel, repository, or data source.
  `AppManager` is a static singleton created in `DeviceApplication.onCreate()`.
  It reads `Build` and `DisplayMetrics` values once and exposes them as
  `List<Item>`. Fragments and the share action read from it directly.
- Notes: Best classified as plain Activity/Fragment code with a global
  service, not MVC, MVP, or MVVM.

### ADR-CURRENT-003: No Storage

- Status: Current
- Decision observed: The app has no database, SharedPreferences, files, or
  backup. All values are recomputed from the platform on each process start.

### ADR-CURRENT-004: No Networking

- Status: Current
- Decision observed: There is no HTTP client and no `INTERNET` permission.

### ADR-CURRENT-005: No Async

- Status: Current
- Decision observed: All data collection runs synchronously on the main thread
  in `Application.onCreate()`. There are no coroutines, threads, or callbacks
  for data work.

### ADR-CURRENT-006: No Dependency Injection

- Status: Current
- Decision observed: Dependencies are reached through
  `AppManager.getInstance()` and direct construction. No DI framework is
  present.

### ADR-CURRENT-007: Navigation Is A Single Activity With Manual Tabs

- Status: Current
- Decision observed: `TabActivity.selectTab(...)` replaces the fragment in
  `R.id.fragment_container` and adds every tab change to the back stack. Tabs
  are rendered by the custom `SlidingFragmentTab` view.

### ADR-CURRENT-008: Build And Language Are Modernized; Architecture Is Not

- Status: Current
- Decision observed: AGP 9.4.1, Gradle 9.9.1 wrapper, Kotlin DSL build
  scripts, `gradle/libs.versions.toml`, AndroidX (`appcompat`, `fragment`,
  `viewpager`, `annotation`), Java 17, minSdk 26, compileSdk/targetSdk 37
  (`compileSdk` uses the `release(...)` DSL with `minorApiLevel`), namespace
  and applicationId `vn.npstudio.deviceinfo`. All source is Kotlin, converted 1:1 from
  Java with behavior preserved; the architecture is unchanged.
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
- Migration path: ViewBinding is enabled and used by every screen, adapter, and
  app-owned custom view. Remaining work is `ListView` to `RecyclerView`.

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
- Migration path: Add data sources and a repository first, then a ViewModel per
  screen, then switch the fragments, then delete `AppManager`.

### ADR-TARGET-004: Use Hilt For Dependency Injection

- Status: Accepted
- Decision: Hilt is the DI standard for new and refactored code.
- Why: It matches the owner's other NPStudio apps and supports the MVVM
  boundaries without manual factories.
- Rejected alternatives: Manual singletons/service locators, Dagger without
  Hilt, and Koin.
- Migration path: Add the Hilt plugin and `@HiltAndroidApp` on the Application
  in the first MVVM task. Use KSP for Hilt's annotation processing if supported
  by the AGP/Kotlin toolchain at that time; otherwise use
  `com.android.legacy-kapt` as described in `docs/CONVENTIONS.md`.

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
- Decision: Keep `MainActivity` as the only Activity. Tab destinations are
  owned by `FragmentManager` with stable tags; tab switches must not grow the
  back stack. Selected tab state lives in the ViewModel. Adopt Jetpack
  Navigation only if the app gains more than the two tab destinations.
- Why: Two tabs do not justify a NavGraph, but the current back-stack and
  recreation behavior is incorrect.
- Rejected alternatives: Adding a custom router; one Activity per tab.

### ADR-TARGET-008: Keep Minimum SDK 26

- Status: Accepted
- Decision: Keep minSdk 26 from `gradle/libs.versions.toml`.
- Why: It matches the owner's other apps and removes pre-Oreo branches.
- Migration path: Remove pre-26 `Build.VERSION.SDK_INT` checks (for example
  in `SlidingFragmentTab.createDefaultTabView`) when those files are touched.

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

- Decide whether to keep the vendored `SlidingFragmentTab` tab strip or replace
  it with Material `TabLayout` (adds the Material Components dependency) or a
  `BottomNavigationView`. Recommended: Material `TabLayout` when the tab host is
  refactored; it removes five vendored files.
- Decide whether display values should reflect the current window (Window
  Metrics, changes with rotation/multi-window) or the physical display. Today
  the app reports `getDefaultDisplay().getMetrics(...)` once at startup, and
  the activity is locked to portrait.
- Decide the share target: keep email-only (with the owner's address
  prefilled) or use a generic `ACTION_SEND` share sheet. Recommended: generic
  share sheet without a hardcoded recipient.
- Decide whether to enable R8 (`isMinifyEnabled = true`) for release; it is
  currently disabled.
- Release signing, versioning policy, and Play publishing are not set up in
  this repository.
