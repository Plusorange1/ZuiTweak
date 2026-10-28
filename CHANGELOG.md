# Changelog

All notable changes to this fork. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), versions use
[SemVer](https://semver.org/).

## [1.2.0] — 2026-10-28

### Added

- **Double tap to sleep** — double tap an empty area of the home screen to turn the screen
  off. ZUI's launcher already implements the gesture (`Utilities.doubleTapLockScreen`); the
  toggle drives the hidden `Settings.Secure.double_tap_to_lock_screen` value and re-applies
  it when the launcher starts.
- **Status bar brightness gesture** — slide horizontally along the status bar to change
  screen brightness. SystemUI writes `SCREEN_BRIGHTNESS` and switches brightness mode to
  manual on the first movement.

## [1.1.1] — 2026-10-28

### Fixed

- **Hide buttons under keyboard**: the 120 px strip left behind by the removed IME
  navigation row is now given to the keyboard instead of staying empty. The keyboard
  content is stretched to the full IME window height, so no dead band remains.

## [1.1.0] — 2026-10-28

### Added

- **Hide buttons under keyboard (Gboard)** — removes the *hide keyboard* chevron and the
  *input method* switcher from the IME navigation row. The row is drawn by
  `InputMethodService` (`android.inputmethodservice.navigationbar.NavigationBarFrame`), so
  the module must be scoped to `com.google.android.inputmethod.latin`.
- This is the first tweak that also applies to a non-system package; add Gboard to the
  module scope to use it.

## [1.0.1] — 2026-10-27

### Changed

- New app icon from the ZUi artwork: adaptive background, foreground and
  monochrome (themed) layers plus legacy density PNGs.

## [1.0] — ZUI 17 fork

First release of the fork, based on upstream **v3.4.0** (ZUI 15/16).

### Added

- **ZUI 17 (Android 15) support**
  - version gates accept `17.0` across 9 hooks;
  - Dolby re-targeted to `com.lenovo.settings.sound.dolby.DolbyAtmosUtils.isHeadsetConnected(Context)`;
  - Smart Split re-targeted to `com.lenovo.settings.onevision.horizontal.EmbeddingAppFragment`;
  - taskbar hook re-targeted to `com.android.launcher3.DeviceProfile$Builder` and
    `NavigationBarInflaterView.inflateLayout(String)`.
- **Russian localization** — full `values-ru` (107 strings + new UI keys), language
  dialog with English / 한국어 / Русский.
- **Material 3 UI** — Scaffold + TopAppBar, status card, grouped cards, per-option
  switches, constraint badges, human-readable "unavailable" reasons, search field.
- Redesigned `SettingsViewActivity`.
- `docs/` (install, compatibility, build, porting notes) and issue templates.

### Fixed

- Language switching did not apply resources on modern Android — replaced the
  deprecated `updateConfiguration` path with `attachBaseContext`; hook titles are now
  resolved in the selected locale.
- `NavigationBarInflaterView.onLikelyDefaultLayoutChange` no longer exists on ZUI 17 —
  the taskbar hook now uses `inflateLayout(String)`.
- `HookRemoveShutdownMessage` was gated to ZUI 16 and showed as unavailable on 17.

### Changed

- Version reset to **1.0** for the fork.
- Developer line changed to **by Plusorange**.
- Taskbar toggle re-enabled (upstream ships it disabled).

### Removed

- **Multiple Space** is not offered on ZUI 17: the Settings classes were removed and
  only resource strings remain.

[1.0]: ../../releases/tag/v1.0

[1.0.1]: ../../releases/tag/v1.0.1

[1.1.0]: ../../releases/tag/v1.1.0

[1.1.1]: ../../releases/tag/v1.1.1

[1.2.0]: ../../releases/tag/v1.2.0
