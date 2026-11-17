# Architecture

Based on the source in `app/` after completing
`docs/requirements/001-device-diagnostics.md`. The app uses XML layouts;
Compose is not part of the target architecture.

## Current Architecture

**What the app does**

- Device tab: hero card (device name, brand, model), hardware (`Build`,
  device name, bootloader state, Samsung vendor properties from `getprop`
  shown only when set), identifiers (Android ID, GSF ID,
  advertising ID, serial, fingerprint, IMEI, SIM serial, subscriber ID, MAC
  addresses, USB debugging, developer options), telephony
  (`TelephonyManager`), a hardware feature checklist (`PackageManager`), and
  video/audio codecs (`MediaCodecList`). Values guarded by
  `READ_PHONE_STATE` show "Tap to grant"; values Android blocks show
  "Restricted". Identifiers are excluded from the share report.
- System tab: hero card (Android version, codename, release date), build
  details, security (root check, SELinux, partitions, Treble, VNDK, system
  features list), kernel and runtime, status (uptime ticking every second,
  boot count, language, time zone), and Widevine DRM (`MediaDrm`). Build
  properties come from `getprop` (parsed by `SystemPropertiesParser`).
- CPU tab: SoC, ABIs, core count, clusters, governor, thermal status and
  headroom from `Build`, `PowerManager`, `/proc/cpuinfo`, and
  `/sys/devices/system/cpu`; per-core current frequency polled every second
  while the tab is visible.
- Battery tab: sticky `ACTION_BATTERY_CHANGED` extras (live receiver),
  `BatteryManager` properties and `PowerManager.isPowerSaveMode` polled every
  2 s while the tab is visible.
- Display tab: physical resolution, refresh rate and supported rates, HDR
  types, wide color gamut (display mode / `Display`), density bucket, dpi,
  estimated size, font scale, plus app window size and cutout from a
  `WindowSnapshot` measured by the UI.
- Memory tab: `ActivityManager.MemoryInfo` polled every 2 s while visible;
  internal storage from `StorageStatsManager` (default volume) on each
  resume.
- Screen Test tab: starts `ScreenTestActivity` (immersive, full brightness,
  cutout area covered) that draws 9 patterns with `ScreenTestPatternView`;
  the answers are kept in memory by `ScreenTestResultRepository`.
- Shares a plain-text report (Device + System + CPU + Battery + Display + Memory + Screen Test sections) through
  the system share sheet.

**Pattern: strict MVVM with Hilt**

```text
CpuFragment
  -> CpuViewModel   flow { getCpuInfo(); emitAll(observeFrequencies().map(rows)) }
  -> CpuRepository  getCpuInfo() (suspend) + observeFrequencies() (1 s ticker,
                    flowOn IO; emits [] and completes when unreadable)
  -> CpuInfoDataSource (Build, PowerManager) + SysFsReader (/proc, /sys)
  -> CpuSysFsParser (pure) and CpuRowMapper (pure)

BatteryFragment
  -> BatteryViewModel   observe().map(rows)
  -> BatteryRepository  combine(batteryChanges() callbackFlow receiver,
                        2 s properties ticker) -> BatteryParser (pure)
                        -> distinctUntilChanged, flowOn IO
  -> BatteryDataSource  (ContextCompat.registerReceiver NOT_EXPORTED,
                        BatteryManager, PowerManager)
  -> BatteryRowMapper (pure)

ScreenTestFragment -> ScreenTestResultViewModel  result.map(rows)
ScreenTestActivity -> ScreenTestViewModel  pattern index, 2 s hint timer,
                      dialog answers; Save -> ScreenTestResultRepository.save()
ScreenTestResultRepository  @Singleton in-memory StateFlow<ScreenTestResult?>

MemoryFragment  onResume -> MemoryViewModel.onResumed()
  -> MemoryViewModel   combine(observeRam(), resumes.map { getStorageInfo() })
  -> MemoryRepository  observeRam() (2 s ticker, distinctUntilChanged, flowOn IO)
                       + getStorageInfo() (suspend, IO)
  -> MemoryDataSource  (ActivityManager, StorageStatsManager)
  -> MemoryRowMapper (pure)

DisplayFragment  doOnLayout -> WindowSnapshotReader (androidx.window, root insets)
  -> DisplayViewModel.onWindowMeasured(snapshot)
       SharedFlow(replay 1).map { DisplayRowMapper.map(getDisplayInfo(), it) }
  -> DisplayRepository -> DisplayDataSource (DisplayManager, displayMetrics)

DeviceFragment  onResume -> DeviceViewModel.onResumed(); permission granted -> refresh()
  -> DeviceViewModel   reloads.mapLatest { DeviceRowMapper.sections(getDeviceInfo()) }
                       (reloads on resume only if READ_PHONE_STATE changed)
  -> DeviceInfoRepository  (suspend, IO) -> BuildInfo, DeviceIdentifier,
                           Telephony, Feature, Media data sources

SystemFragment
  -> SystemViewModel   combine(flow { getSystemInfo() }, observeUptime())
                       -> SystemRowMapper.hero(...) + sections(...)
  -> SystemInfoRepository  getSystemInfo() (suspend, IO) + observeUptime()
                           (1 s ticker) -> BuildInfo, Platform, Feature,
                           Runtime, Media data sources

Every list tab -> List<InfoSection> -> InfoListItem.from(state) -> InfoListAdapter

MainActivity share menu
  -> MainViewModel.onShareClicked()
     -> repositories + row mappers -> MainUiEffect.Share(List<ReportSection>)
  -> MainActivity resolves UiText, ShareReportRenderer.render(...)
  -> Intent.createChooser(ACTION_SEND text/plain)
```

**Layers**

- Presentation: `MainActivity` (toolbar, `TabLayout` + `ViewPager2` via
  `TabLayoutMediator`, share chooser), `InfoListFragment` base with
  one fragment per tab, `InfoListAdapter` (`ListAdapter`, hero / header / row /
  feature-chip view types), `ListEntriesBottomSheet` for long lists.
- ViewModels: `MainViewModel` (visible tabs from `AppConfigRepository.tabs`,
  selected tab in `SavedStateHandle`, share effect through a `Channel`), `DeviceViewModel`, `DisplayViewModel`. No
  `Context`; user-visible text is `UiText`.
- Domain: `DeviceInfo`, `DisplayInfo` models and repository interfaces.
- Data: repository implementations switch to the injected
  `CoroutineDispatcher` (IO) and call data sources. Data sources are
  interfaces with `Impl` classes bound in `di/`.
- Theme: `ThemeApplier` (installed in `DeviceInfoApplication`) sets the
  AppCompat night mode from `AppConfigRepository` and applies the primary
  and accent theme overlays to each Activity before it inflates views. It
  tracks the `ThemeColors` applied per Activity and recreates stale ones
  (immediately if started, otherwise on their next start).
  Colors and tabs are stored in SharedPreferences (`app_config`).
- List UI: each `InfoSection` is a titled card (primary-colored icon) with
  left-right rows (label ~42%, medium-weight value) and dividers; the card
  shape comes from `RowPosition`. Feature checklists are chips (filled =
  supported, outlined = not supported). Rows may carry a `RowAction`:
  `ShowList` opens `ListEntriesBottomSheet`, `RequestPermission` asks for a
  runtime permission in `InfoListFragment` (Snackbar to App info when denied
  permanently). Long-press copies a value. Lists and the Screen Test tab use
  `View.applySystemBarPadding()` for insets.
- Formatting: `ValueFormatter` and per-tab row mappers are pure Kotlin and
  unit tested. Labels are `UiText` (usually a string resource).

**State and navigation**

- Each list tab exposes `StateFlow<InfoListUiState(loadingMode, sections, hero)>`.
  Values are re-read when a tab gets a new subscriber after more than 5 s
  without one (for example, returning from background), so uptime refreshes.
- Single Activity. `FragmentStateAdapter` owns tab fragments; tab switches do
  not touch the back stack, so Back exits the app. The toolbar gear opens
  `ConfigActivity` (Settings page); dark mode changes recreate activities
  through AppCompat, and the temperature unit is read by `BatteryViewModel`
  (combined flow) and `MainViewModel.onShareClicked`. The selected tab is saved
  in `MainViewModel` (`SavedStateHandle`) and restored on recreation.
- Tab Manager: `TabManagerBottomSheet` (`BottomSheetDialogFragment`, opened
  from the main overflow menu and the Settings page) edits a draft in
  `TabManagerViewModel`; Save calls `AppConfigRepository.saveTabs` and emits
  `TabManagerUiEffect.Dismiss`. `MainActivity` collects
  `MainViewModel.visibleTabs` and updates `MainTabAdapter` (stable ids =
  `MainTab.ordinal`) and the `TabLayoutMediator` (`autoRefresh`) without
  recreating; the selected tab stays if visible, otherwise the first visible
  tab is selected. Fragments of tabs that stay visible keep their state.
- Edge-to-edge: `enableEdgeToEdge()`; the app bar pads for top/side insets
  and each list pads for bottom/side insets (`clipToPadding=false`).
- The activity is locked to portrait.

## Architecture Rules (what we are moving toward)

- Pattern: strict MVVM, single `:app` module, packages by feature then layer
  (see `docs/MODULE_MAP.md`).
- UI stack: XML + ViewBinding. No Compose.
- Activity/Fragment: UI only. Inflate bindings, collect state with
  `collectWithLifecycle(...)`, render state, forward user actions to the
  ViewModel, and perform effects such as launching the share chooser.
- ViewModel: holds UI state and calls repositories and pure mappers only. No
  `Context`, `Build`, `WindowManager`, `PackageManager`, or Android string
  resolution. Emits `UiText` for user-visible text.
- Repository: one per domain area, returns domain models through `suspend`
  functions (static data) or `Flow` (live data).
- Data sources: platform-only, no product rules, never throw for missing
  values (return null instead).
- Live tabs (CPU, Battery, Memory) collect at `Lifecycle.State.RESUMED` and use
  `stateIn(WhileSubscribed(5000))` so polling and receivers stop when the tab
  is not visible.
- Async: `viewModelScope` plus an injected dispatcher; no work in
  `Application.onCreate()` except Hilt setup.
- DI: Hilt constructor injection; no static singletons.

## Gaps & Recommendations

### 1. Screen Test results are not persisted

- Current: `ScreenTestResultRepository` keeps the last result in memory, so
  it is lost when the process dies (spec 001 decision).
- Target: Persist with SharedPreferences only if the owner asks for it
  (ADR-TARGET-009).
- Priority: Low

