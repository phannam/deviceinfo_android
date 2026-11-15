# Data Models

## Current Models

No persistence or network models exist.

| Model | Location | Notes |
|---|---|---|
| `DeviceInfo` | `domain/model/DeviceInfo.kt` | Build/OS strings (nullable when blank or `Build.UNKNOWN`), `apiLevel: Int`, `uptimeMillis: Long` |
| `DisplayInfo` | `domain/model/DisplayInfo.kt` | `widthPx`/`heightPx: Int?` from the default display mode (physical), `densityDpi: Int`, `density`, `xdpi`, `ydpi`, `fontScale: Float` |
| `InfoRowUiModel` | `ui/list/InfoRowUiModel.kt` | `@StringRes label`, `value: UiText`, `progress: Int?` (0-100) |
| `InfoListUiState` | `ui/list/InfoListUiState.kt` | `loadingMode: LoadingMode`, `rows: List<InfoRowUiModel>` |
| `MainTab` | `feature/main/MainTab.kt` | Enum `DEVICE`, `DISPLAY` with `@StringRes titleRes` |
| `ReportSection` | `feature/main/ReportSection.kt` | `@StringRes title`, `rows` |
| `MainUiEffect.Share` | `feature/main/MainUiEffect.kt` | `sections: List<ReportSection>` |
| `UiText` | `common/text/UiText.kt` | `Dynamic(value)` or `Resource(resId, args)`; args may be nested `UiText` |

Mapping:

- Data sources -> domain models (raw values, nulls for unavailable).
- Domain models -> `List<InfoRowUiModel>` in `DeviceRowMapper` /
  `DisplayRowMapper` using `ValueFormatter`. Null values become
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
