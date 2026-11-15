# Architecture

Based on the source in `app/` after Phase 4 of
`docs/requirements/001-device-diagnostics.md`. The app uses XML layouts;
Compose is not part of the target architecture.

## Current Architecture

**What the app does**

- Device tab: build and OS information from `android.os.Build`,
  `System.getProperty`, and `SystemClock`.
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
- Shares a plain-text report (Device + CPU + Battery + Display sections) through the system
  share sheet.

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

DisplayFragment  doOnLayout -> WindowSnapshotReader (androidx.window, root insets)
  -> DisplayViewModel.onWindowMeasured(snapshot)
       SharedFlow(replay 1).map { DisplayRowMapper.map(getDisplayInfo(), it) }
  -> DisplayRepository -> DisplayDataSource (DisplayManager, displayMetrics)

DeviceFragment / DisplayFragment (InfoListFragment, collect at RESUMED)
  -> DeviceViewModel / DisplayViewModel   StateFlow<InfoListUiState>
       (flow { repository.get() }.stateIn(WhileSubscribed(5000)))
  -> DeviceInfoRepository / DisplayRepository      (suspend, IO dispatcher)
  -> BuildInfoDataSource / DisplayDataSource        (platform only)
  -> DeviceRowMapper / DisplayRowMapper (pure) -> List<InfoRowUiModel>

MainActivity share menu
  -> MainViewModel.onShareClicked()
     -> repositories + row mappers -> MainUiEffect.Share(List<ReportSection>)
  -> MainActivity resolves UiText, ShareReportRenderer.render(...)
  -> Intent.createChooser(ACTION_SEND text/plain)
```

**Layers**

- Presentation: `MainActivity` (toolbar, `TabLayout` + `ViewPager2` via
  `TabLayoutMediator`, share chooser), `InfoListFragment` base with
  `DeviceFragment`/`DisplayFragment`, `InfoRowAdapter` (`ListAdapter`).
- ViewModels: `MainViewModel` (selected tab in `SavedStateHandle`, share
  effect through a `Channel`), `DeviceViewModel`, `DisplayViewModel`. No
  `Context`; user-visible text is `UiText`.
- Domain: `DeviceInfo`, `DisplayInfo` models and repository interfaces.
- Data: repository implementations switch to the injected
  `CoroutineDispatcher` (IO) and call data sources. Data sources are
  interfaces with `Impl` classes bound in `di/`.
- Formatting: `ValueFormatter` and per-tab row mappers are pure Kotlin and
  unit tested. Labels are `UiText` (usually a string resource).

**State and navigation**

- Each list tab exposes `StateFlow<InfoListUiState(loadingMode, rows)>`.
  Values are re-read when a tab gets a new subscriber after more than 5 s
  without one (for example, returning from background), so uptime refreshes.
- Single Activity. `FragmentStateAdapter` owns tab fragments; tab switches do
  not touch the back stack, so Back exits the app. The selected tab is saved
  in `MainViewModel` (`SavedStateHandle`) and restored on recreation.
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
- Live tabs (CPU, Battery) collect at `Lifecycle.State.RESUMED` and use
  `stateIn(WhileSubscribed(5000))` so polling and receivers stop when the tab
  is not visible.
- Async: `viewModelScope` plus an injected dispatcher; no work in
  `Application.onCreate()` except Hilt setup.
- DI: Hilt constructor injection; no static singletons.

## Gaps & Recommendations

### 1. Remaining diagnostics tabs

- Current: Device, CPU, Battery, and Display exist.
- Target: Memory & Storage and Screen Test per
  `docs/requirements/001-device-diagnostics.md` Phases 5-7.
- Priority: High

