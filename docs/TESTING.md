# Testing

## Current Test Coverage

- What is tested today (JVM, `app/src/test/java/vn/npstudio/deviceinfo/`)
  - `ui/format/ValueFormatterTest`: duration split, ISO date localization and
    fallback, MHz/GHz frequency, hours/minutes, refresh rate Hz, decimal byte units, unavailable values.
  - `data/source/CpuSysFsParserTest`: CPU lists (`0-7`, `0-3,4-7`, bad
    input), kHz files (text, blank, missing), `/proc/cpuinfo` with and without
    `Hardware`.
  - `data/repository/CpuRepositoryImplTest`: cluster grouping, core and SoC
    fallbacks, offline cores, 1 s polling (virtual time), unreadable sysfs
    emits an empty list and completes. Uses `FakeSysFsReader`.
  - `data/source/BatteryParserTest`: level percent, µA unsupported values
    (`Int.MIN_VALUE`, `0`), status/plug/health constants, missing extras.
  - `data/repository/BatteryRepositoryImplTest`: broadcast + 2 s property
    poll combined (virtual time), unchanged polls are not re-emitted.
  - `feature/battery/BatteryRowMapperTest`: units (°C, V, mA, mAh), enum
    strings, estimated capacity rules, time to full only while charging,
    unavailable values.
  - `feature/battery/BatteryViewModelTest`: live updates and upstream stop
    5 s after the last subscriber.
  - `data/repository/MemoryRepositoryImplTest`: 2 s RAM polling (virtual
    time), unchanged values are not re-emitted, storage read.
  - `feature/memory/MemoryRowMapperTest`: decimal byte units, used-percent
    bars, unavailable values without bars.
  - `feature/memory/MemoryViewModelTest`: live RAM combined with storage;
    `onResumed()` reloads storage.
  - `feature/cpu/CpuRowMapperTest`, `feature/cpu/CpuViewModelTest`: CPU rows,
    per-core progress and Offline, notice row, live updates.
  - `feature/device/DeviceRowMapperTest`: row values and unavailable handling.
  - `feature/display/DisplayRowMapperTest`: resolution and window rows,
    density bucket, estimated diagonal, refresh rates, HDR ordering and None,
    unavailable values (including no window snapshot).
  - `feature/display/DisplayViewModelTest`: waits for the window snapshot;
    an equal snapshot still reloads display values.
  - `feature/device/DeviceViewModelTest`: `INITIAL` then loaded rows (Turbine).
  - `feature/main/ShareReportRendererTest`: header and section layout with a
    fake resolver.
  - `feature/main/MainViewModelTest`: selected tab restored from
    `SavedStateHandle`; share effect contains Device, CPU, Battery, Display, and Memory sections.
  - Shared helpers in `testing/`: `MainDispatcherRule`
    (`StandardTestDispatcher`), sample models, fake repositories and data sources, and
    `byLabel()` for row lookups (`rows[UiText.res(R.string.label_x)]`).
  - `app/src/androidTest/.../ApplicationTest.kt` asserts the package name.

- Tools currently set up
  - JUnit 4.13.2, `kotlinx-coroutines-test`, Turbine.
  - AndroidX Test ext JUnit and runner (`androidTestImplementation`).
  - Not configured: MockK, Espresso, Robolectric, coverage tooling, CI.

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
  - Tests are Kotlin. Put shared fakes and rules in `testing/`.

- Naming
  - Unit tests: `ClassNameTest`. Instrumented tests: `ClassNameAndroidTest`.
  - Method names describe behavior, for example
    `load_whenDataSourceReturnsMetrics_emitsRowsInOrder`.

- Libraries
  - `kotlinx-coroutines-test` with `MainDispatcherRule`; pass its dispatcher
    to `runTest`.
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
- Switch tabs several times, then press Back once: the app exits.
- Rotate or enable **Don't keep activities**, leave and return: the selected
  tab and its content are restored without duplicates.
- Share on Android 11+ and on an older device: the chooser opens and the
  shared text contains both sections.
- If installs from Android Studio fail with a generic "Error" and no logs,
  check `adb shell echo ok`. If it hangs, the device's `adbd` is stuck:
  run `adb kill-server && adb start-server` or reconnect the device.

## Gaps & Recommendations

❌ Current: No CI.
✅ Target: A pull-request workflow running `:app:testDebugUnitTest` and
`:app:lintDebug`.
⚠️ Priority: Low

❌ Current: No coverage tooling.
✅ Target: Kover or JaCoCo before enforcing the 80% target.
⚠️ Priority: Low
