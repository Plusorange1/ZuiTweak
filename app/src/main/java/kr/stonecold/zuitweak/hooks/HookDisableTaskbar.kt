package kr.stonecold.zuitweak.hooks

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookDisableTaskbar : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.UNFUCKZUI,
            title = LanguageUtil.getString(R.string.hook_disable_taskbar_title),
            description = LanguageUtil.getString(R.string.hook_disable_taskbar_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.zui.launcher", "com.android.settings", "com.android.systemui")
    override val hookTargetPackageOptional: Array<String> = arrayOf("com.zui.desktoplauncher")

    private var isPad: ThreadLocal<Boolean?> = ThreadLocal.withInitial { null }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.zui.launcher", "com.zui.desktoplauncher" -> {
                hookDeviceProfileBuilderBuild(lpparam)
            }

            "com.android.settings" -> {
                hookSettingsHomepageActivityOnCreate(lpparam)
            }

            "com.android.systemui" -> {
                when (Constants.deviceVersion) {
                    "16.0", "17.0" -> {
                        hookUtilitiesIsLargeScreen(lpparam)
                        hookXSystemUtilIsDevicePad(lpparam)
                    }

                    "15.0" -> {
                        hookUtilitiesIsTablet(lpparam)
                        hookXSystemUtilIsPad(lpparam)
                    }
                }
                hookNavigationBarInflaterViewInflateLayout(lpparam)
            }
        }
    }

    private fun hookDeviceProfileBuilderBuild(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.launcher3.DeviceProfile\$Builder"
        val methodName = "build"
        val parameterTypes = arrayOf<Any>(Boolean::class.java)
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    if (param.hasThrowable()) {
                        return
                    }
                    val profile = param.result
                    if (XposedHelpers.getIntField(profile, "displayId") == 0) {
                        XposedHelpers.setBooleanField(profile, "isTaskbarPresent", false)
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookSettingsHomepageActivityOnCreate(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.homepage.SettingsHomepageActivity"
        val methodName = "onCreate"
        val parameterTypes = arrayOf<Any>(Bundle::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val activity = param.thisObject as Activity
                    val window = activity.window
                    window.navigationBarColor = 0x00000000
                    @Suppress("DEPRECATION")
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookUtilitiesIsLargeScreen(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.shared.recents.utilities.Utilities"
        val methodName = "isLargeScreen"
        val parameterTypes = arrayOf<Any>(Context::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val stack = Thread.currentThread().stackTrace
                    for (line in stack) {
                        if (line.className.contains("NavigationBarController")) {
                            param.result = false
                            return
                        }
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookUtilitiesIsTablet(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.shared.recents.utilities.Utilities"
        val methodName = "isTablet"
        val parameterTypes = arrayOf<Any>(Context::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val stack = Thread.currentThread().stackTrace
                    for (line in stack) {
                        if (line.className.contains("NavigationBarController")) {
                            param.result = false
                            return
                        }
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookXSystemUtilIsDevicePad(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.util.XSystemUtil"
        val methodName = "isDevicePad"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val hookedValue = isPad.get()
                    if (hookedValue != null) {
                        param.result = hookedValue == true
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookXSystemUtilIsPad(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.util.XSystemUtil"
        val methodName = "isPad"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val hookedValue = isPad.get()
                    if (hookedValue != null) {
                        param.result = hookedValue == true
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookNavigationBarInflaterViewInflateLayout(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.systemui.navigationbar.NavigationBarInflaterView"
        val methodName = "inflateLayout"
        val parameterTypes = arrayOf<Any>(String::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    isPad.set(false)
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }

            override fun afterHookedMethod(param: MethodHookParam?) {
                try {
                    isPad.set(null)
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
}
