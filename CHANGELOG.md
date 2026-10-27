# Changelog

All notable changes to this fork. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), versions use
[SemVer](https://semver.org/).

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
