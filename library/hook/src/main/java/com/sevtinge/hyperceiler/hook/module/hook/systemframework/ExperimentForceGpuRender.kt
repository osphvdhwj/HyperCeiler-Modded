package com.sevtinge.hyperceiler.hook.module.hook.systemframework

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Force GPU hardware acceleration for all windows.
 * Hooks WindowManagerService internals to ensure hardware-accelerated rendering is always enabled.
 */
object ExperimentForceGpuRender : BaseHook() {
    override fun init() {
        // Hook WindowState.isHardwareAccelerated() to always return true
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

        // Hook WindowManagerService to force hardware acceleration flag via system ready
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.server.wm.WindowManagerService",
                lpparam.classLoader,
                "systemReady",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        // Ensure GPU rendering is never disabled after system ready
                        runCatching {
                            XposedHelpers.callMethod(param.thisObject, "enableScreenAfterBoot")
                        }
                    }
                }
            )
        }
    }
}
