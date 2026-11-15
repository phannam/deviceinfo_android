# Architecture

Based on the source in `app/` as of 2026-11-15. The app uses XML layouts;
Compose is not part of the target architecture.

## Current Architecture

**What the app does**

- Shows general device information read from `android.os.Build`.
- Shows display information read from `DisplayMetrics`.
- Shares all values as plain text through an email app.

**Folder structure observed**

- `app/`: the only Gradle module.
- `app/src/main/java/vn/npstudio/deviceinfo/activities/`: `BaseActivity`
  (empty `AppCompatActivity`), `TabActivity` (abstract tab host),
  `MainActivity` (launcher, tabs, share menu).
- `.../fragments/`: `BaseFragment` (empty), `ListFragment` (abstract list
  screen), `DeviceFragment`, `DisplayFragment`.
- `.../utils/`: `DeviceApplication`, `AppManager` (singleton data holder),
  `Item` (name/value model), `ItemListAdapter`, `Constant` (tab type ids).
- `.../views/`: `SlidingFragmentTab`, `SlidingTab`, `SlidingTabRenderer`
  (app-owned tab strip), and `SlidingTabLayout`, `SlidingTabStrip` (adapted
  AOSP Apache-2.0 sample code).
- `app/src/main/res/`: four layouts, one menu, tab icons in `mipmap-xhdpi`,
  a share icon, and launcher icons.

**What pattern is actually being used**

Plain Activity/Fragment code backed by a static singleton:

```text
DeviceApplication.onCreate()
  -> AppManager.create(context)
     -> reads Build.* and WindowManager.getDefaultDisplay().getMetrics(...)
     -> stores List<Item> deviceInfoList / displayInfoList

DeviceFragment / DisplayFragment (ListFragment)
  -> AppManager.getInstance().xxxInfoList
  -> ItemListAdapter (ArrayAdapter<Item>) -> ListView

MainActivity share menu
  -> PackageManager.queryIntentActivities(mailto: intent)
  -> AppManager.getInstance().getAllInfos()
  -> startActivity(ACTION_SEND_MULTIPLE)
```

**How each layer is currently defined**

- Presentation: `MainActivity`, `TabActivity`, fragments, `ItemListAdapter`,
  and the tab views. `MainActivity` also builds tab models, sets the toolbar
  title and color, resolves email apps, and builds share intents.
- Data: `AppManager` reads platform values, formats them as strings, builds
  labels, and formats the share text. It holds the application `Context` in a
  static field.
- Model: `Item` (Kotlin data class with `name` and `value`).
- There is no ViewModel, repository, data source, domain, or DI layer.

**How data currently flows**

- All values are read once, synchronously, on the main thread when the process
  starts. They are never refreshed while the process lives.
- Display values come from `getDefaultDisplay().getMetrics(...)`, which
  reports app window metrics at startup; the activity is locked to portrait.
- Device/display row labels and share headings are hardcoded English strings.

**How state is currently managed**

- Selected tab: `TabActivity.selectedTab` and `SlidingTab.isSelected`.
  It is not saved; recreation resets to the Device tab.
- List data: read from `AppManager` in `ListFragment.onViewCreated` and passed
  to a new `ItemListAdapter`.
- No `ViewModel`, `StateFlow`, `LiveData`, or saved state is used.

**How navigation currently works**

- Single Activity. `TabActivity.selectTab(...)` calls `replace(...)` on
  `R.id.fragment_container`, adds the transaction to the back stack, and
  manually calls `onPause()`/`onStop()` on the previous fragment.
- The share action launches an external app.

## Architecture Rules (what we are moving toward)

- Pattern: strict MVVM, single `:app` module, packages by feature then layer
  (see `docs/MODULE_MAP.md`).
- UI stack: XML + ViewBinding. No Compose.
- Activity/Fragment: UI only. Inflate bindings, collect state with
  `collectWithLifecycle(...)`, render state, forward user actions to the
  ViewModel, and perform effects such as launching the share chooser.
- ViewModel: holds UI state and calls the repository only. No `Context`,
  `Build`, `WindowManager`, `PackageManager`, or Android string resolution.
  Emits `UiText` for user-visible messages.
- Repository: `DeviceInfoRepository` coordinates the data sources and returns
  domain models (`DeviceInfo`, `DisplayInfo`).
- Data sources: platform-only, no product rules.
  - `BuildInfoDataSource` reads `android.os.Build`.
  - `DisplayInfoDataSource` reads window/display metrics with non-deprecated
    APIs.
- Formatting: mapping domain models to labeled rows and to share text is a
  pure Kotlin mapper/formatter that is unit tested. Labels are string
  resource ids, not hardcoded text.
- Navigation: single Activity. Tab destinations are owned by
  `FragmentManager` with stable tags; tab switches are not added to the back
  stack; the selected tab is held in the main ViewModel state.
- State: one immutable UI state per screen through read-only `StateFlow`;
  one-time actions through `SharedFlow`/`Channel`.
- Async: `viewModelScope` plus an injected dispatcher; no work in
  `Application.onCreate()` except Hilt setup.
- DI: Hilt constructor injection; no static singletons.

Target flow:

```text
DeviceFragment / DisplayFragment
  -> DeviceInfoViewModel / DisplayInfoViewModel (StateFlow<InfoListUiState>)
  -> DeviceInfoRepository
  -> BuildInfoDataSource / DisplayInfoDataSource
  -> InfoRowMapper -> List<InfoRowUiModel>

MainActivity (share)
  -> MainViewModel.onShareClicked()
  -> DeviceInfoRepository + ShareTextFormatter
  -> MainUiEffect.Share(text) -> Activity launches the share chooser
```

## Gaps & Recommendations

### 1. Static `AppManager` singleton owns all data

- Current: `AppManager` holds a static `Context`, reads platform values, builds
  labels, and formats share text. `getInstance()` returns `null` if called
  before `DeviceApplication.onCreate()`.
- Target: Hilt-provided data sources + repository + formatter.
- How to fix: Add `BuildInfoDataSource`, `DisplayInfoDataSource`, and
  `DeviceInfoRepository`; move formatting into a mapper; switch callers; then
  delete `AppManager` and `DeviceApplication`'s manual initialization.
- Priority: High

### 2. Logic in `MainActivity`

- Current: The Activity resolves email apps, builds intents, and creates tab
  models with hardcoded titles.
- Target: The ViewModel decides what to share and emits an effect; the
  Activity only launches the intent.
- How to fix: Add `MainViewModel` with share and tab-selection actions.
- Priority: High

### 3. Tab back stack and recreation

- Current: Every tab switch is added to the back stack, so Back walks through
  tab history and can leave an empty container. `initTabs()` runs again on
  recreation and pushes another `replace`. The previous fragment's lifecycle
  methods are called manually.
- Target: FragmentManager-owned tab fragments with stable tags, no back-stack
  entries for tab switches, and selected tab restored from ViewModel state.
- How to fix: Rewrite `TabActivity`/`MainActivity` tab handling when the main
  screen is refactored. Consider Material `TabLayout` to replace the vendored
  tab views.
- Priority: High

### 4. Share action breaks on Android 11+ and uses the wrong extras

- Current: `queryIntentActivities` for a `mailto:` intent returns nothing on
  API 30+ without a `<queries>` element in the manifest, so the app shows
  "No email client!" on modern devices. `EXTRA_EMAIL` is passed as a `String`
  instead of `String[]`, the `ACTION_SEND_MULTIPLE` action is used for a single
  text payload, and the owner's email address is hardcoded.
- Target: A standard `ACTION_SEND` text share through
  `Intent.createChooser(...)` with no package query and no hardcoded
  recipient (pending owner decision in `docs/DECISIONS.md`).
- Priority: High

### 5. Data read once on the main thread at startup

- Current: Values are computed in `Application.onCreate()` and never
  refreshed.
- Target: Load in `viewModelScope` when the screen starts; display values
  refresh on configuration change.
- Priority: Medium

### 6. Deprecated display APIs

- Current: `getDefaultDisplay().getMetrics(...)` and `scaledDensity`.
- Target: `WindowMetricsCalculator` (androidx.window) or
  `WindowManager.currentWindowMetrics` with a pre-30 fallback, and font scale
  from `Configuration`.
- Priority: Medium

### 7. No testing seams

- Current: Static singleton and direct platform calls cannot be tested on the
  JVM.
- Target: Interfaces for data sources so repository, mapper, and ViewModels
  can be tested with fakes.
- Priority: Medium
