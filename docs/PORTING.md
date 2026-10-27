# Porting notes — ZUI 17 (Android 15)

How this fork was produced from upstream v3.4.0. Useful when a future ZUI release moves
the same classes again.

## 1. Why upstream stops working on ZUI 17

Upstream v3.4.0 predates ZUI 17. `Constants.deviceVersion` reads `ro.com.zui.version`
(`"17.0"`), and every `when (deviceVersion) { "16.0" … "15.0" … }` falls through, so the
hooks run **nothing** — not even the parts whose classes still exist.

There are 38 such gates across 17 files. Most only need `"17.0"` added next to `"16.0"`;
the rest need re-targeting.

## 2. Finding the new classes

Everything was verified against the device's own APKs:

```
/system/priv-app/ZuiSettings/ZuiSettings.apk
/system_ext/priv-app/ZuiSystemUI/ZuiSystemUI.apk
/system/priv-app/ZuiLauncher/ZuiLauncher.apk
```

Fast class-existence check without decompiling:

```bash
unzip -p ZuiSettings.apk 'classes*.dex' | grep -aq 'Lcom/lenovo/settings/…;' && echo present
unzip -p ZuiSettings.apk 'classes*.dex' | grep -aoE 'com/lenovo/settings/[a-z/]*[Tt]askbar[A-Za-z/]*'
```

For method signatures and code, decompile a single class:

```bash
jadx --no-res --single-class com.lenovo.settings.sound.dolby.DolbyAtmosSettingsFragment \
     -d out ZuiSettings.apk
```

## 3. What moved

| Feature | Old target | ZUI 17 target |
|---|---|---|
| Dolby | `…sound.dolby.DolbyAtmosFragment` | `…sound.dolby.DolbyAtmosUtils.isHeadsetConnected(Context)` (static) |
| Smart Split | `…onevision.EmbeddingAppFragment` | `…onevision.horizontal.EmbeddingAppFragment` (+ inner classes) |
| Launcher taskbar | `${packageName}.DeviceProfile$Builder` | `com.android.launcher3.DeviceProfile$Builder` |
| Nav bar | `NavigationBarInflaterView.onLikelyDefaultLayoutChange()` | `NavigationBarInflaterView.inflateLayout(String)` |
| Taskbar settings | `…system.TaskbarDisplaySettingsController` | `com.lenovo.settings.system.taskbar.TaskbarSwitchPreferenceController` |
| Multiple Space | `…feature.multiplespace.manager.MultipleSpaceManager` | removed — only strings remain |

Still present and unchanged: `XSystemUtil.isPad` / `isDevicePad`, `Utilities.isLargeScreen`,
`QDolbyAtmosTile`, `QDolbyAtmosDetailView`, `BatteryMeterView`,
`KeyguardQuickAffordanceInteractor`, `LenovoUtils.isPrcVersion` / `isRowVersion`.

## 4. Hook fixes in this fork

- **Dolby** — new 17.0 branch calling `DolbyAtmosUtils.isHeadsetConnected(Context)` with
  `XC_MethodReplacement.returnConstant(true)`.
- **Smart Split** — new `"17.0"` match criteria list pointing at the `horizontal/`
  package; `LenovoUtils` hooks now run for `"16.0", "17.0"`.
- **Taskbar** — re-enabled in `HookManager`; launcher class renamed to
  `com.android.launcher3.DeviceProfile$Builder`; SystemUI branch reuses the 16.0 path
  (`Utilities.isLargeScreen` + `XSystemUtil.isDevicePad`); navigation hook switched to
  `inflateLayout(String)`.
- **Remove shutdown message** — `isEnabledCustomCheck()` now accepts 16.0 and 17.0.
- 9 hooks got `"17.0"` added to their 16.0 version branch.

## 5. UI and localization (v1.0)

- `attachBaseContext` applies the selected locale — `Resources.updateConfiguration()` is a
  no-op on modern Android.
- `resolveLanguage()` falls back to `Resources.getSystem()` instead of `Locale.getDefault()`
  (the app process can report `en` even when the system is `ru`).
- `LanguageUtil.getString()` resolves hook titles in the selected locale; without this the
  Activity was translated but hook titles stayed in the system language.
- The module uses `xposedsharedprefs`, so Vector redirects its preferences to
  `/data/misc/<uuid>/prefs/kr.stonecold.zuitweak/` — remember this when debugging settings.

## 6. Not portable

**Multiple Space**: `MultiSpace*` classes do not exist in ZUI 17 `ZuiSettings.apk`; only
`multiple_space_*` resource strings remain. Upstream also needs the `ZuiTweak-magisk`
companion, which is ZUI 15/16-only.

## 7. Checklist for the next ZUI release

1. `getprop ro.com.zui.version` — add the new value to the version gates.
2. For each hook: confirm the target class/method still exists (see §2).
3. Decompile moved classes with jadx; check whether the method semantics changed.
4. Add a `"x.y"` branch instead of editing the old one — keeps older firmwares working.
5. Build, install, reboot, and verify one hook per process (Settings, SystemUI, launcher).
6. Log with `su -c 'logcat -d | grep -i ZuiTweak'`; hooks fail individually and never
   crash the system.
