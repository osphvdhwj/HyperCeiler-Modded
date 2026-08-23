package com.sevtinge.hyperceiler.hook.module.hook.home

import android.view.View
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Adds smooth 3D scale and alpha animation when opening home screen folders.
 */
object ExperimentHomeFolderEffect : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return

        // Hook Folder open animation to add 3D depth effect
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.Folder",
                classLoader,
                "animateOpen",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val folder = param.thisObject as? View ?: return
                        folder.scaleX = 0.85f
                        folder.scaleY = 0.85f
                        folder.alpha = 0f
                        folder.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .alpha(1.0f)
                            .setDuration(280)
                            .setInterpolator(android.view.animation.DecelerateInterpolator())
                            .start()
                    }
                }
            )
        }

        // Hook FolderIcon click for micro-bounce animation
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.miui.home.launcher.FolderIcon",
                classLoader,
                "onFolderIconClick",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val icon = param.thisObject as? View ?: return
                        icon.animate()
                            .scaleX(0.92f)
                            .scaleY(0.92f)
                            .setDuration(100)
                            .withEndAction {
                                icon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
                            }
                            .start()
                    }
                }
            )
        }
    }
}
