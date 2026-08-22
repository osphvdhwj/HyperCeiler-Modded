package com.sevtinge.hyperceiler.hook.module.hook.home

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object ExperimentHomeIconShadow : BaseHook() {
    override fun init() {
        try {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.ShortcutIcon",
                lpparam.classLoader,
                "onDraw",
                android.graphics.Canvas::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                    }
                }
            )
        } catch (e: Throwable) {
        }
    }
}
