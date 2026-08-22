package com.sevtinge.hyperceiler.hook.module.hook.systemframework

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Keep high refresh rate even in power saving mode.
 * Hooks DisplayModeDirector to prevent forced low-refresh-rate policy in battery saver.
 */
object ExperimentAllowHighRefreshInLowPower : BaseHook() {
    override fun init() {
        // Hook 1: Block low-power refresh rate throttle from DisplayModeDirector
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.server.display.mode.DisplayModeDirector",
                lpparam.classLoader,
                "onBatteryChanged",
                Boolean::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // Always tell DisplayModeDirector battery is fine
                        param.args[0] = false
                    }
                }
            )
        }.onFailure {
            // Fallback for older HyperOS/MIUI: hook via DisplayPowerController
            runCatching {
                XposedHelpers.findAndHookMethod(
                    "com.android.server.display.DisplayPowerController",
                    lpparam.classLoader,
                    "shouldReduceRefreshRate",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = false
                        }
                    }
                )
            }
        }

        // Hook 2: Prevent BatterySaverStateMachine from lowering display performance
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.server.power.batterysaver.BatterySaverStateMachine",
                lpparam.classLoader,
                "setShouldAdjustRefreshRate",
                Boolean::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.args[0] = false
                    }
                }
            )
        }
    }
}
