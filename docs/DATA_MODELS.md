# Data Models

## Current Models

The app has one model and no persistence or network models.

| Model | Location | Fields | Used by |
|---|---|---|---|
| `Item` | `utils/Item.java` | `name: String`, `value: String` (getters only; has an unused no-arg constructor) | `AppManager`, `ListFragment`, `ItemListAdapter` |
| `SlidingTab` | `views/SlidingTab.java` | `tab` (type id), normal/selected icon res, normal/selected text color, `name`, mutable `selected` | `MainActivity`, `TabActivity`, `SlidingTabRenderer` |
| `Constant.Tab` | `utils/Constant.java` | `TAB_DEVICEINFO = 0`, `TAB_DISPLAY = 1` | `MainActivity` |

Values produced by `AppManager`:

- Device list (from `android.os.Build`): Product (`PRODUCT`), Device
  (`DEVICE`), Board (`BOARD`), Manufacturer (`MANUFACTURER`), Brand (`BRAND`),
  Model (`MODEL`).
- Display list: Display (`Build.DISPLAY`, which is the build display id, not a
  screen property), Width Pixels, Height Pixels, Density, DensityDpi, Scaled
  Density, x dpi, y dpi (from `DisplayMetrics`), and the constants Default/Low/
  Medium/High Density (`DisplayMetrics.DENSITY_*`), which are the same on every
  device.
- Share text (`getAllInfos()`): `Device` and `Display` headings followed by
  tab-indented `name: value` lines.

Issues:

- Labels are hardcoded English strings inside the data holder.
- Numbers are converted with `"" + value` without formatting or units.
- Raw platform values, display labels, and share formatting live in one class.
- `Build.DISPLAY` appears under Display although it is build metadata.

## Target Model Design

| Layer | Target naming | Owns | Must not contain |
|---|---|---|---|
| Platform/data | Data source return types, or the domain models directly when the mapping is 1:1 | Raw values from `Build`, window metrics, `Configuration` | Labels, formatting, UI types |
| Domain | `DeviceInfo`, `DisplayInfo` (Kotlin data classes, non-null fields) | Typed values: strings for build fields; `Int` pixels and dpi; `Float` density, scaled density, xdpi, ydpi | Android types, resource ids, formatted strings |
| UI | `InfoRowUiModel(@StringRes labelRes: Int, value: String)`, `InfoListUiState` | Label resource ids and display-ready values | Platform calls |

Example target domain models:

```kotlin
data class DeviceInfo(
    val product: String,
    val device: String,
    val board: String,
    val manufacturer: String,
    val brand: String,
    val model: String,
    val buildDisplayId: String,
)

data class DisplayInfo(
    val widthPx: Int,
    val heightPx: Int,
    val density: Float,
    val densityDpi: Int,
    val scaledDensity: Float,
    val xdpi: Float,
    val ydpi: Float,
)
```

Mapping:

- `BuildInfoDataSource` -> `DeviceInfo`; `DisplayInfoDataSource` ->
  `DisplayInfo`.
- `DeviceInfo`/`DisplayInfo` -> `List<InfoRowUiModel>` in a pure mapper in
  `ui` (unit tested).
- `DeviceInfo` + `DisplayInfo` -> share text in `ShareTextFormatter` (unit
  tested). Section headings and labels are localized.
- Replace `Constant.Tab` int ids with an enum such as
  `MainTab { DEVICE, DISPLAY }` held in `MainUiState`.
- Replace `SlidingTab` mutable selection with state derived from
  `MainUiState.selectedTab`.

## Gaps & Recommendations

### 1. One untyped model for everything

- Current: `Item(name, value)` carries label and stringified value.
- Target: Typed domain models plus `InfoRowUiModel` with a string resource
  label.
- Priority: High

### 2. Hardcoded labels and headings

- Current: English literals in `AppManager` and `MainActivity`.
- Target: `strings.xml` resources referenced by the mapper/formatter.
- Priority: Medium

### 3. Constant density rows

- Current: Default/Low/Medium/High Density rows show framework constants that
  never vary by device.
- Target: Owner decision: remove them, or replace them with the device's
  density bucket name (for example `xxhdpi`) derived from `densityDpi`.
- Priority: Low

### 4. `Build.DISPLAY` is listed as a display property

- Current: Shown first in the Display tab.
- Target: Move it to the Device tab as "Build ID" (owner decision).
- Priority: Low
