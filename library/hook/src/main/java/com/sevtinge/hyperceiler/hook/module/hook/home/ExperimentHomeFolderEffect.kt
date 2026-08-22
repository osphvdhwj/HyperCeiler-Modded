package com.sevtinge.hyperceiler.hook.module.hook.home

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object ExperimentHomeFolderEffect : BaseHook() {
    override fun init() {
        try {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.FolderInfo",
                lpparam.classLoader,
                "isOpened",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        // Dummy logic
                    }
                }
            )
        } catch (e: Throwable) {
        }
    }
}
