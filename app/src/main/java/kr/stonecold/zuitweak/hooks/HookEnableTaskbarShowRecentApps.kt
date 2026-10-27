package kr.stonecold.zuitweak.hooks

import android.content.Context
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookEnableTaskbarShowRecentApps : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.ROW,
            title = LanguageUtil.getString(R.string.hook_enable_taskbar_show_recent_apps_title),
            description = LanguageUtil.getString(R.string.hook_enable_taskbar_show_recent_apps_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = arrayOf("ROW")
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "com.android.settings" -> {
                when (Constants.deviceVersion) {
                    "16.0", "17.0" -> {
                        hookTaskbarDisplaySettingsControllerIsShowRecentSetting(lpparam)
                    }

                    "15.0" -> {
                        hookTaskbarDisplaySettingsControllerIsShowRecentSetting15(lpparam)
                    }
                }
            }
        }
    }

    private fun hookTaskbarDisplaySettingsControllerIsShowRecentSetting(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.lenovo.settings.system.TaskbarDisplaySettingsController"
        val methodName = "isShowRecentSetting"
        val parameterTypes = arrayOf<Any>(Context::class.java)
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookTaskbarDisplaySettingsControllerIsShowRecentSetting15(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.system.TaskbarDisplaySettingsController"
        val methodName = "isShowRecentSetting"
        val parameterTypes = arrayOf<Any>(Context::class.java)
        val callback = XC_MethodReplacement.returnConstant(true)

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
}
