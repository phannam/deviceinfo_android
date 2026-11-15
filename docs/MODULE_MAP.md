# Module Map

## Current Module Structure

`settings.gradle.kts` includes one module: `:app`.

| Module | Path | Owns |
|---|---|---|
| `:app` | `app/` | Everything: Application, Activity, fragments, adapter, singleton data holder, model, custom tab views, resources, tests |

Build configuration:

- Root `build.gradle.kts` declares the Android application plugin
  (`apply false`).
- `settings.gradle.kts` configures repositories (`google()`, `mavenCentral()`,
  `gradlePluginPortal()` for plugins) with `FAIL_ON_PROJECT_REPOS`.
- `gradle/libs.versions.toml` holds SDK levels, AGP, and library versions.
- `gradle/gradle-daemon-jvm.properties` pins the Gradle daemon JVM
  (JetBrains, version 25).
- `app/libs/*.jar` is included through `fileTree`, but no jar exists.

Current packages under `vn.npstudio.deviceinfo`:

| Package | Contents |
|---|---|
| `activities` | `BaseActivity`, `TabActivity`, `MainActivity` |
| `fragments` | `BaseFragment`, `ListFragment`, `DeviceFragment`, `DisplayFragment` |
| `utils` | `DeviceApplication`, `AppManager`, `Item`, `ItemListAdapter`, `Constant` |
| `views` | `SlidingFragmentTab`, `SlidingTab`, `SlidingTabRenderer`, `SlidingTabLayout`, `SlidingTabStrip` |

## Target Module Structure

Stay with a single `:app` module. The app is too small for feature or core
modules to pay off. Enforce boundaries with packages instead. Revisit
modularization only if the app grows beyond a few screens or shares code with
other NPStudio apps.

Target packages under `vn.npstudio.deviceinfo`:

| Package | Owns | May depend on | Must not contain |
|---|---|---|---|
| (root) | `DeviceInfoApplication` (`@HiltAndroidApp`) | `di` | Feature logic |
| `feature.main` | `MainActivity`, `MainViewModel`, tab and share UI state/effects | `domain`, `common` | Platform data access |
| `feature.device` | `DeviceInfoFragment`, `DeviceInfoViewModel` | `domain`, `common`, `ui` | Platform data access |
| `feature.display` | `DisplayInfoFragment`, `DisplayInfoViewModel` | `domain`, `common`, `ui` | Platform data access |
| `ui` | Shared list UI: `InfoRowAdapter`, `InfoRowUiModel`, row mapper | `domain`, `common` | Repositories, data sources |
| `domain` | `DeviceInfo`, `DisplayInfo`, `DeviceInfoRepository` interface, `ShareTextFormatter` | Kotlin stdlib only | Android framework types, resources |
| `data` | `DeviceInfoRepositoryImpl`, `BuildInfoDataSource`, `DisplayInfoDataSource` | `domain` | UI, ViewModels |
| `di` | Hilt modules (repository/data-source bindings, dispatcher) | `domain`, `data` | Feature logic |
| `common` | `stateflow` (`UiEffect`, `LoadingMode`), `text` (`UiText`), `extension` (`collectWithLifecycle`) | AndroidX lifecycle | Feature or data code |

`ShareTextFormatter` needs localized headings and labels. Keep it free of
Android types by having it produce structured content (sections of label
resource ids and values) that the UI layer resolves, or resolve labels through
an injected string-resolver interface. Choose one in the share refactor task
and document it here.

**Dependency rules**

- `feature.*` must not import another `feature.*` package, except
  `feature.main` creating the tab fragments.
- `feature.*` and `ui` must not import `data`.
- `domain` must not import Android framework classes or `R`.
- `data` must not import `feature.*`, `ui`, or ViewModels.
- `common` must not import any feature, domain, or data package.

**File placement rules for new code**

- New screen: `feature/<name>/` with its Fragment/Activity, ViewModel, and UI
  state.
- New layout: `res/layout/` named per `docs/CONVENTIONS.md`.
- New platform reader (battery, CPU, memory, sensors, storage, network
  state, etc.): a data source in `data/` plus a domain model and repository
  method in `domain/`. Add any required permission to the manifest in the
  same task.
- New shared UI component: `ui/`.
- New state/effect/text helper: `common/`. Reuse before adding.
- New Hilt binding: `di/`.
- Do not add new code to `activities`, `fragments`, `utils`, or `views`;
  those packages are removed as their contents migrate.

## Gaps & Recommendations

### 1. Packages are by type, not feature

- Current: `activities`, `fragments`, `utils`, `views`.
- Target: `feature.*`, `ui`, `domain`, `data`, `di`, `common`.
- How to fix: Move files into the target packages as each one is converted to
  Kotlin. Do not move unrelated files.
- Priority: Medium

### 2. `utils` mixes app startup, data, model, adapter, and constants

- Current: `DeviceApplication`, `AppManager`, `Item`, `ItemListAdapter`, and
  `Constant` share one package.
- Target: Each moves to its owning package; `AppManager` and `Constant` are
  deleted.
- Priority: Medium

### 3. Vendored tab views carry dead code

- Current: `SlidingTabLayout` (ViewPager-based) is never inflated but is kept
  because `SlidingTabStrip` and `SlidingFragmentTab` reference its
  `TabColorizer`. It is the only user of `androidx.viewpager`.
- Target: Replace the custom tab strip (recommended: Material `TabLayout`),
  delete `views/`, and remove the `viewpager` dependency.
- Priority: Low

### 4. Unused `fileTree` dependency

- Current: `implementation(fileTree(... "libs" ...))` with no `app/libs/`.
- Target: Remove it.
- Priority: Low
