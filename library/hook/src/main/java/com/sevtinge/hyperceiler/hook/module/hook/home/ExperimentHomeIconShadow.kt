package com.sevtinge.hyperceiler.hook.module.hook.home

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Home icon shadow customization.
 * Safely enables icon shadow layer without per-frame onDraw reflection overhead.
 */
object ExperimentHomeIconShadow : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.ShortcutIcon",
                classLoader,
                "onFinishInflate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val view = param.thisObject as? android.view.View ?: return
                            view.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        }
                    }
                }
            )
        }
    }
}
