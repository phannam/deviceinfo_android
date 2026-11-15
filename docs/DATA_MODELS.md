# Data Models

## Current Models

No persistence or network models exist.

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
| `BatteryBroadcast`, `BatteryProperties` | `data/source/BatteryDataSource.kt` | Raw intent extras and `BatteryManager` values (`-1` / `Int.MIN_VALUE` = missing); normalized by `BatteryParser` |
| `PollingIntervals` | `data/PollingIntervals.kt` | `cpuFrequencyMillis` (1000), `batteryPropertiesMillis` (2000) |
| `InfoRowUiModel` | `ui/list/InfoRowUiModel.kt` | `label: UiText` (secondary constructor takes `@StringRes Int`), `value: UiText`, `progress: Int?` (0-100) |
| `InfoListUiState` | `ui/list/InfoListUiState.kt` | `loadingMode: LoadingMode`, `rows: List<InfoRowUiModel>` |
| `MainTab` | `feature/main/MainTab.kt` | Enum `DEVICE`, `CPU`, `BATTERY`, `DISPLAY` with `@StringRes titleRes` |
| `ReportSection` | `feature/main/ReportSection.kt` | `@StringRes title`, `rows` |
| `MainUiEffect.Share` | `feature/main/MainUiEffect.kt` | `sections: List<ReportSection>` |
| `UiText` | `common/text/UiText.kt` | `Dynamic(value)`, `Resource(resId, args)` (args may be nested `UiText`), or `Joined(parts, separator)` |

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

Planned models are listed in `docs/requirements/001-device-diagnostics.md`
section 5.1 (`CpuInfo`, `BatteryInfo`, `MemoryInfo`, `StorageInfo`,
`WindowSnapshot`, `ScreenTestResult`, and enums).

## Gaps & Recommendations

None open for Phase 1 models.
