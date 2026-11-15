# AGENTS.md

Read this file before making any change in this Android repository. This is
the contract between the owner and any AI coding agent working on this
modernization.

## Project Summary

DeviceInfo is a small, offline Android app that shows read-only information
about the device it runs on.

The app lets users:

- View general device information from `android.os.Build` (product, device,
  board, manufacturer, brand, model).
- View display information from `DisplayMetrics` (resolution, density, dpi,
  scaled density, and the Android density constants).
- Share all collected information as plain text through an email client.

The app has no network access, no account, no database, and no persisted user
data.

Current state:

- Single `:app` module, 100% Kotlin, about 20 source files, last functionally
  changed in 2015. The build was modernized in 2026 (AGP 9.4.1, Gradle 9.9.1,
  Kotlin DSL, version catalog, AndroidX, namespace `vn.npstudio.deviceinfo`)
  and the source was converted 1:1 from Java to Kotlin with ViewBinding.
- One `MainActivity` hosts two tabs (Device, Display) through a custom tab
  strip and manual `FragmentTransaction`s.
- Data comes from a static `AppManager` singleton created in
  `DeviceApplication.onCreate()`. It reads every value once at startup.
- UI uses XML layouts with ViewBinding, a `ListView`, and an `ArrayAdapter`.
- No ViewModel, no DI, no coroutines.

Modernization goal:

- Keep XML layouts. Do not move to Compose.
- All code is Kotlin. Do not add Java.
- Move to strict MVVM with Hilt, `StateFlow`, coroutines, and ViewBinding.
- Read device and display values through platform data sources behind a
  repository instead of the static `AppManager`.
- Refactor gradually. Do not migrate the entire app at once.

## Tech Stack

| Area | Current | Target |
| --- | --- | --- |
| Language | Kotlin (AGP built-in), JVM target 17 | Kotlin for new and refactored code (AGP built-in Kotlin) |
| UI | XML layouts with ViewBinding, `ListView` + `ArrayAdapter`, vendored tab views | XML + ViewBinding only; `RecyclerView` for lists; no Compose |
| Architecture | Activity/Fragment + static `AppManager` singleton (no ViewModel) | Strict MVVM: UI -> ViewModel -> Repository -> DataSource |
| State | Mutable fields in Activity, fragments, and `AppManager` | Read-only `StateFlow` UI state; `SharedFlow`/`Channel` for one-time effects |
| Storage | None | None. Add SharedPreferences only for lightweight settings if a feature needs it |
| Networking | None | None. If ever added: Retrofit + coroutines (see `docs/DECISIONS.md`) |
| Async | None; all work runs synchronously on the main thread at startup | Coroutines; UI-triggered work starts in `viewModelScope` |
| DI | Static singleton (`AppManager.getInstance()`) | Hilt only |
| Navigation | Single Activity, manual `FragmentTransaction.replace` per tab | Single Activity; FragmentManager-owned tab destinations; Jetpack Navigation only if more screens are added |
| SDK | minSdk 26, compileSdk/targetSdk 37 (`gradle/libs.versions.toml`) | Same |
| Tests | Placeholder JUnit and AndroidX instrumented tests only | Unit tests for data sources, repository, formatter, and ViewModels |

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
- Do not migrate the entire app at once.
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
- No new static singletons or service locators. Do not add new callers of
  `AppManager.getInstance()`.
- No new `GlobalScope`, ad-hoc `MainScope`, or runtime `runBlocking`.
- No new manual ViewModel factories when Hilt can provide the ViewModel.
- No `LiveData`, `MutableLiveData`, or `SingleLiveEvent` for ViewModel state.
- Do not migrate the entire app at once.

## XML Layout And Dimen Rules

When translating Zeplin, Figma, screenshots, or mockups into Android XML
layouts, do not map design pixels one-to-one into Android `dp` by default. Use
mocks to understand hierarchy, spacing intent, density, and emphasis, then
implement responsive Android layout behavior.

- Prefer `wrap_content` for dynamic text, content rows, and containers.
- Use fixed heights only when the element is truly fixed-size.
- Prefer `minHeight` and `minWidth` for row/control touch targets so content can
  grow with localization, font scale, or dynamic values.
- `res/values/dimens.xml` currently holds only unused template dimens
  (`app_bar_height`, `fab_margin`, `text_margin`), and layouts use inline `dp`
  values. When a task touches layout spacing, add semantic spacing dimens with
  these names and reuse them: `xTinyGap`, `tinyGap`, `xSmallGap`, `smallGap`,
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

- `AppManager`: static singleton created in `DeviceApplication.onCreate()`. It
  holds the application `Context` and computes all values once. Every screen and
  the share action read from it.
- `TabActivity` / `MainActivity` tab switching: every tab change calls
  `FragmentTransaction.replace(...)` plus `addToBackStack(...)`, and manually
  calls `onPause()`/`onStop()` on the previous fragment. `initTabs()` runs again
  on recreation and pushes another transaction.
- Vendored tab views in `views/` (`SlidingTabLayout` and `SlidingTabStrip` are
  adapted AOSP Apache-2.0 samples; `SlidingFragmentTab`, `SlidingTab`, and
  `SlidingTabRenderer` are app-owned). `SlidingTabLayout` is not inflated
  anywhere, but `SlidingTabStrip` and `SlidingFragmentTab` use its
  `TabColorizer`.
- Share action in `MainActivity`: queries email apps with a `mailto:` intent
  that contains the owner's email address, then sends `ACTION_SEND_MULTIPLE`.
  Package visibility on API 30+ affects this query (see
  `docs/ARCHITECTURE.md`).
- `applicationId` is `vn.npstudio.deviceinfo`. Do not change it; it is the
  installed app identity.

## When Uncertain

Do not guess silently.

State the ambiguity.

Propose two options.

Ask the owner to choose.
