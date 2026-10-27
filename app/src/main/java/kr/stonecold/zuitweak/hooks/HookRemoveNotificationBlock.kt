package kr.stonecold.zuitweak.hooks

import android.os.Parcel
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

@Suppress("unused")
class HookRemoveNotificationBlock : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.DEVELOPMENT,
            //title = LanguageUtil.getString(R.string.hook_enable_zui_camera_shutter_option_title),
            //description = LanguageUtil.getString(R.string.hook_enable_zui_camera_shutter_option_desc),
            title = LanguageUtil.getString(R.string.hook_remove_notification_block_title),
            description = LanguageUtil.getString(R.string.hook_remove_notification_block_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("android", "com.android.settings")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                hookNotificationManagerCreateNotificationChannels(lpparam)
                //hookNotificationChannel(lpparam)
                //hookNotificationChannel2(lpparam)
            }
            "com.android.settings" -> {
                hookBlockPreferenceControllerIsAvailable(lpparam)
                hookConversationPriorityPreferenceControllerIsAvailable(lpparam)
                hookHighImportancePreferenceControllerIsAvailable(lpparam)
                hookMinImportancePreferenceControllerIsAvailable(lpparam)
                hookNotificationsOffPreferenceControllerIsAvailable(lpparam)
            }
        }
    }

    private fun hookNotificationManagerCreateNotificationChannels(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "android.app.NotificationManager"
        val methodName = "createNotificationChannels"
        val parameterTypes = arrayOf<Any>( MutableList::class.java)
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val list = param.args[0] as MutableList<*>
                    for(noti in list) {
                        val mId = XposedHelpers.getObjectField(noti, "mId") as String
                        if (mId == "USB") {
                            XposedHelpers.setBooleanField(noti, "mBlockableSystem", false)
                            XposedHelpers.setIntField(noti, "mImportance", 2)
                        }
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
    private fun hookNotificationChannel(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "android.app.NotificationChannel"
        val methodName = ""
        val parameterTypes = arrayOf<Any>( String::class.java, CharSequence::class.java, Int::class.java )
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val mId = XposedHelpers.getObjectField(param.thisObject, "mId") as String
                    if (param.args[0] == "USB" || mId == "USB") {
                        XposedUtil.xposedDebug(tag, "$className.$methodName ${param.args[0]} $mId")
                        XposedHelpers.setIntField(param.thisObject, "mImportance", -1000)
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookNotificationChannel2(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "android.app.NotificationChannel"
        val methodName = ""
        val parameterTypes = arrayOf<Any>(  Parcel::class.java)
        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val mId = XposedHelpers.getObjectField(param.thisObject, "mId") as String
                    if (mId == "USB") {
                        XposedUtil.xposedDebug(tag, "$className.$methodName $mId")
                        XposedHelpers.setIntField(param.thisObject, "mImportance", -1000)
                    }
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookBlockPreferenceControllerIsAvailable(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.notification.app.BlockPreferenceController"
        val methodName = "isAvailable"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "$className.$methodName")
                    param.result = true
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookConversationPriorityPreferenceControllerIsAvailable(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.notification.app.ConversationPriorityPreferenceController"
        val methodName = "isAvailable"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "$className.$methodName")
                    param.result = true
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookHighImportancePreferenceControllerIsAvailable(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.notification.app.HighImportancePreferenceController"
        val methodName = "isAvailable"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "$className.$methodName")
                    param.result = true
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookMinImportancePreferenceControllerIsAvailable(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.notification.app.MinImportancePreferenceController"
        val methodName = "isAvailable"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "$className.$methodName")
                    param.result = true
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }

    private fun hookNotificationsOffPreferenceControllerIsAvailable(lpparam: XC_LoadPackage.LoadPackageParam) {
        val className = "com.android.settings.notification.app.NotificationsOffPreferenceController"
        val methodName = "isAvailable"
        val parameterTypes = emptyArray<Any>()
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    XposedUtil.xposedDebug(tag, "$className.$methodName")
                    param.result = true
                } catch (e: Throwable) {
                    XposedUtil.handleHookException(tag, e, className, methodName, *parameterTypes)
                }
            }
        }

        XposedUtil.executeHook(tag, lpparam, className, methodName, *parameterTypes, callback)
    }
}
