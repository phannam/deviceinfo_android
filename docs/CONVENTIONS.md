# Conventions

## Current Conventions (inferred from codebase)

- Language and packages
  - All app code is Kotlin under `app/src/main/java/vn/npstudio/deviceinfo/`.
  - Packages are split by type, not feature: `activities`, `fragments`,
    `utils`, and `views`.
  - Most classes keep the original `Created by namphan on ...` header comment.

- Class naming
  - Android suffixes are used: `MainActivity`, `TabActivity`, `BaseActivity`,
    `DeviceFragment`, `DisplayFragment`, `ListFragment`, `BaseFragment`,
    `ItemListAdapter`, `DeviceApplication`.
  - `ListFragment` shadows the name of the framework/AndroidX `ListFragment`
    class.
  - `AppManager` is a singleton named as a manager but acts as the data source.
  - Field naming is camelCase without `m` prefixes.
  - Constants are grouped in nested objects (`Constant.Tab.TAB_DEVICEINFO`).

- XML layout file naming
  - `activity_main.xml`, `fragment_listitems.xml`, `item_info.xml`,
    `view_tab.xml`.

- Resource ID naming
  - IDs are semantic snake_case: `info_list`, `name_text`, `value_text`,
    `tab_title_text`, `tab_icon_image`, `sliding_tabs`, `fragment_container`,
    and menu `action_share`.

- View access
  - ViewBinding everywhere (`ActivityMainBinding`, `FragmentListitemsBinding`
    with `_binding` cleared in `onDestroyView`, `ItemInfoBinding`,
    `ViewTabBinding`). Only the unused vendored `SlidingTabLayout` calls
    `findViewById`.

- Strings, colors, and dimens
  - Hardcoded user-visible strings exist in Kotlin (`"Info"`, `"Device"`,
    `"Display"`, `"No email client!"`, `"Application not found!"`, every
    device/display row label in `AppManager`, the share text headings) and XML
    (`menu_main` title `"Share"`, `tools:text` samples in row and tab layouts are
    design-time only).
  - `strings.xml` contains unused template strings (`large_text`,
    `action_settings`).
  - Colors mix `colors.xml` entries, framework colors
    (`@android:color/background_dark`, `white`, `darker_gray`,
    `holo_blue_bright`), and inline hex (`#00000000` list selector).
  - Layouts use inline `dp`/`sp` values; `dimens.xml` holds unused template
    values.
  - `styles.xml` contains unused template styles (`AppTheme.NoActionBar`,
    `AppTheme.AppBarOverlay`, `AppTheme.PopupOverlay`).

- Deprecated APIs in use
  - `Display.getMetrics` and `DisplayMetrics.scaledDensity` (suppressed with
    `@Suppress("DEPRECATION")` in `AppManager`), and `singleLine` in XML.

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
  - Do not cache fragment instances outside `FragmentManager`.
  - Look up existing tab fragments by stable tag before adding new ones.
  - On Activity recreation, sync the selected tab from the ViewModel and the
    restored fragments without pushing another initial transaction.
  - Tab switches must not be added to the back stack; Back from the main
    screen exits the app.
  - Never call fragment lifecycle methods (`onPause`, `onStop`) manually.

- StateFlow for UI state, never expose MutableStateFlow
  - `private val _uiState = MutableStateFlow(...)` and
    `val uiState: StateFlow<...> = _uiState.asStateFlow()`.
  - Do not use `LiveData`, `MutableLiveData`, or `SingleLiveEvent`.
  - Use `SharedFlow` or `Channel` for one-time events such as showing a toast
    or launching the share chooser.
  - Reuse `vn.npstudio.deviceinfo.common.stateflow.UiEffect` for common
    effects (`ShowToast`, `ShowError`). Create a feature-owned effect only for
    feature-specific actions, such as `MainUiEffect.Share(text: String)`.
  - Collect flows in UI with `Flow<T>.collectWithLifecycle(...)` from
    `vn.npstudio.deviceinfo.common.extension`. Pass the Activity from
    Activities and `viewLifecycleOwner` from Fragments.
  - ViewModels emit user-visible text as `UiText`
    (`vn.npstudio.deviceinfo.common.text`); UI resolves it with `Context`.
  - These `common` contracts do not exist yet. Create each one in the first
    task that needs it, and reuse it afterwards.

- UI state shape
  - Each screen has an explicit UI state with `loadingMode` and `data`.
  - `LoadingMode` (`IDLE`, `INITIAL`, `REFRESH`) lives in
    `common.stateflow`. Add more values only when a screen needs them.
  - For the info list screens, `data` is a `List<InfoRowUiModel>`.
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
  - Remove dependencies that become unused (for example
    `androidx.viewpager` once the vendored `SlidingTabLayout` is removed).

- Release optimization
  - R8 is currently disabled for release. When it is enabled, keep debug
    unoptimized, keep rules narrowly scoped to reflection/serialization paths,
    and do not disable R8 full mode in `gradle.properties`.

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

❌ Current: Hardcoded user-visible strings in Kotlin and XML.
✅ Target: All user-visible text in `strings.xml`.
📋 How to fix: Move row labels, tab titles, toolbar title, toasts, menu title,
and share headings to string resources.
⚠️ Priority: Medium

❌ Current: Framework and inline hex colors in layouts and code.
✅ Target: Named colors in `colors.xml`.
📋 How to fix: Add semantic colors (row background, primary/secondary text,
divider) and reference them in layouts and the tab strip.
⚠️ Priority: Medium

❌ Current: Deprecated APIs (`getMetrics`, `scaledDensity`).
✅ Target: Current AndroidX/platform APIs.
📋 How to fix: Use `WindowMetricsCalculator`/`WindowManager.currentWindowMetrics`, and
`TypedValue.applyDimension` or font scale for scaled density when the related
code is refactored.
⚠️ Priority: Medium
