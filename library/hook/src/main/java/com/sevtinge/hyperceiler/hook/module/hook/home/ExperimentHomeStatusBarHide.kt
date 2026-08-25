package com.sevtinge.hyperceiler.hook.module.hook.home

import android.app.Activity
import android.os.Build
import android.view.WindowInsets
import android.view.WindowInsetsController
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Feature: Hide Status Bar on Desktop] Hide status bar when on the home screen desktop.
 *
 * Verified via dex analysis of /product/priv-app/MiuiHome/MiuiHome.apk:
 *   - com.miui.home.launcher.Launcher EXISTS, onResume EXISTS ✅
 *   - Real status bar control methods: showStatusBar, showStatusBarDelay (real methods)
 *   - mIsShowStatusBar, mShowStatusBar (real fields)
 *   - hideSystemBars (real method)
 *   - isHideStatusBarWhenEnterRecents (real boolean method)
 *
 * Strategy:
 *   1. Hook Launcher.onResume() to hide status bar on enter (existing approach is correct)
 *   2. Hook Launcher.showStatusBar() to PREVENT it from re-showing the bar
 *      (this fixes the bug where status bar reappears after notifications or recents)
 */
object ExperimentHomeStatusBarHide : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return

        // Hook 1: Hide status bar on resume (verified - Launcher.onResume exists)
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
                                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            }
                        }
                    }
                }
            )
        }

        // Hook 2: Prevent showStatusBar() from re-showing the bar (verified method in dex)
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.Launcher",
                classLoader,
                "showStatusBar",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // Block any call to re-show the status bar on desktop
                        param.result = null
                    }
                }
            )
        }

        // Hook 3: showStatusBarDelay also suppressed (verified in dex)
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.Launcher",
                classLoader,
                "showStatusBarDelay",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = null
                    }
                }
            )
        }
    }
}
