# Module Map

## Current Module Structure

`settings.gradle.kts` includes one module: `:app`.

Build configuration:

- Root `build.gradle.kts` declares the Android application, KSP, and Hilt
  plugins (`apply false`).
- `settings.gradle.kts` configures repositories (`google()`, `mavenCentral()`,
  `gradlePluginPortal()` for plugins) with `FAIL_ON_PROJECT_REPOS`.
- `gradle/libs.versions.toml` holds SDK levels, AGP, plugin, and library
  versions.
- `gradle/gradle-daemon-jvm.properties` pins the Gradle daemon JVM
  (JetBrains, version 25).

Packages under `vn.npstudio.deviceinfo`:

| Package | Contents |
|---|---|
| (root) | `DeviceInfoApplication` (`@HiltAndroidApp`) |
| `common.extension` | `collectWithLifecycle` |
| `common.stateflow` | `LoadingMode` |
| `common.text` | `UiText` |
| `di` | `DispatcherModule`, `DataSourceModule`, `RepositoryModule` |
| `domain.model` | `AppConfig` (+ `DarkMode`, `AppColor`, `TemperatureUnit`), `TabPreferences` (+ `TabId`), `DeviceInfo`, `DisplayInfo` (+ `HdrType`, `WindowSnapshot`), `BatteryInfo` (+ `BatteryStatus`, `PowerSource`, `BatteryHealth`), `CpuInfo` (+ `CpuCore`, `CpuCluster`, `CpuCoreFrequency`), `ThermalStatus`, `MemoryInfo`, `StorageInfo`, `ScreenTestResult` (+ `ScreenDefect`) |
| `domain.repository` | `AppConfigRepository`, `DeviceInfoRepository`, `CpuRepository`, `BatteryRepository`, `DisplayRepository`, `MemoryRepository`, `ScreenTestResultRepository` |
| `data` | `PollingIntervals` (live ticker intervals, provided in `DispatcherModule`) |
| `data.source` | `AppConfigLocalDataSource(Impl)` (+ `AppConfigKeys`, SharedPreferences), `AppConfigParser`, `BatteryDataSource(Impl)` (+ raw `BatteryBroadcast`, `BatteryProperties`), `BatteryParser`, `BuildInfoDataSource(Impl)`, `CpuInfoDataSource(Impl)`, `DisplayDataSource(Impl)`, `MemoryDataSource(Impl)`, `SysFsReader(Impl)`, `CpuSysFsParser` |
| `data.repository` | `AppConfigRepositoryImpl` (`@Singleton`, synchronous first read), `BatteryRepositoryImpl`, `DeviceInfoRepositoryImpl`, `CpuRepositoryImpl`, `DisplayRepositoryImpl`, `MemoryRepositoryImpl`, `ScreenTestResultRepositoryImpl` (`@Singleton`, in-memory) |
| `ui.list` | `InfoListFragment`, `InfoListUiState`, `InfoRowAdapter`, `InfoRowUiModel` |
| `ui.format` | `ValueFormatter` |
| `ui.theme` | `ThemeApplier` (night mode + theme overlays per Activity), `ThemeOverlays` (`AppColor` -> style), `NightModeMapper` |
| `ui.window` | `WindowSnapshotReader` (Activity window size and cutout for the UI) |
| `feature.main` | `MainActivity`, `MainViewModel`, `MainTab`, `MainTabAdapter`, `MainUiEffect`, `ReportSection`, `ShareReportRenderer` |
| `feature.device` | `DeviceFragment`, `DeviceViewModel`, `DeviceRowMapper` |
| `feature.battery` | `BatteryFragment`, `BatteryViewModel`, `BatteryRowMapper` |
| `feature.cpu` | `CpuFragment`, `CpuViewModel`, `CpuRowMapper` |
| `feature.display` | `DisplayFragment`, `DisplayViewModel`, `DisplayRowMapper` |
| `feature.memory` | `MemoryFragment`, `MemoryViewModel`, `MemoryRowMapper` |
| `feature.screentest` | `ScreenTestFragment`, `ScreenTestResultViewModel`, `ScreenTestActivity`, `ScreenTestViewModel`, `ScreenTestUiState`, `ScreenTestUiEffect`, `ScreenTestPattern`, `ScreenTestPatternView`, `ScreenTestRowMapper` |

## Target Module Structure

Stay with a single `:app` module. The app is too small for feature or core
modules to pay off. Enforce boundaries with packages instead. The full target
package list is in `docs/requirements/001-device-diagnostics.md` section 5.1.

| Package | Owns | May depend on | Must not contain |
|---|---|---|---|
| (root) | `DeviceInfoApplication` | `di` | Feature logic |
| `feature.main` | Main screen, tabs, share report | `domain`, `ui`, `common`, other `feature.*` (tab fragments, row mappers) | Platform data access |
| `feature.<tab>` | Fragment, ViewModel, row mapper | `domain`, `ui`, `common` | Platform data access |
| `ui` | Shared list UI and pure formatters | `domain`, `common` | Repositories, data sources |
| `domain` | Models and repository interfaces | Kotlin stdlib only | Android framework types, resources |
| `data` | Repository implementations and platform data sources | `domain` | UI, ViewModels |
| `di` | Hilt modules | `domain`, `data` | Feature logic |
| `common` | `stateflow`, `text`, `extension` helpers | AndroidX lifecycle | Feature or data code |

Share report decision: `MainViewModel` emits structured
`ReportSection(@StringRes title, rows)` values; `MainActivity` resolves
`UiText` and calls the pure `ShareReportRenderer`.

**Dependency rules**

- `feature.*` must not import another `feature.*` package, except
  `feature.main` using tab fragments and row mappers.
- `feature.*` and `ui` must not import `data`.
- `domain` must not import Android framework classes or `R`.
- `data` must not import `feature.*`, `ui`, or ViewModels.
- `common` must not import any feature, domain, or data package.

**File placement rules for new code**

- New tab: `feature/<name>/` with Fragment (extend `InfoListFragment` for
  list tabs), ViewModel, and row mapper; add it to `MainTab` and
  `MainTabAdapter`.
- New layout: `res/layout/` named per `docs/CONVENTIONS.md`.
- New platform reader: a data source in `data/source/` plus a domain model
  and repository in `domain/`, bound in `di/`. Add any required permission
  to the manifest in the same task.
- New shared UI component or formatter: `ui/`.
- New state/effect/text helper: `common/`. Reuse before adding.

## Gaps & Recommendations

None open for the module layout. Legacy `activities`, `fragments`, `utils`,
and `views` packages were removed in Phase 1.
