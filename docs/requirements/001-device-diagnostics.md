# 001 - Device Diagnostics

- Status: In progress. Phases 1-3 implemented; Phase 4 implemented (pending owner build); Phases 5-7 open.
- Owner: @phannam
- Spec author: planning agent
- Implements: `docs/ARCHITECTURE.md` target rules, ADR-TARGET-001..004
- Build policy: the owner runs builds and reports errors. Agents implementing
  a phase must not run Gradle unless the owner approves it for that phase.

## 1. Goal

Turn DeviceInfo into a hardware diagnostics app similar to DevCheck / Device
Info HW: show live, accurate information about the device, CPU, battery,
display, memory and storage. Let the user test the screen for dead pixels and
lines, and share a full report through the system share sheet.

At the same time, finish the architecture migration: Hilt, repository, data
sources, ViewModels with `StateFlow`, Material `TabLayout` + `ViewPager2`,
`RecyclerView`. Remove `AppManager` and the legacy `activities`, `fragments`,
`utils` and `views` packages.

## 2. Owner Decisions (2026-11-15)

| Topic | Decision |
|---|---|
| Sections | Device, CPU, Battery, Display, Memory & Storage, Screen Test |
| Navigation | Material `TabLayout` (scrollable) + `ViewPager2`, swipe between tabs |
| Share | Generic `ACTION_SEND` share sheet, `text/plain`, no hardcoded recipient |
| Live data | Battery updates live; CPU frequencies poll every 1 s while the CPU tab is visible |
| Languages | English (default `values/`) + Vietnamese (`values-vi/`) |
| Builds | The owner runs builds; agents ask before running Gradle |

## 3. Non-Goals

- Detecting dead pixels or lines automatically. No public API or shell
  command reports per-pixel panel faults; detection is a visual test the user
  performs (section 4.6).
- Shell commands that need privileged permissions: `dumpsys`, `getprop` of
  restricted props, `/proc/stat` (blocked for apps since API 26),
  `/sys/class/thermal` (blocked by SELinux on most devices).
- Battery state-of-health %, design capacity, charging policy: they need
  `BATTERY_STATS` (signature/privileged) or hidden `PowerProfile` APIs.
- CPU usage %, per-core load, GPU info (needs a GL context), sensors, camera,
  network, SIM, IMEI/serial (privileged), root detection, benchmarks.
- Persisting test results across process death. Results live for the
  session only.
- Ads, analytics, crash reporting, Compose.

## 4. Information Spec

Rules for every section:

- Data source fields that may be missing on a device or API level are
  nullable in the domain model. The UI shows `Unavailable` / `Không khả dụng`
  for `null`. Never show `0`, `-1` or empty text for missing data.
- API-gated fields check `Build.VERSION.SDK_INT` inside the data source only.
- Every label and every enum value (status, health, plug type...) is a string
  resource. Numbers are formatted by pure Kotlin formatters in the UI layer
  (unit tested), using the user's locale.
- Units: storage and RAM use decimal units (1 GB = 1000^3 bytes) to match
  Android Settings. Temperatures use °C with 1 decimal. Frequencies use MHz
  (or GHz with 2 decimals when ≥ 1000 MHz).

### 4.1 Device tab

Source: `android.os.Build`, `System.getProperty`, `SystemClock`.

| Row | Source | Notes |
|---|---|---|
| Manufacturer | `Build.MANUFACTURER` | |
| Brand | `Build.BRAND` | |
| Model | `Build.MODEL` | |
| Device (codename) | `Build.DEVICE` | |
| Product | `Build.PRODUCT` | |
| Board | `Build.BOARD` | |
| Hardware | `Build.HARDWARE` | |
| Android version | `Build.VERSION.RELEASE` | |
| API level | `Build.VERSION.SDK_INT` | |
| Security patch | `Build.VERSION.SECURITY_PATCH` | Format as localized date when parsable |
| Build number | `Build.DISPLAY` | Moved here from Display tab |
| Build fingerprint | `Build.FINGERPRINT` | Long value; row must wrap |
| Bootloader | `Build.BOOTLOADER` | |
| Kernel version | `System.getProperty("os.version")` | |
| Java VM | `System.getProperty("java.vm.version")` | ART version string |
| Uptime | `SystemClock.elapsedRealtime()` | Format `Xd Yh Zm` |

### 4.2 CPU tab (live)

Static part, read once:

| Row | Source | Notes |
|---|---|---|
| SoC manufacturer | `Build.SOC_MANUFACTURER` (API 31+) | `null` below 31 |
| SoC model | `Build.SOC_MODEL` (API 31+) | Fallback: `Hardware` line of `/proc/cpuinfo`, then `Build.HARDWARE` |
| Supported ABIs | `Build.SUPPORTED_ABIS` | Comma-separated |
| Cores | `/sys/devices/system/cpu/possible`, fallback `Runtime.availableProcessors()` | `possible` also counts offline cores |
| Clusters | Group cores by `cpuinfo_max_freq` | e.g. `4 × 2.00 GHz + 4 × 2.80 GHz` |
| Per-core min/max | `/sys/devices/system/cpu/cpuN/cpufreq/cpuinfo_min_freq`, `cpuinfo_max_freq` (kHz) | |
| Governor | `.../cpuN/cpufreq/scaling_governor` | Optional, `null` if unreadable |
| Thermal status | `PowerManager.currentThermalStatus` (API 29+) | Enum → string res |
| Thermal headroom | `PowerManager.getThermalHeadroom(10)` (API 30+) | `NaN` → `null` |

Live part, polled every 1 s:

- Per-core current frequency from `.../cpuN/cpufreq/scaling_cur_freq`.
- Offline core (file missing or `cpuN/online` = 0) shows `Offline` / `Đang tắt`.
- If no `cpufreq` file is readable (SELinux), show one notice row
  `Live frequency is not available on this device` and stop polling.
- File reads run on the injected IO dispatcher. Polling runs only while the
  CPU tab is `RESUMED` (see 5.4).

### 4.3 Battery tab (live)

Source: sticky `Intent.ACTION_BATTERY_CHANGED` (live via a receiver),
`BatteryManager` properties (polled every 2 s while visible),
`PowerManager.isPowerSaveMode`.

| Row | Source | Notes |
|---|---|---|
| Level | `EXTRA_LEVEL` / `EXTRA_SCALE` | Percent, with progress bar |
| Status | `EXTRA_STATUS` | Charging, Discharging, Full, Not charging, Unknown |
| Power source | `EXTRA_PLUGGED` | AC, USB, Wireless, Dock (API 33+), None |
| Health | `EXTRA_HEALTH` | Good, Overheat, Dead, Over voltage, Failure, Cold, Unknown |
| Temperature | `EXTRA_TEMPERATURE` (tenths of °C) | 1 decimal °C |
| Voltage | `EXTRA_VOLTAGE` (mV) | Show as `x.xxx V` |
| Technology | `EXTRA_TECHNOLOGY` | e.g. `Li-ion` |
| Present | `EXTRA_PRESENT` | |
| Cycle count | `EXTRA_CYCLE_COUNT` (API 34+) | `null` below 34 or when `-1` |
| Current now | `BATTERY_PROPERTY_CURRENT_NOW` (µA) | Show signed mA; `Int.MIN_VALUE`/`0` → `null` |
| Current average | `BATTERY_PROPERTY_CURRENT_AVERAGE` (µA) | Same rules |
| Charge counter | `BATTERY_PROPERTY_CHARGE_COUNTER` (µAh) | Show mAh |
| Estimated full capacity | `chargeCounter / (level / 100)` | Only when counter > 0 and level ≥ 10; label says "estimated" |
| Time to full | `BatteryManager.computeChargeTimeRemaining()` (API 28+) | Only while charging; `-1` → `null` |
| Battery saver | `PowerManager.isPowerSaveMode` | On / Off |

Known risk: some OEMs report `CURRENT_NOW` in mA instead of µA, or with
inverted sign. Follow the API contract (µA); do not guess. Record the
device in section 10 if values look wrong during manual checks.

Register the receiver with `ContextCompat.registerReceiver(...,
ContextCompat.RECEIVER_NOT_EXPORTED)`; system broadcasts are still delivered.
No permission is needed.

### 4.4 Display tab

Source: `DisplayManager.getDisplay(Display.DEFAULT_DISPLAY)`,
`androidx.window` `WindowMetricsCalculator`, `Resources.displayMetrics`,
`Configuration`.

| Row | Source | Notes |
|---|---|---|
| Physical resolution | `display.mode.physicalWidth × physicalHeight` | Panel native pixels |
| App window size | `WindowMetricsCalculator.computeCurrentWindowMetrics(activity)` | Passed in from the UI; changes with multi-window |
| Density | `displayMetrics.densityDpi` + bucket (mdpi…xxxhdpi) | |
| Scale factor | `displayMetrics.density` | |
| xdpi / ydpi | `displayMetrics.xdpi`, `ydpi` | |
| Estimated size | `sqrt((w/xdpi)² + (h/ydpi)²)` inches | Label "estimated"; OEM dpi values can be wrong |
| Refresh rate | `display.mode.refreshRate` | Hz, no decimals when integral |
| Supported refresh rates | `display.supportedModes` at the native resolution | e.g. `60, 90, 120 Hz` |
| HDR | API 34+: `display.mode.supportedHdrTypes`; else `display.hdrCapabilities.supportedHdrTypes` | HDR10, HDR10+, HLG, Dolby Vision, or None |
| Wide color gamut | `display.isWideColorGamut` | |
| Font scale | `configuration.fontScale` | Replaces deprecated `scaledDensity` |
| Display cutout | Root window insets `displayCutout` | Yes/No, passed from UI |

Display values that depend on the Activity window (app window size, cutout)
are read by the Fragment and passed to the ViewModel as a
`WindowSnapshot` value; the data source must not hold an Activity.
Values reload on configuration change.

Implementation notes: `DisplayFragment` reads the snapshot with
`WindowSnapshotReader` in `doOnLayout` (insets are dispatched by then) and
calls `DisplayViewModel.onWindowMeasured`. The ViewModel keeps snapshots in
a `MutableSharedFlow(replay = 1)` so an equal snapshot after recreation still
reloads display values. `MainActivity` passes its own snapshot to
`MainViewModel.onShareClicked(window)` for the report.

### 4.5 Memory & Storage tab

| Row | Source | Notes |
|---|---|---|
| Total RAM | `ActivityManager.MemoryInfo.totalMem` | |
| Available RAM | `MemoryInfo.availMem` | Plus percent used bar |
| Low memory threshold | `MemoryInfo.threshold` | |
| Low memory now | `MemoryInfo.lowMemory` | Yes/No |
| Internal storage total | `StorageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)` | No permission needed |
| Internal storage free | `StorageStatsManager.getFreeBytes(UUID_DEFAULT)` | Plus percent used bar |

RAM refreshes every 2 s while the tab is visible. Storage refreshes when the
tab resumes. `StorageStatsManager` calls run on the IO dispatcher.

### 4.6 Screen Test tab

The tab shows a short explanation, a "Start test" button and the results
of the last run in this session.

`ScreenTestActivity` (full screen):

- Immersive: hide system bars with `WindowInsetsControllerCompat`
  (`BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`), `FLAG_KEEP_SCREEN_ON`,
  `layoutInDisplayCutoutMode = ALWAYS` (API 30+, `SHORT_EDGES` below) so
  the whole panel is covered.
- Brightness: set `window.attributes.screenBrightness = 1f` during the test
  only (window-level, no `WRITE_SETTINGS`).
- Patterns in order, tap to advance, back to exit:
  1. Black (stuck/bright pixels, backlight bleed)
  2. White (dead/dark pixels)
  3. Red, 4. Green, 5. Blue (stuck sub-pixels)
  6. Gray 50% (burn-in, mura, discoloration)
  7. Horizontal gradient black → white (banding, horizontal lines)
  8. Vertical gradient black → white (vertical lines)
  9. Fine 1 px checkerboard (line defects, flicker)
- Patterns are drawn by one custom `ScreenTestPatternView` (Canvas), not
  bitmaps. Gradients must not be dithered (`Paint.isDither = false`).
- A small hint ("Tap to continue · n/9") appears for 2 s on each pattern,
  then hides so it does not hide pixels.
- After the last pattern a result dialog asks three Yes/No questions:
  dead or stuck pixels, lines or banding, burn-in or discoloration.
  Answers are returned to the tab and to the share report.

Results are stored in `ScreenTestResultRepository` (Hilt `@Singleton`,
in-memory `StateFlow<ScreenTestResult?>`).

## 5. Architecture

Follows `docs/ARCHITECTURE.md` and `docs/MODULE_MAP.md`. Additions and
decisions specific to this spec are listed here.

### 5.1 Packages

```text
vn.npstudio.deviceinfo
├── DeviceInfoApplication            @HiltAndroidApp
├── common/
│   ├── extension/  collectWithLifecycle, Context helpers
│   ├── stateflow/  LoadingMode, UiEffect
│   └── text/       UiText
├── di/             DispatcherModule, DataSourceModule, RepositoryModule
├── domain/
│   ├── model/      DeviceInfo, CpuInfo, CpuCoreFrequency, BatteryInfo,
│   │               DisplayInfo, WindowSnapshot, MemoryInfo, StorageInfo,
│   │               ScreenTestResult, enums (BatteryStatus, BatteryHealth,
│   │               PowerSource, ThermalStatus, HdrType)
│   └── repository/ DeviceInfoRepository, CpuRepository, BatteryRepository,
│                   DisplayRepository, MemoryRepository, ScreenTestResultRepository
├── data/
│   ├── source/     BuildInfoDataSource, CpuInfoDataSource, SysFsReader,
│   │               BatteryDataSource, DisplayDataSource, MemoryDataSource
│   └── repository/ *RepositoryImpl
├── ui/
│   ├── list/       InfoRowAdapter (ListAdapter), InfoRowUiModel
│   └── format/     ValueFormatter (bytes, frequency, temperature, duration)
└── feature/
    ├── main/       MainActivity, MainViewModel, MainTab, MainUiEffect,
    │               ReportSection, ShareReportRenderer
    ├── device/     DeviceFragment, DeviceViewModel, DeviceRowMapper
    ├── cpu/        CpuFragment, CpuViewModel, CpuRowMapper
    ├── battery/    BatteryFragment, BatteryViewModel, BatteryRowMapper
    ├── display/    DisplayFragment, DisplayViewModel, DisplayRowMapper
    ├── memory/     MemoryFragment, MemoryViewModel, MemoryRowMapper
    └── screentest/ ScreenTestFragment, ScreenTestViewModel,
                    ScreenTestActivity, ScreenTestPatternView, ScreenTestPattern
```

Data sources are interfaces with an `Impl` bound in `di/`. `SysFsReader`
(reads a text file, returns `String?`, never throws) is the only class that
touches `/proc` and `/sys`, so parsers can be unit tested with fakes.

### 5.2 Flow per tab

```text
Fragment ──collect(RESUMED)──> XxxViewModel.uiState: StateFlow<InfoListUiState>
XxxViewModel ──> XxxRepository: Flow<DomainModel> (live) or suspend fun (static)
XxxRepository ──> XxxDataSource(s)                (platform only)
XxxViewModel ──> XxxRowMapper (pure) ──> List<InfoRowUiModel>
```

- `InfoRowUiModel(label: UiText, value: UiText, progress: Int? = null)`, with a
  secondary constructor taking `@StringRes label: Int` for fixed labels.
  `UiText` labels allow per-core rows such as "Core 3".
  `progress` (0–100) renders a `LinearProgressIndicator` for level/usage rows.
- `InfoListUiState(loadingMode: LoadingMode, rows: List<InfoRowUiModel>)`.
- All list tabs share one layout `fragment_info_list.xml` (RecyclerView)
  and `item_info_row.xml`. DiffUtil matches rows by label (no stable ids;
  hashes can collide) and change animations are off, so live updates do not
  flicker.

### 5.3 Live data

- `BatteryRepository.observe(): Flow<BatteryInfo>` = `callbackFlow` receiver
  for `ACTION_BATTERY_CHANGED`, combined with a 2 s ticker for
  `BatteryManager` properties.
- `CpuRepository.observeFrequencies(): Flow<List<CpuCoreFrequency>>` = 1 s
  ticker on the IO dispatcher; emits once and completes if nothing is
  readable.
- `MemoryRepository.observeRam(): Flow<MemoryInfo>` = 2 s ticker.
- Ticker intervals live in one injectable `PollingIntervals` so tests can
  override them.

### 5.4 Lifecycle rules for live tabs

- ViewModels expose `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
- Fragments collect with `collectWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED)`.
  `FragmentStateAdapter` keeps off-screen pages at `STARTED`, so only the
  visible tab polls. After 5 s off-screen, upstream polling stops.

### 5.5 Main screen and navigation

- `activity_main.xml`: `CoordinatorLayout` > `AppBarLayout` (`MaterialToolbar`
  + scrollable `TabLayout`) + `ViewPager2`.
- `MainTab` enum (`DEVICE, CPU, BATTERY, DISPLAY, MEMORY, SCREEN_TEST`) with
  `@StringRes title`. `TabLayoutMediator` binds titles.
- The selected tab survives recreation (ViewPager2 saved state, mirrored in
  `MainViewModel` via `SavedStateHandle`). No `FragmentTransaction` back stack.
- Edge-to-edge: targetSdk 37 enforces it. `AppBarLayout` handles top insets
  (`fitsSystemWindows`); lists apply bottom insets as padding with
  `ViewCompat.setOnApplyWindowInsetsListener`.
- Theme: `Theme.Material3.DayNight.NoActionBar` with the existing brand color
  as `colorPrimary`. Dark mode supported.
- Keep `screenOrientation="portrait"` for `MainActivity`; `ScreenTestActivity`
  also portrait.

### 5.6 Share report

Decision for the open question in `docs/MODULE_MAP.md` (where localized
labels are resolved):

- `MainViewModel.onShareClicked()` takes one snapshot from every
  repository (`first()` for flows), maps each section with the same row
  mappers as the tabs, and emits
  `MainUiEffect.Share(sections: List<ReportSection>)` where
  `ReportSection(@StringRes title: Int, rows: List<InfoRowUiModel>)`.
- `MainActivity` renders text with `ShareReportRenderer.render(sections,
  header, resolve: (UiText) -> String)`, a pure function that is unit
  tested with a fake resolver. Then it starts
  `Intent.createChooser(Intent(ACTION_SEND).setType("text/plain")
  .putExtra(EXTRA_SUBJECT, ...).putExtra(EXTRA_TEXT, text), null)`.
- Report header: app name + version, report time (local), device model.
- The Screen Test section shows `Not tested` when there is no result.
- No `<queries>` element and no `queryIntentActivities` call are needed.

### 5.7 Dependencies (version catalog)

| Alias | Artifact | Version |
|---|---|---|
| `material` | `com.google.android.material:material` | 1.14.0 |
| `androidx-core-ktx` | `androidx.core:core-ktx` | 1.19.1 |
| `androidx-activity-ktx` | `androidx.activity:activity-ktx` | 1.13.0 |
| `androidx-fragment-ktx` | `androidx.fragment:fragment-ktx` | 1.9.1 (replaces `fragment`) |
| `androidx-lifecycle-viewmodel-ktx`, `-runtime-ktx` | `androidx.lifecycle` | 2.11.0 |
| `androidx-recyclerview` | `androidx.recyclerview:recyclerview` | 1.4.0 |
| `androidx-viewpager2` | `androidx.viewpager2:viewpager2` | 1.1.0 (replaces `viewpager`) |
| `androidx-window` | `androidx.window:window` | 1.5.1 |
| `kotlinx-coroutines-android` | `org.jetbrains.kotlinx` | 1.11.0 |
| `hilt-android` / `hilt-compiler` (ksp) | `com.google.dagger` | 2.60.1 |
| plugin `hilt` | `com.google.dagger.hilt.android` | 2.60.1 |
| plugin `ksp` | `com.google.devtools.ksp` | 2.3.14 |
| test: `kotlinx-coroutines-test` | `org.jetbrains.kotlinx` | 1.11.0 |
| test: `turbine` | `app.cash.turbine:turbine` | 1.2.1 |

- Versions were the latest stable on 2026-11-15; the implementing agent
  may bump patch versions only.
- KSP must work with AGP built-in Kotlin. If it does not, use
  `com.android.legacy-kapt` per `docs/CONVENTIONS.md` and record it in
  `docs/DECISIONS.md`. Do not apply `org.jetbrains.kotlin.android`.
- Remove `fileTree(libs)`, `androidx.viewpager`, and `androidx.fragment`
  (replaced by `fragment-ktx`).
- R8 stays enabled for release. Hilt and AndroidX ship their own consumer
  keep rules; no custom rules are expected.

### 5.8 Permissions and manifest

- No new permissions.
- Rename the application class to `.DeviceInfoApplication`.
- Register `feature.screentest.ScreenTestActivity` (not exported,
  full-screen theme).

### 5.9 Code to delete

`activities/`, `fragments/`, `utils/`, `views/` packages (including vendored
`SlidingTabLayout`/`SlidingTabStrip`), `layout/fragment_listitems.xml`,
`layout/view_tab.xml`, `layout/item_info.xml` (replaced by
`item_info_row.xml`), `mipmap-xhdpi/ic_info_*`, `ic_display_*`,
`drawable/bg_shadow_horizontal.xml` if unused, `values-v21/styles.xml`
(minSdk 26), unused template strings/styles/dimens/colors.

## 6. Delivery Phases

Each phase is one agent task and ends in a buildable, working app. After
each phase the agent lists the Gradle commands for the owner to run. The
owner builds, tests on device, and reports errors.

### Phase 1 - Foundation and migration (implemented)

- Notes: `androidx.window` was deferred to Phase 4 and added there. Display
  resolution uses `Display.getMode()` physical size.

- Add dependencies, Hilt, KSP; `DeviceInfoApplication`.
- `common/` contracts, dispatcher module, `ui/list` adapter + row layout,
  `ValueFormatter`.
- Material 3 theme, new `MainActivity` (toolbar, tabs, ViewPager2), edge-to-edge.
- Device tab (full 4.1) and Display tab (parity with today's fields:
  resolution, density, dpi, font scale) on the new architecture.
- Share via share sheet (Device + Display sections).
- Delete legacy code from 5.9. Strings in `values/` and `values-vi/`.
- Acceptance: app launches, two tabs swipe, rotation/recreation keeps the
  selected tab, Back exits the app from any tab, share sheet opens with the
  report, no `AppManager`, no legacy packages.

### Phase 2 - CPU tab (implemented)

- `SysFsReader`, `CpuInfoDataSource`, parsers, `CpuRepository`, tab UI.
- Acceptance: SoC and cluster rows correct on the test device; frequencies
  change every second only while the tab is visible; unreadable sysfs shows
  the notice row, no crash.

### Phase 3 - Battery tab (implemented)

- `BatteryDataSource` (receiver + properties), `BatteryRepository`, tab UI
  with level progress row.
- Acceptance: plugging/unplugging the charger updates status and power
  source within 2 s; temperature and voltage match `adb shell dumpsys battery`
  (owner check); API-gated rows show Unavailable on API 28.

### Phase 4 - Display tab (full) (implemented)

- Physical resolution, refresh rates, HDR, wide color gamut, estimated size,
  cutout, window size via `WindowSnapshot`.
- Acceptance: values match `adb shell dumpsys display` on the test device.

### Phase 5 - Memory & Storage tab

- `MemoryDataSource`, `MemoryRepository`, tab UI with usage bars.
- Acceptance: totals match Settings > Storage / About phone within rounding.

### Phase 6 - Screen Test

- Screen Test tab, `ScreenTestActivity`, `ScreenTestPatternView`, result
  dialog, `ScreenTestResultRepository`.
- Acceptance: every pattern covers the full panel including cutout and
  system bar areas; hint hides after 2 s; Back exits at any time without
  results; results appear in the tab and in the share report.

### Phase 7 - Report completion and docs

- All sections in the share report, header, Not tested state.
- Update `AGENTS.md` and all `docs/*.md` current-state sections; move
  resolved gaps out; set this spec's status to Done.

## 7. Testing

Unit tests (JVM, `app/src/test`), added in the phase that adds the code:

- Parsers: `/proc/cpuinfo` (with and without `Hardware`), `cpu/possible`
  ranges (`0-7`, `0-3,4-7`), frequency files (kHz text, blank, missing).
- `CpuRepositoryImpl` cluster grouping and offline cores, with fake
  `SysFsReader`.
- `BatteryRowMapper`: status/health/plug enum mapping, µA → mA, tenths °C,
  mV → V, estimated capacity rules, `null` handling.
- `ValueFormatter`: decimal byte units, MHz/GHz, durations, locale.
- ViewModels: initial state, live updates, `WhileSubscribed` stop, using
  fakes + `StandardTestDispatcher` + Turbine.
- `ShareReportRenderer` with a fake resolver; `MainViewModel` share effect.
- `ScreenTestViewModel` pattern sequence and result handling.

Instrumented tests: keep `ApplicationTest`; no UI tests are required in
this spec.

Manual checks by the owner (each phase): Samsung SM-N950U1 (API 28) plus at
least one API 34+ device or emulator for API-gated rows.

## 8. Strings

- Default `values/strings.xml` in English, `values-vi/strings.xml` in
  Vietnamese, same keys.
- Key prefixes: `tab_*`, `label_<section>_*`, `value_*` (enum values),
  `screen_test_*`, `report_*`, `common_*` (`common_unavailable`, `common_yes`,
  `common_no`).
- Units use format strings, e.g. `<string name="unit_celsius">%1$s °C</string>`.

## 9. Docs To Update

During the phases (owned by the phase that changes them):

- `docs/ARCHITECTURE.md`, `docs/MODULE_MAP.md`: add cpu, battery, memory,
  screentest features; record the 5.6 share decision.
- `docs/DATA_MODELS.md`: nullable fields mean "unavailable on this device".
- `docs/DECISIONS.md`: Material TabLayout + ViewPager2, share sheet, live
  polling rules, KSP vs legacy-kapt.
- `docs/TESTING.md`: new test files and manual checks.
- `AGENTS.md`: current-state summary and tech stack table.

## 10. Risks and Open Questions

- OEM `CURRENT_NOW` units/sign vary; record affected devices here.
- `cpufreq` sysfs may be blocked by SELinux on some devices (handled by the
  notice row).
- `xdpi`/`ydpi` are wrong on some OEM builds, so screen size is shown as
  estimated.
- KSP compatibility with AGP built-in Kotlin (fallback in 5.7).
- Old package `info.device.deviceinfo` is still installed on the owner's
  phone; uninstall it to avoid confusion during manual checks.
