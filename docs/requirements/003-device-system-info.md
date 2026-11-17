# 003 - Device Details, System Tab, and Section Card Style

- Status: Done (pending owner build and device checks).
- Owner: @phannam
- Builds on: `001-device-diagnostics.md`, `002-settings-theme-tab-manager.md`
- Build policy: the owner runs builds and reports errors. Agents must not run
  Gradle unless the owner approves it.
- References: owner screenshots of DevInfo (Device and System tabs) in
  `/Users/namphan/Working/Screenshots/deviceInfo/`.

## 1. Goal

- Show every info list as left-right rows (label | value) so the right side
  of the screen is used.
- Expand the Device tab: identifiers, telephony, feature checklist, codecs.
- Add a System tab: Android build, security, kernel and runtime, status, DRM.
- Look a little different from DevInfo.

## 2. Owner Decisions (2026-11-15)

| Topic | Decision |
|---|---|
| Identifiers | Show all identifiers from the screenshots; permission-guarded values show "Tap to grant" and ask on tap |
| Style | Style A: titled section cards with a primary-colored icon, gray label (~42%), medium-weight value, dividers, chips for the feature checklist |
| Process | Implement directly, no separate planning round |

## 3. Scope

In scope: Device and System tabs, section card UI for every list tab, share
report updates. Out of scope: new tabs beyond System, network/Wi-Fi details,
camera and sensor tabs.

## 4. UI

### 4.1 Section cards

- `InfoSection(title, icon, rows, features, inReport)` per card; the first
  card has no top gap, others get `cardGap` (12dp).
- Row: label 42% (`textColorSecondary`), value 58%
  (`TextAppearance.DeviceInfo.RowValue`, medium weight), optional progress
  bar, divider between rows.
- Action rows (`RowAction`) show the value in the primary color with an icon:
  `ShowList` (chevron, "N items", opens `ListEntriesBottomSheet`) and
  `RequestPermission` (key icon, "Tap to grant").
- Long-press on a row copies the value.
- Feature checklist: chips; supported = filled `secondaryContainer` with a
  check icon, not supported = outlined with a close icon at 70% alpha.
- System tab hero card: Android badge, version name, codename, release date
  on `colorPrimaryContainer` (follows the user's primary color).

### 4.2 Permissions and restrictions

- `READ_PHONE_STATE` is requested on tap. It unlocks the network type, and
  IMEI / SIM serial / subscriber ID / serial on Android 8-9 only.
- Android 10+ keeps device identifiers for privileged apps; these rows show
  "Restricted". Bluetooth MAC is always restricted, Wi-Fi MAC on Android 11+.
- When denied permanently, a Snackbar offers "App info".

## 5. Content

### 5.1 Device tab

Hero card (device name, brand, model). Device (device name from
`Settings.Global.DEVICE_NAME`, Build values, bootloader state, and Samsung
vendor properties shown only when set: product code, sales region, CSC
firmware, Knox warranty fuse, manufacture date), Identifiers (vendor serial
number when set, Android ID, GSF ID, advertising ID, serial,
fingerprint, IMEI, SIM serial, subscriber ID, Wi-Fi/Bluetooth MAC, USB
debugging, developer options), Telephony (phone type, SIM slots, operators,
network type), Developer, Features (chips), Multimedia (video/audio decoders
and encoders, container formats).

### 5.2 System tab

Hero, Android (version, API, security patch, build, build date, type, tags,
bootloader, baseband), Security (root, SELinux, system-as-root, seamless
updates, dynamic partitions, Treble, VNDK, system features list), Kernel &
runtime, Status (uptime ticking every second, boot count, language, time
zone), DRM (Widevine; hidden when unavailable).

## 6. Share Report

- Adds a System section after Device.
- The Identifiers section (`inReport = false`) is never shared.
- Feature chips become "Supported" / "Not supported" rows.

## 7. Notes

- `play-services-ads-identifier` reads the advertising ID; it may merge the
  `AD_ID` permission, which needs a Play Console declaration.
- Existing users get the System tab appended to the end of their tab order
  (`AppConfigParser` appends missing tabs).
