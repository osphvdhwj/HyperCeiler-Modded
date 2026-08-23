package com.sevtinge.hyperceiler.hook.module.hook.home

import android.app.Activity
import android.os.Build
import android.view.WindowInsets
import android.view.WindowInsetsController
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Hide status bar when on home screen desktop.
 */
object ExperimentHomeStatusBarHide : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.Launcher",
                classLoader,
                "onResume",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as? Activity ?: return
                        runCatching {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
                                activity.window.insetsController?.systemBarsBehavior =
                                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            } else {
                                @Suppress("DEPRECATION")
                                activity.window.decorView.systemUiVisibility =
                                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            }
                        }
                    }
                }
            )
        }
    }
}
