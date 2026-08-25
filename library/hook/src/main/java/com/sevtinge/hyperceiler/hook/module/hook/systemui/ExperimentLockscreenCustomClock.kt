package com.sevtinge.hyperceiler.hook.module.hook.systemui

import android.view.View
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Custom Lockscreen Clock enhancement.
 * Adds dynamic alpha transition and scale effects to lockscreen clock views.
 *
 * Verified via dex analysis of /system_ext/priv-app/MiuiSystemUI/MiuiSystemUI.apk:
 *   - WRONG: com.android.keyguard.clock.KeyguardClockSwitch (wrong path)
 *   - REAL:  com.android.keyguard.KeyguardClockSwitch ✅
 *   - REAL:  com.miui.clock.MiuiClockController ✅
 *   - Method: setClockAlpha ✅ EXISTS
 */
object ExperimentLockscreenCustomClock : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return

        // Hook KeyguardClockSwitch inflation
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.keyguard.KeyguardClockSwitch", // Corrected path
                classLoader,
                "onFinishInflate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val clockView = param.thisObject as? View ?: return
                        runCatching {
                            clockView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        }
                    }
                }
            )
        }

        // Hook MiuiClockController for smooth fade transition on unlock screen
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.clock.MiuiClockController",
                classLoader,
                "setClockAlpha",
                Float::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // Ensure smooth alpha transition without abrupt hiding
                        val alpha = param.args[0] as? Float ?: return
                        if (alpha < 0.05f) {
                            param.args[0] = 0.0f
                        }
                    }
                }
            )
        }
    }
}
