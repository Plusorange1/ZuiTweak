package kr.stonecold.zuitweak.hooks

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Horizontal slide on the status bar to change screen brightness.
 *
 * The status bar view receives the touches for the top strip and forwards vertical drags to
 * the shade. A horizontal drag is intercepted here, converted into a 0..255 brightness value
 * and written to Settings.System (SystemUI holds WRITE_SETTINGS).
 */
class HookStatusBarBrightness : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.UNFUCKZUI,
            title = LanguageUtil.getString(R.string.hook_statusbar_brightness_title),
            description = LanguageUtil.getString(R.string.hook_statusbar_brightness_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.android.systemui")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    private var downX = 0f
    private var downY = 0f
    private var startBrightness = 0
    private var tracking = false
    private var active = false
    private var lastWrite = 0L
    private var lastValue = -1

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.systemui") return

        val statusBarView = "com.android.systemui.statusbar.phone.PhoneStatusBarView"
        val intercept = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val view = param.thisObject as View
                    val event = param.args[0] as MotionEvent
                    if (handle(view, event)) param.result = true
                } catch (t: Throwable) {
                    XposedUtil.handleHookException(tag, t, statusBarView, "onInterceptTouchEvent")
                }
            }
        }
        val touch = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    val view = param.thisObject as View
                    val event = param.args[0] as MotionEvent
                    if (handle(view, event)) param.result = true
                } catch (t: Throwable) {
                    XposedUtil.handleHookException(tag, t, statusBarView, "onTouchEvent")
                }
            }
        }

        try {
            XposedHelpers.findAndHookMethod(
                statusBarView, lpparam.classLoader, "onInterceptTouchEvent", MotionEvent::class.java, intercept
            )
            XposedHelpers.findAndHookMethod(
                statusBarView, lpparam.classLoader, "onTouchEvent", MotionEvent::class.java, touch
            )
        } catch (t: Throwable) {
            XposedUtil.handleHookException(tag, t, statusBarView, "onTouchEvent")
        }
    }

    /** Returns true when the event belongs to the brightness gesture and must be consumed. */
    private fun handle(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                startBrightness = currentBrightness(view.context)
                tracking = true
                active = false
                lastValue = -1
            }

            MotionEvent.ACTION_MOVE -> {
                if (!tracking) return false
                val dx = event.x - downX
                val dy = event.y - downY
                val slop = ViewConfiguration.get(view.context).scaledTouchSlop

                if (!active && abs(dx) > slop && abs(dx) > abs(dy) * 1.5f) {
                    active = true
                    setManualMode(view.context)
                    XposedUtil.xposedDebug(tag, "brightness gesture started")
                }
                if (active) {
                    val width = view.width.coerceAtLeast(1)
                    val value = (startBrightness + dx / width * 255f)
                        .roundToInt()
                        .coerceIn(1, 255)
                    writeBrightness(view.context, value)
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val consumed = active
                tracking = false
                active = false
                if (consumed) {
                    XposedUtil.xposedDebug(tag, "brightness=${currentBrightness(view.context)}")
                }
                return consumed
            }
        }
        return false
    }

    private fun currentBrightness(context: Context): Int =
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)

    private fun setManualMode(context: Context) {
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        } catch (_: Throwable) {
            // Ignore: brightness still applies through SCREEN_BRIGHTNESS.
        }
    }

    private fun writeBrightness(context: Context, value: Int) {
        val now = SystemClock.uptimeMillis()
        if (value == lastValue || now - lastWrite < 16L) return
        lastWrite = now
        lastValue = value
        try {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
        } catch (t: Throwable) {
            XposedUtil.handleHookException(tag, t, "Settings.System", "putInt")
        }
    }
}
