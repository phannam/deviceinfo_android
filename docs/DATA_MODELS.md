# Data Models

## Current Models

No network models exist. The only persisted data is the app configuration
(SharedPreferences file `app_config`, see below).

| Model | Location | Notes |
|---|---|---|
| `DeviceInfo` | `domain/model/DeviceInfo.kt` | Build/OS strings (nullable when blank or `Build.UNKNOWN`), `apiLevel: Int`, `uptimeMillis: Long` |
| `DisplayInfo` | `domain/model/DisplayInfo.kt` | `widthPx`/`heightPx: Int?` from the default display mode (physical), `densityDpi: Int`, `density`, `xdpi`, `ydpi`, `fontScale: Float`, `refreshRateHz: Float?`, `supportedRefreshRatesHz` (native resolution, ascending), `hdrTypes: List<HdrType>?` (empty = none, null = unknown), `wideColorGamut: Boolean?` |
| `HdrType` | `domain/model/DisplayInfo.kt` | `DOLBY_VISION`, `HDR10`, `HLG`, `HDR10_PLUS` |
| `WindowSnapshot` | `domain/model/DisplayInfo.kt` | Activity window `widthPx`/`heightPx`, `hasCutout: Boolean?`; measured by the UI, never by data sources |
| `CpuInfo` | `domain/model/CpuInfo.kt` | `socManufacturer`/`socModel: String?`, `supportedAbis`, `cores: List<CpuCore>`, `clusters: List<CpuCluster>` (by max freq, ascending), `governors`, `thermalStatus: ThermalStatus?`, `thermalHeadroom: Float?` |
| `CpuCore` | `domain/model/CpuInfo.kt` | `id`, `minFreqKhz`/`maxFreqKhz: Long?` |
| `CpuCluster` | `domain/model/CpuInfo.kt` | `coreCount`, `maxFreqKhz` |
| `CpuCoreFrequency` | `domain/model/CpuInfo.kt` | `coreId`, `currentKhz: Long?` (null = offline or unreadable) |
| `ThermalStatus` | `domain/model/ThermalStatus.kt` | Enum mirroring `PowerManager.THERMAL_STATUS_*` |
| `BatteryInfo` | `domain/model/BatteryInfo.kt` | `levelPercent`, `status: BatteryStatus`, `powerSource: PowerSource?`, `health: BatteryHealth`, `temperatureTenthsCelsius`, `voltageMillivolts`, `technology`, `present`, `cycleCount` (API 34+), `currentNowMicroamps`/`currentAverageMicroamps` (signed µA), `chargeCounterMicroampHours`, `chargeTimeRemainingMillis`, `powerSaveMode`; all nullable except enums |
| `MemoryInfo` | `domain/model/MemoryInfo.kt` | `totalBytes`, `availableBytes`, `lowMemoryThresholdBytes: Long?`, `lowMemory: Boolean?` from `ActivityManager.MemoryInfo` |
| `StorageInfo` | `domain/model/MemoryInfo.kt` | Internal storage `totalBytes`/`freeBytes: Long?` from `StorageStatsManager` (`UUID_DEFAULT`) |
| `BatteryBroadcast`, `BatteryProperties` | `data/source/BatteryDataSource.kt` | Raw intent extras and `BatteryManager` values (`-1` / `Int.MIN_VALUE` = missing); normalized by `BatteryParser` |
| `PollingIntervals` | `data/PollingIntervals.kt` | `cpuFrequencyMillis` (1000), `batteryPropertiesMillis` (2000), `memoryMillis` (2000) |
| `InfoRowUiModel` | `ui/list/InfoRowUiModel.kt` | `label: UiText` (secondary constructor takes `@StringRes Int`), `value: UiText`, `progress: Int?` (0-100) |
| `InfoListUiState` | `ui/list/InfoListUiState.kt` | `loadingMode: LoadingMode`, `rows: List<InfoRowUiModel>` |
| `ScreenTestResult` | `domain/model/ScreenTestResult.kt` | `defects: Set<ScreenDefect>` (`DEAD_OR_STUCK_PIXELS`, `LINES_OR_BANDING`, `BURN_IN_OR_DISCOLORATION`); null in the repository means not tested |
| `ScreenTestUiState` | `feature/screentest/ScreenTestUiState.kt` | `pattern`, `hintVisible`, `askingResult`, `defects`; `position`/`total` for the hint |
| `MainTab` | `feature/main/MainTab.kt` | Enum `DEVICE`, `CPU`, `BATTERY`, `DISPLAY`, `MEMORY`, `SCREEN_TEST` with `@StringRes titleRes` |
| `ReportSection` | `feature/main/ReportSection.kt` | `@StringRes title`, `rows` |
| `MainUiEffect.Share` | `feature/main/MainUiEffect.kt` | `sections: List<ReportSection>` |
| `AppConfig` | `domain/model/AppConfig.kt` | `darkMode: DarkMode` (`OFF`, `ON`, `SYSTEM`), `primaryColor`/`accentColor: AppColor` (14 palette entries, `BRAND` default), `temperatureUnit: TemperatureUnit` (`CELSIUS`, `FAHRENHEIT`) |
| `TabPreferences` | `domain/model/TabPreferences.kt` | `order: List<TabId>` (every tab once), `hidden: Set<TabId>` (never all), `visible` |
| `UiText` | `common/text/UiText.kt` | `Dynamic(value)`, `Resource(resId, args)` (args may be nested `UiText`), or `Joined(parts, separator)` |

Persisted keys (`AppConfigKeys`, values are enum names; tab lists are
comma-joined `TabId` names): `dark_mode`, `primary_color`, `accent_color`,
`temperature_unit`, `tab_order`, `tab_hidden`. `AppConfigParser` drops
unknown names and duplicates, appends missing tabs as visible, and shows
all tabs if every tab is hidden.

Mapping:

- Data sources -> domain models (raw values, nulls for unavailable).
- Domain models -> `List<InfoRowUiModel>` in `DeviceRowMapper` /
  `CpuRowMapper` / `BatteryRowMapper` / `DisplayRowMapper` using `ValueFormatter`. Null values become
  `common_unavailable`.
- `ReportSection` list -> share text in `ShareReportRenderer`.

## Target Model Design

| Layer | Owns | Must not contain |
|---|---|---|
| Domain | Typed raw values; nullable means unavailable | Android types, resource ids, formatted strings |
| UI | Label resource ids and `UiText` values | Platform calls |

All models planned in `docs/requirements/001-device-diagnostics.md`
section 5.1 now exist.

## Gaps & Recommendations

None open.
