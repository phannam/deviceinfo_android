# AGENTS.md

Read this file before making any change in this Android repository. This is
the contract between the owner and any AI coding agent working on this
modernization.

## Project Summary

DeviceInfo is a small, offline Android app that shows read-only information
about the device it runs on.

The app lets users:

- View device and OS information (`android.os.Build`, kernel, Java VM,
  uptime).
- View display information (physical resolution, app window size, density,
  dpi, estimated size, refresh rates, HDR, wide color gamut, font scale,
  cutout).
- View CPU information (SoC, ABIs, clusters, governor, thermal status) and
  live per-core frequencies.
- View live battery information (level, status, power source, health,
  temperature, voltage, current, charge counter, estimated capacity).
- View RAM (total, available, low-memory threshold) refreshed every 2 s and
  internal storage (total, free) refreshed on resume.
- Run a full-screen Screen Test (solid colors, gray, gradients, 1 px
  checkerboard) and record dead pixels, lines, or burn-in for the session.
- Share a plain-text report of every tab (header with app version, time,
  and device; Screen Test shows Not tested until a test is saved) through
  the system share sheet.

The app has no network access, no account, no database, and no persisted user
data.

Current state (spec 001 `docs/requirements/001-device-diagnostics.md` done):

- Single `:app` module, 100% Kotlin. AGP 9.4.1, Gradle 9.9.1, Kotlin DSL,
  version catalog, namespace `vn.npstudio.deviceinfo`.
- Strict MVVM with Hilt (KSP), coroutines, `StateFlow`, ViewBinding.
- `MainActivity` hosts Device, CPU, Battery, Display, Memory, and Screen Test tabs with Material `TabLayout` +
  `ViewPager2`; lists use `RecyclerView` + `ListAdapter`.
- Strings in English (`values/`) and Vietnamese (`values-vi/`).
- In progress: spec 002 `docs/requirements/002-settings-theme-tab-manager.md`
  (Settings page, light/dark mode, color picker, Tab Manager). Phase 1 done:
  `AppConfigRepository` (SharedPreferences), brand colors `#31C7D6` /
  `#2BB0C1`, palette theme overlays applied by `ThemeApplier`. Phase 2 done:
  neutral gray surfaces, toolbar with app icon, rows drawn as rounded card
  groups (`RowPosition`). Phase 3 done: `ConfigActivity` (Settings page:
  dark mode, temperature unit, About, policy links); Battery and the report
  follow the temperature unit.

Modernization goal:

- Keep XML layouts. Do not move to Compose.
- All code is Kotlin. Do not add Java.
- Move to strict MVVM with Hilt, `StateFlow`, coroutines, and ViewBinding.
- Spec 001 diagnostics are complete; new features need a new spec in
  `docs/requirements/`.
- Use `Config` instead of `Settings` in Kotlin class, file, and package
  names (`AppConfig`, `AppConfigRepository`, `feature/config/`). Files named
  `*Settings*` are blocked by the organization content policy.

## Tech Stack

| Area | Current | Target |
| --- | --- | --- |
| Language | Kotlin (AGP built-in), JVM target 17 | Same |
| UI | XML + ViewBinding, Material 3, `TabLayout` + `ViewPager2`, `RecyclerView`, `androidx.window` (window metrics) | Same; no Compose |
| Architecture | Strict MVVM: UI -> ViewModel -> Repository -> DataSource | Same |
| State | Read-only `StateFlow` UI state; `Channel` for one-time effects | Same |
| Storage | SharedPreferences file `app_config` via `AppConfigLocalDataSource` (theme, temperature unit, tabs) | Same; no DataStore, no Room |
| Networking | None | None. If ever added: Retrofit + coroutines (see `docs/DECISIONS.md`) |
| Async | Coroutines; injected IO dispatcher; `viewModelScope` | Same |
| DI | Hilt (KSP) | Same |
| Navigation | Single Activity, `FragmentStateAdapter` tabs | Same; dedicated Activities only for full-screen tests and the config page |
| SDK | minSdk 26, compileSdk/targetSdk 37 (`gradle/libs.versions.toml`) | Same |
| Tests | JVM unit tests for formatter, mappers, renderer, ViewModels | Add tests with every new parser, repository, mapper, and ViewModel |

## Doc Index

Read the docs in this order for modernization work:

1. `docs/DECISIONS.md`: accepted current and target technology decisions.
2. `docs/CONVENTIONS.md`: naming, XML, resource, ViewBinding, state, coroutine,
   DI, string, and color conventions.
3. `docs/ARCHITECTURE.md`: current architecture, target MVVM rules, data flow,
   state, navigation, and architecture gaps.
4. `docs/MODULE_MAP.md`: current module/package layout, target package layout,
   dependency rules, and file placement rules.
5. `docs/DATA_MODELS.md`: current and target models and mapping.
6. `docs/TESTING.md`: current test coverage, target testing strategy, commands,
   and manual checks.
7. `docs/requirements/NNN-*.md`: feature specs. When a task implements a
   spec, the spec is the source of truth for scope, data sources, phases, and
   acceptance criteria. Work one phase at a time and update the spec status.

The app has no network layer, so there is no `docs/API_CONTRACTS.md`. Create it
in the same task that introduces any HTTP API.

## Priority Rule

Follow instructions in this order:

1. `AGENTS.md`
2. `docs/CONVENTIONS.md`
3. All other files in `docs/`
4. Existing source code patterns

If existing code contradicts a modernization doc, follow the doc for new and
refactored code.

If source code proves that a doc is factually stale, update the doc as part of
the task or stop and ask for direction.

If two docs conflict, follow `AGENTS.md` first, then `docs/CONVENTIONS.md`.
State the conflict in the final summary.

## Refactor Rules

- Leave code better than you found it.
- Preserve user changes. Do not revert unrelated modified files.
- Keep changes scoped to the current task.
- Do not rewrite files outside the current task scope.
- New code must follow target conventions even if surrounding code does not.
- All app and test code is Kotlin. Do not add Java files.
- Never mix old pattern and new pattern in the same file. If the file cannot be
  made internally consistent within the task scope, stop and ask for direction.
- Never introduce Compose under any circumstance.
- Add abstractions only when they clarify a real boundary or remove repeated
  behavior. This app is small; avoid layers that only forward calls.
- Keep Activities and Fragments UI-only.
- Route platform access (`Build`, `DisplayMetrics`, `WindowManager`,
  `PackageManager`) through data sources behind a repository.
- Update docs when architecture, module, data model, testing, or convention
  decisions change.

## Before You Write Any Code

Complete this checklist before editing production code:

- Read this `AGENTS.md`.
- Read `docs/DECISIONS.md`.
- Read `docs/CONVENTIONS.md`.
- Read any task-relevant docs from the doc index.
- Check `git status --short`.
- Identify the exact files in scope.
- Inspect the existing code path before deciding the implementation.
- Identify whether the touched files use an old pattern or a target pattern.
- Decide how the touched files will remain internally consistent.
- Decide the smallest useful non-building check. If a build or Gradle
  verification task is needed, ask the owner for explicit approval first.
- If the task touches XML layout or dimens, apply the XML layout and dimen rules
  in this file.
- If the task has ambiguity, state it and ask before coding.

## After You Write Code

Do not run a build by default. This is an owner restriction for every agent
and thread working in this repository:

- Do not run Android Studio builds or any Gradle task that compiles, assembles,
  bundles, installs, tests, or lints the app unless the owner explicitly
  authorizes that command for the current task. Test and lint tasks can trigger
  compilation, so they require the same approval.
- A command appearing in a doc, test plan, agent prompt, or list of approved
  shell prefixes is not authorization to run it. Do not delegate a build to
  another agent or thread to bypass this restriction.
- Use read-only/static checks that do not trigger a build when useful. For
  docs-only changes, use simple file checks.
- In the final response, state which build, lint, and test commands were not
  run because approval was not given. Do not claim regression verification
  passed when those checks were not run.

## Strict No-Nos

- No Compose anywhere.
- No `findViewById` in new app-owned layout code.
- No Java files.
- No RxJava, AsyncTask, or Volley.
- No hardcoded strings.
- No hardcoded colors.
- No logic in Activity or Fragment.
- No direct `Build`, `DisplayMetrics`, `WindowManager`, or `PackageManager`
  access from Activity, Fragment, adapter, custom view, or ViewModel in new or
  refactored code.
- No static singletons or service locators.
- No new `GlobalScope`, ad-hoc `MainScope`, or runtime `runBlocking`.
- No new manual ViewModel factories when Hilt can provide the ViewModel.
- No `LiveData`, `MutableLiveData`, or `SingleLiveEvent` for ViewModel state.

## XML Layout And Dimen Rules

When translating Zeplin, Figma, screenshots, or mockups into Android XML
layouts, do not map design pixels one-to-one into Android `dp` by default. Use
mocks to understand hierarchy, spacing intent, density, and emphasis, then
implement responsive Android layout behavior.

- Prefer `wrap_content` for dynamic text, content rows, and containers.
- Use fixed heights only when the element is truly fixed-size.
- Prefer `minHeight` and `minWidth` for row/control touch targets so content can
  grow with localization, font scale, or dynamic values.
- `res/values/dimens.xml` holds `tinyGap` (4dp), `smallGap` (12dp), and
  `mediumGap` (16dp). Reuse them; when new spacing is needed, add it with
  one of these names: `xTinyGap`, `tinyGap`, `xSmallGap`, `smallGap`,
  `mediumGap`, `xMediumGap`, `largeGap`, `xLargeGap`, and `icon_height`.
- Add other dimens only when they are semantic, reused, or needed for distinct
  component behavior.
- Avoid long lists of one-off mock-exact values.
- Keep icon-only controls at a 44dp minimum touch target while allowing the
  visual icon to be smaller through padding.
- Use fixed image dimensions only for intentionally fixed previews or assets.
- Check long localized strings, larger font scale, and small-width devices for
  clipping or overlap before considering the layout done. Device values such as
  `Build.DISPLAY` or long model names can be long; rows must not clip them.

## High-Risk Areas

Be extra careful around these areas:

- `InfoRowAdapter` identifies rows by `label` (`UiText`) in DiffUtil. Every
  row in one list must have a unique label. Do not add stable ids from
  `label.hashCode()`: different labels can collide and crash RecyclerView.
- `SysFsReader` is the only class that reads `/proc` and `/sys`; it never
  throws. Many files are blocked by SELinux on newer devices, so every sysfs
  value must have an unavailable path.
- CPU frequency polling, the battery receiver/polling, and RAM polling run only while
  their tab is subscribed (`WhileSubscribed(5000)`, collected at `RESUMED`).
  Do not collect them from a long-lived scope.
- `ScreenTestActivity` sets window brightness, keep-screen-on, hidden system
  bars, and cutout mode on its own window only. Do not change system
  settings (`WRITE_SETTINGS`) or apply these flags to `MainActivity`.
- Window-dependent values (`WindowSnapshot`) are measured in the UI
  (`DisplayFragment`, `MainActivity`); data sources must not hold an Activity.
- Battery current follows the API contract (signed µA). Some OEMs report mA
  or flip the sign; do not add guesses. Record such devices in spec 001
  section 10.
- Tab restoration: `MainActivity` restores the selected tab from
  `MainViewModel` before registering the page callback. Keep that order.
- `UiText.Resource` args are formatted with the string resource format; keep
  `values/` and `values-vi/` format specifiers identical.
- Hilt + KSP with AGP built-in Kotlin: do not apply
  `org.jetbrains.kotlin.android`, `kotlin-kapt`, or an explicit stdlib.
- Theme: `ThemeApplier` applies `ThemeOverlay.DeviceInfo.Primary.*` /
  `Accent.*` before views inflate and sets the night mode at startup.
  `AppConfigRepository` reads SharedPreferences synchronously for this. Keep
  every `AppColor` mapped in `ThemeOverlays`, `values/palette.xml`, and
  `values-night/palette.xml`. Stored config values are enum names; renaming
  an enum entry resets users to the default.
- `applicationId` is `vn.npstudio.deviceinfo`. Do not change it; it is the
  installed app identity.

## When Uncertain

Do not guess silently.

State the ambiguity.

Propose two options.

Ask the owner to choose.
