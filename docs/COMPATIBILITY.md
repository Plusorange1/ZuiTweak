# Compatibility

## Devices

| Device | Model | ZUI 15 | ZUI 16 | ZUI 17 · Android 15 |
|---|---|:--:|:--:|:--:|
| Legion Tab Gen 2 / Y700 2023 | `TB320FC` | ✅ | ✅ | ✅ tested |
| Xiaoxin Pad Pro 12.7 | `TB371FC` | ✅ | ✅ | ⚠️ untested |

The module checks `ro.product.model` and refuses to run on anything else.

Verified on:

```
TB320FC_ROW_OPEN_USER_Q00031.0_V_ZUI_17.0.313_ST_250808
Android 15 · vendor patch 2025-02-05 · region ROW
```

## Hooks

`✅` supported · `⚠️` conditional (region/device/package) · `❌` not available · `—` not registered

| Hook | Category | Constraint | 15 | 16 | 17 | Notes |
|---|---|:--:|:--:|:--:|:--:|---|
| Enable camera silent mode | Common | — | ✅ | ✅ | ✅ | |
| Remove lockscreen shortcuts | Common | — | ✅ | ✅ | ✅ | SystemUI keyguard classes unchanged |
| Remove shutdown message | Common | — | ✅ | ✅ | ✅ | gate updated to 17 |
| Enable hotspot | Common | — | ✅ | ✅ | ✅ | |
| Enable Wi-Fi Direct | Common | — | ✅ | ✅ | ✅ | |
| Change keyboard hotkeys | Common | `TB371FC` keys | ✅ | ✅ | ✅ | |
| Disable keyboard hotkey menu | Common | — | ✅ | ✅ | ✅ | |
| Expand display settings | Common | — | ✅ | ✅ | ✅ | |
| Remove unused game icons | Common | — | ✅ | ✅ | ✅ | |
| Disable auto pen button | Common | — | ✅ | ✅ | ✅ | |
| Battery consumption warning | ROW | ROW | ✅ | ✅ | ✅ | |
| Smart Split | ROW | ROW | ✅ | ✅ | ✅ | moved to `onevision/horizontal/*` |
| Multiple Space | ROW | ROW + companion | ✅ | ✅ | ❌ | classes removed in ZUI 17 |
| Notification icon | Unfuck | — | ✅ | ✅ | ✅ | |
| Dolby for built-in speakers | Unfuck | — | ✅ | ✅ | ✅ | new target `DolbyAtmosUtils` |
| Disable app force-stop | Unfuck | — | ✅ | ✅ | ✅ | |
| Disable taskbar | Unfuck | — | ✅ | ✅ | ✅ | re-enabled, `inflateLayout` |
| Disable virus scan | Unfuck | PRC | ✅ | ✅ | ⚠️ | `com.zui.safecenter` absent on ROW |
| AOSP package installer | Unfuck | PRC | ✅ | ✅ | ⚠️ | ROW uses `com.google.android.packageinstaller` |
| AOSP permission controller | Unfuck | PRC | ✅ | ✅ | ⚠️ | ROW uses `com.google.android.permissioncontroller` |
| Enable pen service | Device | `TB371FC` | ✅ | ✅ | ⚠️ | device-gated |
| Korean language settings | PRC | PRC | ✅ | ✅ | ⚠️ | requires companion patch |
| Launcher online search | PRC | PRC | ✅ | ✅ | ⚠️ | |
| DocumentsUI crash fix | PRC | PRC | ✅ | ✅ | ⚠️ | ROW uses `com.google.android.documentsui` |
| Study launcher | PRC | PRC | ✅ | ✅ | ⚠️ | |
| Remove PRC info | PRC | PRC | ✅ | ✅ | ⚠️ | |
| Battery icon test mode | PRC | PRC | ✅ | ✅ | ⚠️ | version gate is 15.0 |
| Smart Rotation | PRC | PRC | ✅ | ✅ | ⚠️ | |
| Apply Korean translation | Dev | PRC | — | — | ⚠️ | debug builds only |
| Remove notification block | Dev | — | — | — | ✅ | debug builds only |

## ZUI 17 class moves

What changed between ZUI 16 and 17 (found by decompiling `ZuiSettings.apk`,
`ZuiSystemUI.apk`, `ZuiLauncher.apk` with jadx):

| Feature | ZUI 15/16 | ZUI 17 |
|---|---|---|
| Dolby switch | `com.android.settings.dolby.DolbyAtmosPreferenceFragment`<br>`com.lenovo.settings.sound.dolby.DolbyAtmosFragment` | `com.lenovo.settings.sound.dolby.DolbyAtmosUtils.isHeadsetConnected(Context)` |
| Smart Split | `com.lenovo.settings.onevision.EmbeddingAppFragment` | `com.lenovo.settings.onevision.horizontal.EmbeddingAppFragment` |
| Taskbar (launcher) | `${packageName}.DeviceProfile$Builder` | `com.android.launcher3.DeviceProfile$Builder` |
| Taskbar (SystemUI) | `NavigationBarInflaterView.onLikelyDefaultLayoutChange()` | `NavigationBarInflaterView.inflateLayout(String)` |
| Taskbar settings | `com.android.settings.system.TaskbarDisplaySettingsController` | `com.lenovo.settings.system.taskbar.TaskbarSwitchPreferenceController` |
| Multiple Space | `com.android.settings.feature.multiplespace.manager.MultipleSpaceManager` | removed (strings only) |
| One Vision | `com.android.settings.onevision.OneVisionSettingsFragment` | `com.lenovo.settings.onevision.OneVisionSettingsFragment` |

Still present in ZUI 17 and used as-is: `XSystemUtil.isPad` / `isDevicePad`,
`Utilities.isLargeScreen`, `QDolbyAtmosTile` / `QDolbyAtmosDetailView`,
`BatteryMeterView`, `KeyguardQuickAffordanceInteractor`, `LenovoUtils.isPrcVersion` /
`isRowVersion`.

`Utilities.isTablet` and `NavigationBarInflaterView.onLikelyDefaultLayoutChange` are gone.

## Reporting a compatibility issue

Include:

- device model and full build string (`getprop ro.build.display.id`)
- region (`getprop ro.config.lgsi.region`) and ZUI version (`getprop ro.com.zui.version`)
- root manager + Zygisk provider + Xposed framework versions
- `su -c 'logcat -d | grep -i ZuiTweak'`
