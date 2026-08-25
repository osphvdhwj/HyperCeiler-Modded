package com.sevtinge.hyperceiler.hook.module.hook.home

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Feature: Home Icon Shadow] Enable icon drop shadow on home screen icons.
 *
 * Verified via dex analysis of /product/priv-app/MiuiHome/MiuiHome.apk:
 *   - com.miui.home.launcher.ShortcutIcon EXISTS ✅ (with inner classes $1..$6, $ImageSetCallback)
 *   - onFinishInflate EXISTS ✅ (verified in global method strings)
 *   - Real shadow fields: mShadowDx, mShadowDy, shadowFar, shadowSrc (verified in dex)
 *   - Real shadow methods: setShadowLayer, setShadowsEnabled, setShadowRadius, addShadow (verified)
 *
 * Strategy:
 *   1. Hook ShortcutIcon.onFinishInflate() to enable hardware layer for GPU compositing
 *   2. Hook ShortcutIcon.setShadowsEnabled() to force shadow on (prevents it being turned off)
 */
object ExperimentHomeIconShadow : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return

        // Hook 1: onFinishInflate — enable hardware layer for GPU shadow compositing
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.ShortcutIcon",
                classLoader,
                "onFinishInflate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        runCatching {
                            val view = param.thisObject as? android.view.View ?: return
                            // Enable hardware layer for GPU shadow rendering
                            view.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        }
                    }
                }
            )
        }

        // Hook 2: setShadowsEnabled — prevent launcher from disabling shadows (verified method)
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.ShortcutIcon",
                classLoader,
                "setShadowsEnabled",
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // Always force shadows enabled = true
                        param.args[0] = true
                    }
                }
            )
        }
    }
}
