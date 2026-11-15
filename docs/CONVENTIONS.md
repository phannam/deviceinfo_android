# Conventions

## Current Conventions (inferred from codebase)

- Language and packages
  - All app and test code is Kotlin. Packages follow feature then layer
    (`feature.*`, `ui`, `domain`, `data`, `di`, `common`); see
    `docs/MODULE_MAP.md`.

- Class naming
  - `XxxActivity`, `XxxFragment`, `XxxViewModel`, `XxxRepository` +
    `XxxRepositoryImpl`, `XxxDataSource` + `XxxDataSourceImpl` (same file),
    `XxxRowMapper`, `ValueFormatter`.
  - Pure mappers/renderers are Kotlin `object`s.

- XML layouts
  - `activity_main.xml`, `fragment_info_list.xml` (shared by all list tabs),
    `item_info_row.xml`. Menu `menu_main.xml`.
  - IDs: `app_bar`, `toolbar`, `tab_layout`, `view_pager`, `info_list`,
    `loading_indicator`, `name_text`, `value_text`, `progress_indicator`,
    `action_share`.

- View access
  - ViewBinding everywhere. `InfoListFragment` uses `_binding` cleared in
    `onDestroyView` with a `checkNotNull` getter.

- Resources
  - Theme `Theme.DeviceInfo` (`Theme.Material3.DayNight.NoActionBar`) in
    `values/themes.xml`; brand and `neutral_*` surface colors in
    `values/colors.xml` and `values-night/colors.xml`. Widget styles
    (`Widget.DeviceInfo.*`) in `values/styles.xml`.
  - Screens use `?android:attr/colorBackground`; cards and row groups use
    `?attr/colorSurface` with `@dimen/cardCornerRadius`. Never hardcode
    colors in layouts.
  - Strings in `values/strings.xml` (English) and `values-vi/strings.xml`
    (Vietnamese) with the same keys; `app_name` is `translatable="false"`.
    Prefixes: `tab_*`, `label_<section>_*`, `value_*`, `report_*`,
    `common_*`, `action_*`.
  - Row dimens in `values/dimens.xml`. Vector icon `drawable/ic_share.xml`.

## Target Conventions (all new and refactored code must follow)

- Language
  - All app and test code is Kotlin. Do not add Java files.
  - Use idiomatic Kotlin: `val` by default, data classes for models, no
    `m`-prefixed fields, no `!!` unless the invariant is documented.

- Packages: by feature, then layer
  - Follow the package layout in `docs/MODULE_MAP.md`
    (`feature/device`, `feature/display`, `feature/main`, `data`, `domain`,
    `common`, `di`).

- File naming
  - Activities: `FeatureActivity.kt`
  - Fragments: `FeatureFragment.kt`
  - ViewModels: `FeatureViewModel.kt`
  - Repositories: `FeatureRepository.kt` for the interface and
    `FeatureRepositoryImpl.kt` for the implementation.
  - Platform data sources: `FeatureDataSource.kt` (for example
    `BuildInfoDataSource`, `DisplayInfoDataSource`).
  - UI state: `FeatureUiState.kt` when shared across more than one file.
  - Mappers/formatters: `FeatureMapper.kt` or `FeatureFormatter.kt`.
  - App settings code uses `Config`, never `Settings`, in class, file, and
    package names (`AppConfig`, `AppConfigRepository`, `feature/config/`).
    User-visible strings still say "Settings".
  - Theme overlays: `ThemeOverlay.DeviceInfo.<Role>.<Color>`; palette colors
    `palette_<color>`, `_on`, `_container`, `_on_container` in `values/` and
    `values-night/palette.xml`.

- XML layout naming: `activity_*`, `fragment_*`, `item_*`, `view_*`
  - Use `item_*` for RecyclerView rows and `view_*` for reusable includes,
    custom view content, and dialog content.
  - Do not add new `layout_*` names.

- Resource ID naming: semantic snake_case
  - Examples: `info_list`, `name_text`, `value_text`, `tab_title_text`,
    `tab_icon_image`, `fragment_container`, `action_share`.
  - Do not add new Hungarian prefixes (`tv_`, `lst_`, `img_`). Rename existing
    IDs only when the layout and all references are touched in the same task.

- ViewBinding: always, binding variable always named `binding`
  - Activities, Fragments, adapters, and custom views use ViewBinding.
  - Do not add `findViewById` for app layouts.
  - Fragments use a private nullable `_binding` backing field cleared in
    `onDestroyView`, and access the non-null `binding` getter.

- Fragment and tab restoration: let FragmentManager own instance identity
  - Do not cache fragment instances outside `FragmentManager`. Tabs are
    created only in `MainTabAdapter.createFragment` (`FragmentStateAdapter`).
  - On Activity recreation, restore the selected tab from `MainViewModel`
    with `setCurrentItem(..., false)` before registering the page callback.
  - Tab switches must not be added to the back stack; Back from the main
    screen exits the app.
  - Never call fragment lifecycle methods (`onPause`, `onStop`) manually.

- StateFlow for UI state, never expose MutableStateFlow
  - `private val _uiState = MutableStateFlow(...)` and
    `val uiState: StateFlow<...> = _uiState.asStateFlow()`.
  - Do not use `LiveData`, `MutableLiveData`, or `SingleLiveEvent`.
  - Use `SharedFlow` or `Channel` for one-time events such as showing a toast
    or launching the share chooser.
  - Use `vn.npstudio.deviceinfo.common.stateflow.UiEffect` for common
    effects (`ShowToast`, `ShowError`). Create a feature-owned effect only for
    feature-specific actions, such as `MainUiEffect.Share(sections)`.
  - Collect flows in UI with `Flow<T>.collectWithLifecycle(...)` from
    `vn.npstudio.deviceinfo.common.extension`. Pass the Activity from
    Activities and `viewLifecycleOwner` from Fragments.
  - ViewModels emit user-visible text as `UiText`
    (`vn.npstudio.deviceinfo.common.text`); UI resolves it with `Context`.
  - `UiEffect` does not exist yet. Create it in the first task that needs a
    common effect, and reuse it afterwards.

- UI state shape
  - Each screen has an explicit UI state with `loadingMode` and its data.
  - `LoadingMode` (`IDLE`, `INITIAL`, `REFRESH`) lives in
    `common.stateflow`. Add more values only when a screen needs them.
  - Info list screens use `InfoListUiState(loadingMode, rows)`.
  - Do not add ad-hoc nullable `isLoading`/`errorMessage` flags.

- Coroutines in viewModelScope only
  - UI-triggered work starts in `viewModelScope`.
  - Repositories and data sources expose `suspend` functions or `Flow`.
  - Inject a `CoroutineDispatcher` for off-main work. Add qualifiers only when
    the graph provides more than one dispatcher.
  - No `GlobalScope`, `MainScope`, `runBlocking`, `AsyncTask`, or `Handler`
    for data work.

- Hilt for all dependency injection
  - `@HiltAndroidApp` on the Application, `@AndroidEntryPoint` on Activities
    and Fragments that use injected dependencies or Hilt ViewModels.
  - `@HiltViewModel` with a single `@Inject constructor`. No secondary
    constructors for tests.
  - Bind repositories and data sources in Hilt modules under `di/` with
    `@Binds` or `@Provides`.
  - No new singletons via `getInstance()`, service locators, or manual
    ViewModel factories.

- AGP built-in Kotlin
  - Use the Kotlin support built into AGP 9. Do not apply
    `org.jetbrains.kotlin.android` or `kotlin-android`.
  - Do not add or pin an explicit Kotlin standard-library dependency.
  - Prefer KSP for annotation processing. If KAPT is unavoidable, apply
    `com.android.legacy-kapt` at the AGP version and declare processors with
    `kapt(...)`. Do not apply `org.jetbrains.kotlin.kapt` or `kotlin-kapt`.

- Dependencies and build scripts
  - Build scripts are Kotlin DSL (`*.gradle.kts`). Do not add Groovy scripts.
  - Declare every dependency, plugin, and SDK version in
    `gradle/libs.versions.toml` and reference it through `libs.*`.
  - Remove dependencies that become unused.

- Release optimization
  - Release uses the AGP 9.3+ DSL `optimization { enable = true }` (R8 code
    and resource optimization with default Android keep rules). Do not use the
    legacy `isMinifyEnabled`/`isShrinkResources`/`proguardFiles` DSL.
  - Keep debug unoptimized. Put keep rules in `app/src/main/keepRules/*.keep`,
    narrowly scoped to reflection/serialization paths. No custom rules exist
    today.
  - Do not disable R8 full mode in `gradle.properties`.

- All strings in `strings.xml`, all colors in `colors.xml`
  - User-visible text, including device/display row labels and share text
    headings, comes from `strings.xml`.
  - Use `tools:text` for design-time sample text in layouts, not
    `android:text`.
  - Reusable colors are named in `colors.xml`; layouts and drawables reference
    them instead of inline hex or deprecated framework colors.
  - Delete unused template strings, styles, colors, and dimens when the file
    is touched.

## Gaps & Recommendations

None open. Phase 1 moved all user-visible text to resources, removed inline
and framework colors, and replaced deprecated display APIs.
