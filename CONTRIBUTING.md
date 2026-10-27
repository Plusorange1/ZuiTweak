# Contributing

Thanks for helping. This is a small module, so the rules are short.

## Before opening an issue

1. Read [docs/INSTALL.md](docs/INSTALL.md) and [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md).
2. Search existing issues.
3. Make sure you rebooted after enabling the module — most "it does nothing" reports are a
   process that was never restarted.

Use the issue template and include:

- `getprop ro.build.display.id`
- `getprop ro.config.lgsi.region` and `getprop ro.com.zui.version`
- root manager + Zygisk provider + Xposed framework versions
- `su -c 'logcat -d | grep -i ZuiTweak'`

## Pull requests

1. Fork the repo and create a topic branch (`feat/…`, `fix/…`, `port/…`).
2. Keep changes focused — one hook or one fix per PR.
3. Build locally (`./gradlew assembleDebug`) and test on a real device.
4. Describe what you tested: device, firmware, which toggles, before/after.

## Adding a hook

1. Create `hooks/HookYourThing.kt` extending `HookBaseHandleLoadPackage` or
   `HookBaseHandleInitPackageResources`.
2. Provide a `menuItem` with a `HookMenuCategory`, a title and a description from
   `strings.xml` (add all three locales: `values`, `values-ko`, `values-ru`).
3. Declare the constraints:

   ```kotlin
   override val hookTargetDevice: Array<String> = emptyArray()   // e.g. arrayOf("TB320FC")
   override val hookTargetRegion: Array<String> = arrayOf("ROW") // or "PRC", empty = any
   override val hookTargetVersion: Array<String> = emptyArray()  // empty = any version
   override val hookTargetPackage: Array<String> = arrayOf("com.android.settings")
   ```

4. Use `XposedUtil.executeHook(...)` — it logs failures instead of crashing the target
   process.
5. For version-specific code, **add** a branch rather than editing the old one:

   ```kotlin
   when (Constants.deviceVersion) {
       "16.0" -> hookOld(lpparam)
       "17.0" -> hookNew(lpparam)
   }
   ```

6. Register it in `HookManager.registerHooks()`.
7. If it depends on a class that may be missing, guard with `XposedHelpers.findClassIfExists`
   or let `executeHook` catch it.

## Style

- Kotlin, 4-space indent, explicit imports.
- No hardcoded user-visible strings — always `strings.xml`.
- Do not touch framework/HAL/telephony internals or remove system apps.
- Keep the original license headers.

## Commit messages

```
feat(hooks): add ZUI 17 branch for X
fix(taskbar): use inflateLayout on ZUI 17
docs: describe the Vector scope
```

## License

By contributing you agree that your changes are released under the
[Apache License 2.0](LICENSE).
