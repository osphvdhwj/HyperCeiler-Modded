package com.sevtinge.hyperceiler.hook.module.hook.home

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object ExperimentHomeStatusBarHide : BaseHook() {
    override fun init() {
        try {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.Launcher",
                lpparam.classLoader,
                "onResume",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as? android.app.Activity ?: return
                        activity.window.decorView.systemUiVisibility = 
                            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                    }
                }
            )
        } catch (e: Throwable) {
        }
    }
}
