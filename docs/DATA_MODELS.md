# Data Models

## Current Models

No network models exist. The only persisted data is the app configuration
(SharedPreferences file `app_config`, see below).

| Model | Location | Notes |
|---|---|---|
| `DeviceInfo` | `domain/model/DeviceInfo.kt` | `deviceName: String?`, `hardware: DeviceHardware` (Build strings), `vendor: VendorDetails` (bootloader state and Samsung `getprop` values, parsed by `SystemPropertiesParser.vendor`), `identifiers: DeviceIdentifiers`, `telephony: TelephonyInfo`, `features: Map<HardwareFeature, Boolean>`, `codecs: List<CodecInfo>` |
| `GuardedValue<T>` | `domain/model/DeviceInfo.kt` | `Available(value)`, `NeedsPermission(permission)`, `Restricted` (blocked on this OS version), `Unavailable` |
| `CodecInfo` | `domain/model/DeviceInfo.kt` | `name`, `kind: CodecKind` (`VIDEO`, `AUDIO`), `mimeTypes`, `encoder`, `hardwareAccelerated: Boolean?` (null below API 29) |
| `SystemInfo` | `domain/model/SystemInfo.kt` | `build: AndroidBuild`, `security: SystemSecurity` (root, `SelinuxMode?`, partition flags, Treble, VNDK), `systemFeatures`, `runtime: RuntimeInfo` (kernel, Java VM, heap, Play services), `region: RegionInfo` (BCP 47 language tag, IANA zone id, boot count), `drm: DrmInfo?` (Widevine) |
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
| `PollingIntervals` | `data/PollingIntervals.kt` | `cpuFrequencyMillis` (1000), `batteryPropertiesMillis` (2000), `memoryMillis` (2000), `uptimeMillis` (1000) |
| `InfoRowUiModel` | `ui/list/InfoRowUiModel.kt` | `label: UiText` (secondary constructor takes `@StringRes Int`), `value: UiText`, `progress: Int?` (0-100), `action: RowAction?` (`ShowList(title, entries)` or `RequestPermission(permission)`) |
| `InfoSection` | `ui/list/InfoSection.kt` | `title: UiText?`, `@DrawableRes icon`, `rows`, `features: List<FeatureUiModel>`, `inReport` (false = left out of the share report) |
| `HeroUiModel` | `ui/list/HeroUiModel.kt` | `badge`/`badgeLabel` or `badgeIcon`, `title`, `subtitle?`, `detail?` |
| `InfoListUiState` | `ui/list/InfoListUiState.kt` | `loadingMode: LoadingMode`, `sections: List<InfoSection>`, `hero: HeroUiModel?`; `rows` flattens all section rows |
| `InfoListItem` | `ui/list/InfoListItem.kt` | `Hero`, `Header`, `Row`, `Features` adapter items with stable `key` and `RowPosition` |
| `ScreenTestResult` | `domain/model/ScreenTestResult.kt` | `defects: Set<ScreenDefect>` (`DEAD_OR_STUCK_PIXELS`, `LINES_OR_BANDING`, `BURN_IN_OR_DISCOLORATION`); null in the repository means not tested |
| `ScreenTestUiState` | `feature/screentest/ScreenTestUiState.kt` | `pattern`, `hintVisible`, `askingResult`, `defects`; `position`/`total` for the hint |
| `MainTab` | `feature/main/MainTab.kt` | Enum `DEVICE`, `SYSTEM`, `CPU`, `BATTERY`, `DISPLAY`, `MEMORY`, `SCREEN_TEST` with `@StringRes titleRes` and `id: TabId`; `MainTab.of(TabId)` |
| `TabManagerItem` | `feature/tabmanager/TabManagerUiState.kt` | `id: TabId`, `@StringRes title`, `visible`, `toggleEnabled` (false for the last visible tab) |
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
- Domain models -> `List<InfoSection>` in each `XRowMapper.sections(...)`
  using `ValueFormatter`. Null values become `common_unavailable`.
  `XRowMapper.map(...)` = `sections(...).reportRows()` (share report rows;
  feature chips become "Supported" / "Not supported" rows).
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
