# Installation guide

Tested on `TB320FC` running ZUI 17.0.313 ROW (Android 15) with Magisk 31.1 and
Vector 2.2. The same steps apply to ZUI 15/16, with LSPosed allowed on Android ≤ 14.

## 1. Prerequisites

- Unlocked bootloader and working **root** (Magisk recommended).
- A **Zygisk** provider — exactly one:
  - Magisk built-in (Settings → Zygisk), or
  - [NeoZygisk](https://github.com/JingMatrix/NeoZygisk), or
  - [Zygisk Next](https://github.com/Dr-TSNG/ZygiskNext).
- A boot/vendor_boot backup. If you do not have one, take it before touching anything.

> Two Zygisk providers at the same time is the most common reason modules load but do
> nothing (and the cause of `SELinux policy not loaded` in the Vector manager).

## 2. Install the Xposed framework

### Vector (recommended, Android 12–17)

1. Download the latest **Release** zip from
   [JingMatrix/Vector releases](https://github.com/JingMatrix/Vector/releases).
2. Magisk → **Modules** → *Install from storage* → pick the zip → reboot.
3. Open the manager from the system notification (Vector installs its manager
   parasitically into the shell app).

### LSPosed (Android ≤ 14 only)

Use [LSPosed 1.9.2](https://github.com/LSPosed/LSPosed/releases) as a Zygisk module.
Classic LSPosed is archived and will not run on Android 15.

## 3. Install ZuiTweak

```bash
# over ADB
adb install -r ZuiTweak-1.1.1.apk
```

or copy the APK to the tablet and open it.

If an older ZuiTweak is installed, uninstall it first — this fork uses a different
signing key and Android will refuse the update:

```bash
adb uninstall kr.stonecold.zuitweak
adb install ZuiTweak-1.1.1.apk
```

## 4. Enable and scope the module

Open the Vector manager → **Modules** → tap **ZuiTweak** → enable it.

Set the scope (the processes the hooks are injected into):

```
com.android.settings
com.android.systemui
com.android.permissioncontroller
com.android.packageinstaller
com.android.documentsui
com.zui.launcher
com.zui.desktoplauncher
com.zui.camera
com.zui.safecenter
com.zui.version
com.zui.game.service
com.lenovo.penservice
com.google.android.inputmethod.latin
```

> `com.google.android.inputmethod.latin` is only needed for the hide-keyboard-buttons tweak.

From a terminal (root) the same thing can be done with the Vector CLI:

```bash
V=/data/adb/modules/zygisk_vector/cli
su -c "/system/bin/sh $V modules enable kr.stonecold.zuitweak"
su -c "/system/bin/sh $V scope set kr.stonecold.zuitweak \
  com.android.settings/0 com.android.systemui/0 com.zui.launcher/0 \
  com.zui.desktoplauncher/0 com.zui.camera/0 com.zui.version/0 \
  com.zui.game.service/0 com.lenovo.penservice/0 \
  com.google.android.inputmethod.latin/0"
```

## 5. Reboot

A reboot is required so the hooks are injected into `system_server`, SystemUI and the
launcher. Without it the module shows as enabled but does nothing.

## 6. Verify

```bash
# module visible and enabled
su -c '/system/bin/sh /data/adb/modules/zygisk_vector/cli modules ls'

# hook activity
su -c 'logcat -d | grep -i "ZuiTweak"'
```

Then open ZuiTweak and flip a visible toggle (for example *Remove lockscreen
shortcuts*), restart SystemUI or reboot, and check the result.

## 7. Language

Menu ⋮ → **Language** → English / 한국어 / Русский. The app follows the system language
on first launch. The choice is stored in the Xposed shared-prefs store
(`/data/misc/<uuid>/prefs/kr.stonecold.zuitweak/`), so it survives reboots and updates.

## Updating

1. Uninstall the old APK (different signing key between builds):
   `adb uninstall kr.stonecold.zuitweak`
2. Install the new APK.
3. The module stays enabled in Vector; toggles are stored per hook and are kept only if
   you did **not** uninstall. Back them up if that matters.

## Uninstall

1. Disable the module in Vector.
2. Uninstall the APK.
3. Reboot.

No system partitions are modified — removing the module is enough.

## Recovery (bootloop)

The module does not touch partitions, so recovery is software-only:

```bash
# disable the whole framework
su -c 'touch /data/adb/modules/zygisk_vector/disable'
# or just this module
su -c '/system/bin/sh /data/adb/modules/zygisk_vector/cli modules disable kr.stonecold.zuitweak'
```

Then reboot. If the system boots but SystemUI crashes, disable the module the same way
from a root shell (ADB or TWRP terminal).

## FAQ

**The module is enabled but nothing changes.**
The target process was not restarted. Reboot, or restart SystemUI
(`su -c 'killall com.android.systemui'`).

**One option is greyed out.**
It is not applicable: wrong region (ROW/PRC), missing package, or unsupported ZUI
version. The reason is printed under the description.

**Do I need the ZuiTweak-magisk companion?**
Only for Multiple Space and the Korean language patch, both from upstream. Multiple
Space does not exist on ZUI 17.
