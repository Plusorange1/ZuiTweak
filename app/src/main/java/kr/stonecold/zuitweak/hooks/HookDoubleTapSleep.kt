package kr.stonecold.zuitweak.hooks

import android.app.Application
import android.provider.Settings
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

/**
 * Enables ZUI's built-in "double tap to sleep" on the home screen.
 *
 * The launcher already implements the gesture (`Utilities.doubleTapLockScreen`,
 * active in `LauncherState.NORMAL`) behind a hidden secure setting. We only have to
 * turn that setting on; the UI writes it back to 0 when the toggle is disabled.
 */
class HookDoubleTapSleep : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.UNFUCKZUI,
            title = LanguageUtil.getString(R.string.hook_double_tap_sleep_title),
            description = LanguageUtil.getString(R.string.hook_double_tap_sleep_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.zui.launcher")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.zui.launcher") return

        try {
            XposedHelpers.findAndHookMethod(
                Application::class.java,
                "onCreate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val app = param.thisObject as Application
                            Settings.Secure.putInt(app.contentResolver, SETTING, 1)
                            XposedUtil.xposedDebug(tag, "double_tap_to_lock_screen=1")
                        } catch (t: Throwable) {
                            XposedUtil.handleHookException(tag, t, "Settings.Secure", "putInt")
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            XposedUtil.handleHookException(tag, t, "Application", "onCreate")
        }
    }

    companion object {
        const val KEY = "HookDoubleTapSleep"
        const val SETTING = "double_tap_to_lock_screen"
    }
}
