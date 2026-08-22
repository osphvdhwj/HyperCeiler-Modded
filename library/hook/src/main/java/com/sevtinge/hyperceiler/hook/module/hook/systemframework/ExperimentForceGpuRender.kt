package com.sevtinge.hyperceiler.hook.module.hook.systemframework

import android.view.WindowManager
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Force GPU hardware acceleration for all windows.
 * Hooks WindowManagerService to ensure hardware-accelerated rendering is always enabled.
 */
object ExperimentForceGpuRender : BaseHook() {
    override fun init() {
        // Hook WindowManagerService to always set hardware-accelerated flag
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.server.wm.WindowState",
                lpparam.classLoader,
                "isHardwareAccelerated",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                }
            )
        }

        // Force FLAG_HARDWARE_ACCELERATED on all window layouts
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.server.wm.WindowManagerService",
                lpparam.classLoader,
                "addWindow",
                "com.android.server.wm.Session",
                android.view.IWindow::class.java,
                android.view.WindowManager.LayoutParams::class.java,
                Int::class.java,
                Int::class.java,
                android.view.InsetsState::class.java,
                android.view.InputChannel::class.java,
                android.view.InsetsSourceControl.Array::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val lp = param.args[2] as? WindowManager.LayoutParams ?: return
                        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        param.args[2] = lp
                    }
                }
            )
        }
    }
}
