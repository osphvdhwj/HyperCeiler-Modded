package com.sevtinge.hyperceiler.hook.module.hook.home

import android.view.View
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Feature: Home Folder 3D Effect] Smooth scale+alpha animation when opening home screen folders.
 *
 * Verified via dex analysis of /product/priv-app/MiuiHome/MiuiHome.apk:
 *   - com.miui.home.launcher.Folder EXISTS — but method "animateOpen" does NOT exist.
 *     Real folder open method is: "open", "openAnimUpdate", "onOpenAnimationFinished"
 *   - com.miui.home.launcher.FolderIcon EXISTS
 *     But "onFolderIconClick" does NOT exist.
 *     Real click method: "onIconClick"
 *   - Real animation engine: com.miui.home.launcher.folder.FolderAnimController
 *     with methods: startAnim, breakOpenAnim, breakOpenAnimIfNeeded
 *
 * Strategy:
 *   1. Hook Folder.open() — triggered when the folder is tapped and begins to open
 *   2. Hook FolderIcon.onIconClick() — for micro-bounce press feedback
 *   3. Hook FolderAnimController for the open animation start
 */
object ExperimentHomeFolderEffect : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return

        // Hook 1: Folder.open() — real entry point when folder opens
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.Folder",
                classLoader,
                "open",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val folder = param.thisObject as? View ?: return
                        // Apply 3D depth entrance: start from scaled-down state
                        folder.scaleX = 0.88f
                        folder.scaleY = 0.88f
                        folder.alpha = 0f
                        folder.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .alpha(1.0f)
                            .setDuration(260)
                            .setInterpolator(android.view.animation.DecelerateInterpolator(1.5f))
                            .start()
                    }
                }
            )
        }

        // Hook 2: FolderIcon.onIconClick() — real click handler (verified in dex)
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.FolderIcon",
                classLoader,
                "onIconClick",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val icon = param.thisObject as? View ?: return
                        // Micro-bounce press animation on tap
                        icon.animate()
                            .scaleX(0.92f)
                            .scaleY(0.92f)
                            .setDuration(80)
                            .withEndAction {
                                icon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                            }
                            .start()
                    }
                }
            )
        }

        // Hook 3: FolderAnimController.startAnim() — intercept open animation start
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.folder.FolderAnimController",
                classLoader,
                "startAnim",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        // Animation has started - no override needed, just logging point
                    }
                }
            )
        }
    }
}
