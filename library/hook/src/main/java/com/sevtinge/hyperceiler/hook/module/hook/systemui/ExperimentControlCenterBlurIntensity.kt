package com.sevtinge.hyperceiler.hook.module.hook.systemui

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Control Center blur intensity customization.
 */
object ExperimentControlCenterBlurIntensity : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.systemui.statusbar.policy.BlurUtils",
                classLoader,
                "blurRadiusOfRatio",
                Float::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val ratio = param.args[0] as? Float ?: return
                        param.result = ratio * 100f
                    }
                }
            )
        }
    }
}
