package kr.stonecold.zuitweak

import kr.stonecold.zuitweak.common.*
import kr.stonecold.zuitweak.hooks.*

@Suppress("unused")
object HookManager {
    private val hooks: MutableList<HookBase> = mutableListOf()

    init {
        registerHooks()
    }

    private fun registerHook(hook: HookBase) {
        hooks.add(hook)
    }

    fun registerHooks() {
        hooks.clear()

        //COMMON
        registerHook(HookEnableZuiCameraShutterOption())
        registerHook(HookRemoveLockscreenShortcuts())
        registerHook(HookRemoveShutdownMessage())
        registerHook(HookEnableHotspot())
        registerHook(HookEnableWifiDirect())
        registerHook(HookChangeKeyboardHotkeys())
        registerHook(HookDisableKeyboardHotkeyMenu())
        registerHook(HookEnableHiddenDisplaySettings())
        registerHook(HookRemoveUnusedGameServiceIcons())
        registerHook(HookDisableAutoPenButton())
        //registerHook(HookEnableTurnScreenOn()) ///테스트 필요

        //ROW
        if (Constants.deviceVersion == "16.0") {
            registerHook(HookEnableMultipleSpace16())
        } else {
            registerHook(HookEnableMultipleSpace())
        }
        registerHook(HookEnableBatteryOverheatNotify())
        registerHook(HookEnableOneVisionSmartSplit())
        //registerHook(HookEnableTaskbarShowRecentApps()) //동작안함
        //registerHook(HookEnableStudyLauncher()) //추가 패키지 팔요

        //PRC
        registerHook(HookAddKoreanLanguageSettings())
        registerHook(HookDisableLauncherOnlineSearch())
        registerHook(HookFixDocumentsUICrash())
        registerHook(HookDisableStudyLauncher())
        registerHook(HookRemovePrcInfo())
        registerHook(HookFixBatteryIconTestMode())
        registerHook(HookEnableOneVisionSmartRotation())

        //DEVICE
        registerHook(HookEnablePenService())

        //UNFUCKZUI
        //registerHook(HookDisableTaskbar()) //기본 기능으로 제공
        registerHook(HookDisableTaskbar()) // ZUI 17 fork: re-enabled
        registerHook(HookChangeNotificationIcon())
        registerHook(HookAllowDisableDolbyAtmosForBuiltinSpeakers())
        registerHook(HookHideImeNavBar())
        registerHook(HookDoubleTapSleep())
        registerHook(HookStatusBarBrightness())
        registerHook(HookDisableAppSwitchForceStop())
        registerHook(HookDisableVirusScan())
        registerHook(HookChangePackageInstaller())
        registerHook(HookChangePermissionController())

        //DEVELOPMENT
        if (BuildConfig.DEBUG) {
            registerHook(HookApplyKoreanLanguage())
            registerHook(HookRemoveNotificationBlock())
        }
    }

    fun getHook(hookClass: Class<out HookBase>): HookBase? {
        return hooks.find { it::class.java == hookClass }
    }

    fun getAllHooks(): List<HookBase> {
        return hooks
    }
}
