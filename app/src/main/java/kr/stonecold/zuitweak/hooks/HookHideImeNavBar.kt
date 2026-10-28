package kr.stonecold.zuitweak.hooks

import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kr.stonecold.zuitweak.R
import kr.stonecold.zuitweak.common.*

/**
 * Hides Gboard's bottom navigation row — the "hide keyboard" chevron and the input-method
 * switcher.
 *
 * The row belongs to the IME, not to SystemUI: the InputMethod window extends to the bottom
 * of the screen and SystemUI hides its navigation bar while the keyboard is up. Gboard
 * builds the row as `NavigationBarFrame` → `NavigationBarView` → `NavigationBarInflaterView`.
 */
class HookHideImeNavBar : HookBaseHandleLoadPackage() {
    override val menuItem
        get() = HookMenuItem(
            category = HookMenuCategory.UNFUCKZUI,
            title = LanguageUtil.getString(R.string.hook_hide_ime_nav_bar_title),
            description = LanguageUtil.getString(R.string.hook_hide_ime_nav_bar_desc),
            defaultSelected = false,
        )

    override val hookTargetDevice: Array<String> = emptyArray()
    override val hookTargetRegion: Array<String> = emptyArray()
    override val hookTargetVersion: Array<String> = emptyArray()

    override val hookTargetPackage: Array<String> = arrayOf("com.google.android.inputmethod.latin")
    override val hookTargetPackageOptional: Array<String> = emptyArray()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.google.android.inputmethod.latin") return

        val callback = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val service = param.thisObject as InputMethodService
                    val decor = service.window?.window?.decorView ?: return
                    schedule(decor, 0)
                } catch (t: Throwable) {
                    Log.w(TAG, "hook failed: ${t.message}")
                }
            }
        }

        try {
            XposedHelpers.findAndHookMethod(
                "android.inputmethodservice.InputMethodService",
                lpparam.classLoader,
                "onStartInputView",
                EditorInfo::class.java,
                Boolean::class.javaPrimitiveType,
                callback
            )
            XposedHelpers.findAndHookMethod(
                "android.inputmethodservice.InputMethodService",
                lpparam.classLoader,
                "onWindowShown",
                callback
            )
        } catch (t: Throwable) {
            XposedUtil.handleHookException(tag, t, "InputMethodService", "onStartInputView")
        }
    }

    /** The window is not laid out yet when onStartInputView fires; retry briefly. */
    private fun schedule(root: View, attempt: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (root.height == 0 && attempt < 6) {
                    schedule(root, attempt + 1)
                    return@postDelayed
                }
                apply(root)
            } catch (t: Throwable) {
                Log.w(TAG, "apply failed: ${t.message}")
            }
        }, 400L)
    }

    private fun apply(root: View) {
        val frame = findFrame(root)
        if (frame == null) {
            Log.i(TAG, "navigation row not found")
            return
        }
        val parent = frame.parent as? ViewGroup ?: return
        val rowHeight = frame.height

        // The decor stacks: keyboard content, a 120 px spacer View, and NavigationBarFrame.
        // Hide the frame and any sibling that occupies the same bottom slot.
        var keyboard: View? = null
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val sameSlot = child.top == frame.top && child.height == rowHeight
            if (child === frame || sameSlot) {
                if (child.visibility != View.GONE) {
                    child.visibility = View.GONE
                    child.layoutParams?.let { params ->
                        params.height = 0
                        child.layoutParams = params
                    }
                }
            } else if (child.visibility == View.VISIBLE && child.height > 0) {
                if (keyboard == null || child.height > keyboard.height) keyboard = child
            }
        }

        // Give the freed strip to the keyboard instead of leaving it empty.
        keyboard?.let { view ->
            if (view.height != parent.height) {
                view.layoutParams?.let { params ->
                    params.height = parent.height
                    view.layoutParams = params
                }
                view.requestLayout()
                Log.i(TAG, "keyboard stretched to ${parent.height} (row $rowHeight)")
            }
        }
        parent.requestLayout()
    }

    /**
     * Gboard names the container `NavigationBarFrame` and the inner view `NavigationBarView`.
     * Fall back to the parent of the inner view if the outer name ever changes.
     */
    private fun findFrame(root: View): View? {
        var inner: View? = null

        fun visit(view: View) {
            if (inner != null) return
            when (view.javaClass.simpleName) {
                "NavigationBarFrame" -> {
                    inner = view
                    return
                }
                "NavigationBarView" -> inner = (view.parent as? View) ?: view
            }
            (view as? ViewGroup)?.let { group ->
                for (i in 0 until group.childCount) visit(group.getChildAt(i))
            }
        }

        visit(root)
        return inner
    }

    private companion object {
        const val TAG = "ZuiTweakIme"
    }
}
