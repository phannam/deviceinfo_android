# 002 - Settings, Theme, and Tab Manager

- Status: In progress. Phases 1-3 implemented (pending owner build);
  Phases 4-5 open.
- Owner: @phannam
- Spec author: planning agent
- Builds on: `docs/requirements/001-device-diagnostics.md` (done)
- Implements: ADR-TARGET-007 (amended), ADR-TARGET-009 (SharedPreferences)
- Build policy: the owner runs builds and reports errors. Agents implementing
  a phase must not run Gradle unless the owner approves it for that phase.
- References: owner screenshots in
  `/Users/namphan/Working/Screenshots/deviceInfo/settings/` (DevInfo app:
  Settings page, Dark mode dialog, Primary/Accent color dialogs, overflow
  menu, Tab Manager bottom sheet).

## 1. Goal

The app works but looks plain. This spec:

- refreshes the UI with a new brand color and a card-based look in light
  and dark mode;
- adds a Settings page (dark mode, primary/accent color, temperature unit,
  Tab Manager, about, policy links);
- adds a Tab Manager that lets the user turn tabs on/off and reorder them.

## 2. Owner Decisions (2026-11-15)

| Topic | Decision |
|---|---|
| Brand colors | `colorPrimary #31C7D6`, `colorPrimaryDark #28A6B3`, `colorAccent #2BB0C1` |
| Dark mode | Off / On / System, chosen in Settings |
| Color picker | User can change primary and accent from a fixed palette; "Recommend" restores the brand colors |
| Settings items | Dark mode, Primary color, Accent color, Tab Manager, Temperature unit, About card, Privacy Policy + Terms of Service links |
| Tab Manager | Toggle on/off + drag to reorder, Reset and Save (bottom sheet, like the screenshot) |
| Share report | Always contains all six sections in the default order, even for hidden tabs |
| UI refresh | Yes: gray background + rounded cards, toolbar with app icon, gear action, overflow menu |
| Settings storage | SharedPreferences (owner confirmed; no DataStore) |
| Code naming | Use `Config` instead of `Settings` in Kotlin class, file, and package names (`AppConfig`, `AppConfigRepository`, `feature/config/ConfigActivity`); user-visible text still says "Settings" |
| Contact | Feedback email and policy URLs will be provided later; use placeholders |

## 3. Non-Goals

- PRO / in-app purchase, ads, "More Apps" section, PRO badges.
- Floating monitor, widgets, notifications, new-app analysis, distance unit,
  export to file, Rate, Feedback (Feedback is planned once the owner gives
  the email; not in this spec).
- Free color wheel or arbitrary hex input; Material You dynamic color.
- New launcher icon (the About card uses the existing `@mipmap/ic_launcher`).
- Jetpack Compose, DataStore, Room, Jetpack Navigation.

## 4. Feature Spec

### 4.1 Theme and colors

- Base theme stays `Theme.Material3.DayNight.NoActionBar`.
- Brand palette (`values/colors.xml`):

  | Color | Light | Dark (`values-night`) |
  |---|---|---|
  | `brand_primary` | `#31C7D6` | `#31C7D6` |
  | `brand_primary_dark` | `#28A6B3` | `#28A6B3` |
  | `brand_accent` | `#2BB0C1` | `#2BB0C1` |
  | `brand_on_primary` | `#00363C` | `#00363C` |

  `#31C7D6` with white text is 2.1:1, so `colorOnPrimary` is dark
  (`#00363C`, at least 4.5:1). Containers use lighter (light mode) or darker
  (dark mode) tones of the same hue.
- Theme attribute mapping:

  | Owner color | M3 attribute | Used by |
  |---|---|---|
  | Primary | `colorPrimary`, `colorOnPrimary`, `colorPrimaryContainer`, `colorOnPrimaryContainer` | switches, buttons, radio/checkbox, toolbar icons, selected tab text |
  | Primary dark | `colorPrimaryDark` (legacy attr) | kept for AppCompat widgets; no visible effect with edge-to-edge |
  | Accent | `colorSecondary`, `colorOnSecondary`, `colorSecondaryContainer`, `colorOnSecondaryContainer` | tab indicator, row progress bars, footer links |

- Surfaces: the M3 baseline neutrals are purple-tinted, so the theme
  overrides every neutral attribute with gray `neutral_*` colors (`values`
  and `values-night/colors.xml`). Screen background
  `?android:attr/colorBackground` (`#F1F3F4` light, `#0F1113` dark, equal to
  `colorSurfaceContainer` in light mode); cards `?attr/colorSurface`
  (`#FFFFFF` / `#1C1F22`) with 16 dp corners, no stroke, 0 dp elevation
  (`Widget.DeviceInfo.Card`, the theme's `materialCardViewStyle`).
- Palette for the color picker (`AppColor` enum, 14 entries, in order):
  `BRAND, RED, PINK, PURPLE, DEEP_PURPLE, INDIGO, BLUE, TEAL, GREEN,
  LIGHT_GREEN, AMBER, ORANGE, DEEP_ORANGE, BLUE_GREY`.
  - `BRAND` means `brand_primary` for primary and `brand_accent` for accent.
  - Other entries use Material 2 palette tones: 600 in light mode, 300 in
    dark mode. On-color is black or white, whichever gives at least 4.5:1.
    Container is tone 100 in light mode and 800 in dark mode.
  - Resources: `palette_<name>`, `palette_<name>_on`,
    `palette_<name>_container`, `palette_<name>_on_container` in `values`
    and `values-night`. Overlays:
    `ThemeOverlay.DeviceInfo.Primary.<Name>` (sets the four primary attrs)
    and `ThemeOverlay.DeviceInfo.Accent.<Name>` (sets the four secondary
    attrs).
- Applying the theme:
  - Night mode: `AppCompatDelegate.setDefaultNightMode()` in
    `DeviceInfoApplication.onCreate()` and on every change (AppCompat
    recreates activities).
  - Colors: `DeviceInfoApplication` registers
    `ActivityLifecycleCallbacks.onActivityCreated` and calls
    `activity.theme.applyStyle(overlay, true)` for primary, then accent.
    This runs inside `super.onCreate()`, before `setContentView()`.
  - When the colors change, open activities must recreate:
    `ConfigActivity` calls `recreate()`. `MainActivity` remembers the
    colors it was created with; in `onStart` it calls `recreate()` if they
    differ.

### 4.2 Main screen (UI refresh)

- Toolbar (`MaterialToolbar` on the screen background, no elevation, no
  lift on scroll):
  - app icon (32 dp, rounded) and title `Device Info`;
  - action `Settings` (gear icon, `showAsAction="always"`). It stays
    `android:visible="false"` until Phase 3 adds the page;
  - overflow menu: `Share device report`, `Tab Manager`. The share icon
    action is removed from the toolbar.
- `TabLayout` (`Widget.DeviceInfo.TabLayout`): scrollable, transparent
  background, accent indicator, selected
  text `?attr/colorPrimary`, unselected `?attr/colorOnSurfaceVariant`.
- Info lists (`fragment_info_list.xml`, `item_info_row.xml`):
  - background `?android:attr/colorBackground`, 16 dp horizontal padding;
  - rows form one rounded card group: the row background depends on its
    position (`single`, `top`, `middle`, `bottom` drawables with 16 dp outer
    corners). Hairline dividers between rows, inset 16 dp;
  - label `textAppearanceBodyMedium` + `colorOnSurfaceVariant`, value
    `textAppearanceBodyLarge` + `colorOnSurface`;
  - progress indicator: rounded track, `indicatorColor ?attr/colorSecondary`.
  - `InfoRowAdapter` maps the position with `RowPosition.of(index, count)`.
    When the list size or the first/last label changes, it calls
    `notifyItemRangeChanged` with a position payload that only rebinds the
    background and divider.
- Screen Test tab: description in a card; `Start test` is a filled button;
  results use the same card group as other tabs.
- Edge-to-edge insets behave as today; lists keep bottom inset padding.
  `View.applySystemBarPadding()` adds the insets to the layout padding
  instead of replacing it.

### 4.3 Settings page

`ConfigActivity` (not exported, portrait), opened from the toolbar gear.
Layout: `NestedScrollView` on `?android:attr/colorBackground` (same as the
main screen, see 4.1), cards as in the screenshots. Up arrow returns to
`MainActivity`. Before Phase 4 the color tiles show the current colors but
are not clickable; the Tab Manager row is added in Phase 5.

Card 1 - Appearance and preferences:

1. Top row with three equal tiles (icon/swatch above, label below):
   - Dark mode: moon icon + current value (`Off` / `On` / `System`), label
     `Dark mode`. Tap opens a single-choice dialog with Off, On, System
     (default System).
   - Primary color: 40 dp circle filled with the current primary, label
     `Primary color`. Tap opens the color dialog (4.4).
   - Accent color: same for accent.
2. Divider.
3. Row `Tab Manager` (icon, title, subtitle `Show, hide, and sort tabs`).
   Opens the Tab Manager sheet (4.5).
4. Row `Temperature unit` (thermometer icon, subtitle = current value
   `Celsius` / `Fahrenheit`). Tap opens a single-choice dialog.

Card 2 - About: app icon (64 dp), `Device Info - v<versionName>`, and
slogan `Know your device, inside and out`.

Footer (no card): `Privacy Policy | Terms of Service` text links in
`?attr/colorSecondary`. Each opens `Intent.ACTION_VIEW` with the URL from
non-translatable strings `url_privacy_policy` and `url_terms_of_service`.
If no activity can handle the intent, show a Snackbar `No browser found`.
Placeholder URLs are `https://example.com/privacy` and
`https://example.com/terms` until the owner provides real ones.

Temperature unit effect:

- `TemperatureUnit { CELSIUS, FAHRENHEIT }`, default `CELSIUS`.
- `BatteryRowMapper.map(info, unit)` formats °F as `c * 9 / 5 + 32` with
  1 decimal (`value_temperature_fahrenheit`, `%1$.1f °F`).
- `BatteryViewModel` combines battery info with the unit;
  `MainViewModel.onShareClicked` uses the current unit.

### 4.4 Color dialog

- `MaterialAlertDialogBuilder` with a custom view:
  - header strip filled with the color currently picked in the dialog,
    showing the title (`Primary color` / `Accent color`) in its on-color;
  - one horizontal row of 14 swatches (`AppColor` order), each a 40 dp
    circle; the selected swatch is larger and has a check mark. The row
    scrolls horizontally on narrow screens.
- Buttons: `Recommend` (neutral) picks `BRAND` without closing; `OK`
  (positive) saves and closes. Back or tapping outside discards.
- Swatches show the tone for the current night mode (resolved from
  `palette_*` resources).
- Each swatch has a content description with the color name
  (`color_<name>` strings).

### 4.5 Tab Manager

- `TabManagerBottomSheet` (`BottomSheetDialogFragment`), opened from the
  main overflow menu and from Settings. It opens fully expanded
  (`skipCollapsed`), with 28 dp top corners.
- Header: `Tab Manager`, subtitle `Long press and drag to sort`.
- List: one card per tab (drag handle icon, tab title, `MaterialSwitch`).
  `ItemTouchHelper` drags vertically: start on handle touch-down or long
  press on the card.
- Accessibility: each card has custom actions `Move up` / `Move down`
  (`ViewCompat.addAccessibilityAction`); the switch carries the tab name.
- Bottom bar: `Reset` (outlined) restores the default order with all tabs on
  in the draft only; `Save` (filled) saves and dismisses. Dismiss without
  Save discards the draft.
- At least one tab must stay on: the switch of the last enabled tab is
  disabled.
- After Save, `MainActivity` updates its tabs without recreating:
  - the selected tab stays selected if it is still visible, otherwise the
    first visible tab is selected;
  - Screen Test results and live tabs keep working.
- Default: all six tabs on, in `MainTab` order.
- The share report ignores this setting (all six sections, default order).

## 5. Architecture

### 5.1 New packages and files

```text
vn.npstudio.deviceinfo
├── domain/
│   ├── model/      AppConfig(darkMode, primaryColor, accentColor,
│   │               temperatureUnit), DarkMode {OFF, ON, SYSTEM},
│   │               AppColor, TemperatureUnit, TabId,
│   │               TabPreferences(order: List<TabId>, hidden: Set<TabId>)
│   └── repository/ AppConfigRepository
├── data/
│   ├── source/     AppConfigLocalDataSource (+ Impl, SharedPreferences),
│   │               AppConfigParser (pure: tab list / enum parsing)
│   └── repository/ AppConfigRepositoryImpl
├── ui/
│   ├── theme/      ThemeOverlays (AppColor -> @StyleRes per role, swatch
│   │               color), NightModeMapper (DarkMode -> AppCompat mode),
│   │               ThemeApplier (ActivityLifecycleCallbacks)
│   └── list/       InfoRowAdapter (position backgrounds), RowPosition
└── feature/
    ├── main/       MainActivity (menu, dynamic tabs), MainTab (+ TabId map),
    │               MainTabAdapter (dynamic list), MainViewModel (visibleTabs)
    ├── config/     ConfigActivity, ConfigViewModel, ConfigUiState,
    │               ColorPickerDialog, SwatchAdapter
    └── tabmanager/ TabManagerBottomSheet, TabManagerViewModel,
                    TabManagerUiState, TabManagerAdapter
```

### 5.2 Settings storage

- ADR-TARGET-009 allows SharedPreferences. The theme must be known
  synchronously before `setContentView()`, so SharedPreferences fits better
  than DataStore and needs no new dependency.
- File `app_config` (`Context.MODE_PRIVATE`). Keys and values:

  | Key | Value | Default |
  |---|---|---|
  | `dark_mode` | `DarkMode.name` | `SYSTEM` |
  | `primary_color` | `AppColor.name` | `BRAND` |
  | `accent_color` | `AppColor.name` | `BRAND` |
  | `temperature_unit` | `TemperatureUnit.name` | `CELSIUS` |
  | `tab_order` | comma-joined `TabId.name` | all, `MainTab` order |
  | `tab_hidden` | comma-joined `TabId.name` | empty |

- Parsing (`AppConfigParser`, unit tested):
  - unknown names are dropped and duplicates are removed;
  - missing tabs are appended as visible (new tabs in future versions);
  - if every tab ends up hidden, all are shown.
- `AppConfigLocalDataSource` reads and writes with `edit { apply() }`.
  `AppConfigRepositoryImpl` (`@Singleton`) loads once at injection into
  `MutableStateFlow`s and exposes:
  - `config: StateFlow<AppConfig>`
  - `tabs: StateFlow<TabPreferences>`
  - `setDarkMode`, `setPrimaryColor`, `setAccentColor`,
    `setTemperatureUnit`, `saveTabs(TabPreferences)`.
  Writes update the StateFlow first, then persist.
- The first read happens on the main thread during `Application.onCreate`
  (one small XML file). This is an accepted StrictMode disk read; record it
  in `docs/DECISIONS.md`.
- `allowBackup="true"` stays, so settings are restored with Auto Backup.

### 5.3 Flows

- `ConfigViewModel`: maps `config` to `ConfigUiState` (value labels as
  `UiText`, swatches as `AppColor`). The Activity opens dialogs on tap and
  calls ViewModel setters; no dialog effects are needed.
- `TabManagerViewModel`: draft `MutableStateFlow<List<TabManagerItem>>`
  initialised from `tabs.value`; `onMoved(from, to)`, `onToggled(id, on)`,
  `onReset()`, `onSave()` -> `UiEffect.Dismiss`.
- `MainViewModel.visibleTabs: StateFlow<List<MainTab>>` from
  `appConfigRepository.tabs`. `selectedTab` stays in `SavedStateHandle`.
- `MainTabAdapter.submit(tabs)`: overrides `getItemId` (`tab.ordinal`) and
  `containsItem` so `FragmentStateAdapter` keeps fragments of tabs that
  remain. `TabLayoutMediator` uses `autoRefresh = true`.

### 5.4 ADR changes

- Amend ADR-TARGET-007: tabs stay in `MainActivity`. Settings is a
  dedicated `ConfigActivity` (simple, no NavGraph); Tab Manager is a
  bottom sheet in the calling Activity.
- Record under ADR-TARGET-009: settings use SharedPreferences, with a
  synchronous first read for theming.
- New ADR: theme colors are a fixed palette applied with theme overlays (no
  runtime color tinting of individual views, no dynamic color).

### 5.5 Manifest and dependencies

- Register `feature.config.ConfigActivity` (not exported, portrait).
- No new permissions and no new libraries. `MaterialSwitch`,
  `BottomSheetDialogFragment`, and `MaterialAlertDialogBuilder` come from
  `material`; `ItemTouchHelper` from `recyclerview`.
- Opening URLs needs no `<queries>`: catch `ActivityNotFoundException`.

## 6. Delivery Phases

Each phase is one agent task and ends in a buildable app. After each phase
the agent lists the Gradle command for the owner:
`./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.

### Phase 1 - Settings storage and theme foundation (implemented)

- Domain models, `AppConfigRepository`, `AppConfigLocalDataSource`,
  `AppConfigParser`, DI bindings.
- Brand colors (4.1) in `values` and `values-night`; palette colors and the
  28 theme overlays; `ThemeApplier`; night mode applied at startup.
- No visible settings UI yet; the app shows the new brand colors.
- Tests: `AppConfigParserTest`, `AppConfigRepositoryImplTest` (fake data
  source).

### Phase 2 - Main screen UI refresh (implemented)

- Toolbar (icon, title, hidden gear until Phase 3, overflow with
  `Share device report`; Tab Manager item added in Phase 5).
- TabLayout colors, card-group rows, Screen Test tab styling.
- Check light and dark mode on device for all six tabs.
- Tests: `RowPositionTest`.

### Phase 3 - Settings page (implemented)

- `ConfigActivity`, `ConfigViewModel`, layout (4.3), dark mode dialog,
  temperature unit dialog (with battery and report changes), About card,
  policy links. The toolbar gear becomes visible and opens `ConfigActivity`.
- Tests: `ConfigViewModelTest`, `BatteryRowMapperTest` (°F), updated
  `BatteryViewModelTest`, `MainViewModelTest` (report uses unit).

### Phase 4 - Color picker

- `ColorPickerDialog` (4.4), swatches, Recommend/OK, recreate logic in
  `ConfigActivity` and `MainActivity`.
- Tests: `ConfigViewModelTest` color cases; `ThemeOverlays` covers every
  `AppColor` for both roles (test iterates all entries).

### Phase 5 - Tab Manager and docs

- `TabManagerBottomSheet`, ViewModel, adapter, drag and accessibility
  actions; dynamic tabs in `MainActivity`; entries in the overflow menu and
  Settings.
- Tests: `TabManagerViewModelTest` (move, toggle, last tab locked, reset,
  save), `MainViewModelTest` (visible tabs, selected tab fallback).
- Docs sweep (section 8).

## 7. Testing

- JVM tests as listed per phase; follow `docs/TESTING.md` patterns
  (`MainDispatcherRule`, Turbine, fakes in `testing/Fakes.kt`).
- Manual checks (owner, on device):
  - Light/dark: switch Off/On/System; every screen readable; no white flash
    when opening Settings in dark mode.
  - Colors: pick each role; Main screen updates after going back; colors
    survive app restart and process death.
  - Tab Manager: hide/show, reorder, Reset, Save, dismiss without Save,
    selected tab fallback; settings survive process death.
  - Temperature unit: Battery tab and shared report follow the setting.
  - TalkBack: Tab Manager move actions and switches; swatch names.
  - Policy links open the browser.

## 8. Strings and Docs

- New prefixes: `settings_`, `tab_manager_`, `color_` (palette names),
  `dark_mode_`, `temperature_unit_`, `url_` (non-translatable).
- Every new string also in `values-vi/strings.xml` with the same format
  specifiers. Menu: `action_settings`, `action_share_report`,
  `action_tab_manager`.
- Docs to update in the phase that changes them: `AGENTS.md` (capabilities,
  current state, high-risk areas: theme overlays, settings keys),
  `docs/ARCHITECTURE.md`, `docs/MODULE_MAP.md`, `docs/DATA_MODELS.md`
  (settings models and keys), `docs/DECISIONS.md` (5.4),
  `docs/CONVENTIONS.md` (string prefixes, palette naming),
  `docs/TESTING.md`.

## 9. Risks and Open Questions

- Owner primary `#31C7D6` has low contrast as text on white (2.1:1).
  Primary text on light surfaces (selected tab label) may be hard to read;
  if the owner agrees, use `#28A6B3` or a darker tone for text only.
- `colorPrimaryDark` has no M3 role; it is kept as the legacy attribute.
- Recreating `MainActivity` after a color change resets scroll positions;
  the selected tab is kept by `SavedStateHandle`.
- `FragmentStateAdapter` with a changing list is error-prone; stable ids
  must be `tab.ordinal`, never positions.
- Feedback email, Privacy Policy, and Terms URLs are placeholders until the
  owner provides them.
