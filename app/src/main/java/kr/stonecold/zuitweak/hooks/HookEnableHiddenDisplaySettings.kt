package kr.stonecold.zuitweak.hooks

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookEnableHiddenDisplaySettings : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.COMMON,
            title = LanguageUtil.getString(R.string.hook_enable_hidden_display_settings_title),
            description = LanguageUtil.getString(R.string.hook_enable_hidden_display_settings_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.settings" -> {
                when (Constants.deviceVersion) {
                    "16.0", "17.0" -> {
                        hookAutoColorPreferenceControllerGetAvailabilityStatus(lpparam)
                        hookGameEnhancePreferenceControllerGetAvailabilityStatus(lpparam)
                        hookVideoColorEnhancePreferenceControllerGetAvailabilityStatus(lpparam)
                        hookVideoQualityEnhancePreferenceControllerGetAvailabilityStatus(lpparam)
                    }

                    "15.0" -> {
                        hookUtilsIsFeatureEnabled(lpparam)
                        hookSettingsPreferenceFragmentRemovePreference(lpparam)
                    }
                }
            }
        }
    }

    private fun hookAutoColorPreferenceControllerGetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.display.AutoColorPreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "param.result: ${param.result}")
                    param.result = 0
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookGameEnhancePreferenceControllerGetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.display.GameEnhancePreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "param.result: ${param.result}")
                    param.result = 0
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookVideoColorEnhancePreferenceControllerGetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.display.VideoColorEnhancePreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "param.result: ${param.result}")
                    param.result = 0
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookVideoQualityEnhancePreferenceControllerGetAvailabilityStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.display.VideoQualityEnhancePreferenceController"
        val methodName = "getAvailabilityStatus"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "param.result: ${param.result}")
                    param.result = 0
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookUtilsIsFeatureEnabled(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.Utils"
        val methodName = "isFeatureEnabled"
        val parameterTypes = arrayOf<Any>(String::class.java, Context::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val featureName = param.args[0] as String
                    if (featureName == "config_zuimiravision_enabled") {
                        param.result = true
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookSettingsPreferenceFragmentRemovePreference(lpparam: XC_LoadPackage.LoadPackageParam) {
        val preferenceNamesToCheck = setOf(
            "gaming_hdr_enhancement",
            "screensaver",
            "video_color_enhance",
            "video_quality_enhance",
            "display_enhancement",
        )

        val className = "com.android.settings.SettingsPreferenceFragment"
        val methodName = "removePreference"
        val parameterTypes = arrayOf<Any>(String::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val preferenceName = param.args[0] as String
                    if (preferenceName in preferenceNamesToCheck) {
                        param.result = false
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
}
