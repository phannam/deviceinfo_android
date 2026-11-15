# Testing

## Current Test Coverage

- What is tested today
  - `app/src/test/java/vn/npstudio/deviceinfo/ExampleUnitTest.java` asserts
    `2 + 2 == 4`.
  - `app/src/androidTest/java/vn/npstudio/deviceinfo/ApplicationTest.java`
    asserts that the target context package name is `vn.npstudio.deviceinfo`.
  - No app behavior is covered.

- Tools currently set up
  - JUnit 4.13.2 (`testImplementation(libs.junit)`).
  - AndroidX Test ext JUnit and runner (`androidTestImplementation`).
  - `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`.
  - Not configured: coroutines-test, MockK, Turbine, Espresso, Robolectric,
    coverage tooling, CI.

## Target Testing Strategy

- What to test
  - Mappers and formatters: `DeviceInfo`/`DisplayInfo` -> rows, and share text
    output (headings, ordering, number formatting).
  - Repository: combines data sources correctly (use fake data sources).
  - ViewModels: initial loading state, loaded state, tab selection, share
    effect emission.
  - Data sources: keep thin; cover on device only if they contain logic such
    as API-level fallbacks.
- What to skip
  - Activities/Fragments directly, generated bindings, Hilt-generated code,
    and framework behavior.
  - Real `Build` values in JVM tests. Use fakes behind data-source interfaces.

- Folder structure
  - Unit tests: `app/src/test/java/vn/npstudio/deviceinfo/...`, mirroring the
    production package.
  - Instrumented tests: `app/src/androidTest/java/vn/npstudio/deviceinfo/...`
    only for platform behavior (display metrics fallback, tab restoration
    across recreation).
  - New tests are Kotlin. Replace the Java placeholders when the first real
    tests are added.

- Naming
  - Unit tests: `ClassNameTest`. Instrumented tests: `ClassNameAndroidTest`.
  - Method names describe behavior, for example
    `load_whenDataSourceReturnsMetrics_emitsRowsInOrder`.

- Libraries (add to the version catalog with the first real tests)
  - `kotlinx-coroutines-test` with a test dispatcher injected into ViewModels.
  - Turbine for `StateFlow`/`SharedFlow` assertions.
  - MockK only if a fake is impractical; prefer fakes. Do not add Mockito.
  - For `UiText`, assert the type and resource id or dynamic value; do not
    resolve Android strings in JVM tests.

- Coverage target
  - Aim for 80% of `domain`, `data` repository, `ui` mapper, and ViewModel
    code. Exclude Activities, Fragments, adapters, generated code, and
    resources. Add coverage tooling (Kover or JaCoCo) before enforcing it.

- Command reference only. The owner restriction in `AGENTS.md` prohibits
  running these without explicit approval for the current task; listing them
  here is not authorization.
  - Debug APK: `./gradlew :app:assembleDebug`
  - Release APK: `./gradlew :app:assembleRelease`
  - Unit tests: `./gradlew :app:testDebugUnitTest`
  - Instrumented tests (device required):
    `./gradlew :app:connectedDebugAndroidTest`
  - Lint: `./gradlew :app:lintDebug`
  - Install on a connected device: `./gradlew :app:installDebug`
  - Clean: `./gradlew clean`

## Manual Checks

Run these on a device after UI, tab, or share changes:

- Both tabs show all rows; long values (for example `Build.DISPLAY` or long
  model names) are not clipped at large font scale.
- Switch tabs several times, then press Back once: the app exits (target
  behavior; today Back walks through tab history).
- Rotate or enable **Don't keep activities**, leave and return: the selected
  tab and its content are restored without duplicates.
- Share on Android 11+ and on an older device: the chooser opens and the
  shared text contains both sections.
- If installs from Android Studio fail with a generic "Error" and no logs,
  check `adb shell echo ok`. If it hangs, the device's `adbd` is stuck:
  run `adb kill-server && adb start-server` or reconnect the device.

## Gaps & Recommendations

❌ Current: Only placeholder tests exist.
✅ Target: Unit tests for mapper, formatter, repository, and ViewModels.
📋 How to fix: Add tests in the same task that introduces each MVVM piece.
⚠️ Priority: High

❌ Current: No coroutine/Flow test tooling.
✅ Target: `kotlinx-coroutines-test` and Turbine in the version catalog.
📋 How to fix: Add them with the first ViewModel.
⚠️ Priority: Medium

❌ Current: No CI.
✅ Target: A pull-request workflow running `:app:testDebugUnitTest` and
`:app:lintDebug`.
📋 How to fix: Add after the first real tests exist.
⚠️ Priority: Low
