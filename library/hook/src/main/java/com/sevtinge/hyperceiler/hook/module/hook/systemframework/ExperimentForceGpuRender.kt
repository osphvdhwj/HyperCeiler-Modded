package com.sevtinge.hyperceiler.hook.module.hook.systemframework

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Force GPU hardware acceleration for system windows.
 * Safely initializes hardware acceleration flags without per-frame WindowState hooks.
 */
object ExperimentForceGpuRender : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.server.wm.WindowManagerService",
                classLoader,
                "systemReady",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val context = XposedHelpers.getObjectField(param.thisObject, "mContext") as? android.content.Context
                            context?.let {
                                android.provider.Settings.Global.putInt(
                                    it.contentResolver,
                                    "hardware_accelerated_main_ui", 1
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
