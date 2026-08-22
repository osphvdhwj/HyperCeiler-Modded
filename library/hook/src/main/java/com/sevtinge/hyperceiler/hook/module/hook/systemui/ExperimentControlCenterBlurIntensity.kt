package com.sevtinge.hyperceiler.hook.module.hook.systemui

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object ExperimentControlCenterBlurIntensity : BaseHook() {
    override fun init() {
        try {
            XposedHelpers.findAndHookMethod(
                "com.android.systemui.statusbar.policy.BlurUtils",
                lpparam.classLoader,
                "blurRadiusOfRatio",
                Float::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val ratio = param.args[0] as Float
                        param.result = (ratio * 100f).toInt()
                    }
                }
            )
        } catch (e: Throwable) {
        }
    }
}
