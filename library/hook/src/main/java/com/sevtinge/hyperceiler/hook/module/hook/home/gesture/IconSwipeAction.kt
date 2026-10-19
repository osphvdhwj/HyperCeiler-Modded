package com.sevtinge.hyperceiler.hook.module.hook.home.gesture

import android.view.MotionEvent
import android.view.View
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object IconSwipeAction : BaseHook() {
    private const val SWIPE_THRESHOLD_DP = 40f

    override fun init() {
        val bubbleClass = findClassIfExists("com.miui.home.launcher.BubbleTextView") ?: return
        
        bubbleClass.hookBeforeAllMethods("onTouchEvent") { param ->
            val view = param.thisObject as? View ?: return@hookBeforeAllMethods
            val event = param.args[0] as? MotionEvent ?: return@hookBeforeAllMethods

            // Only act on the Home Screen (Workspace), not the App Drawer
            val parent = view.parent
            if (parent?.javaClass?.name?.contains("Workspace") != true) {
                return@hookBeforeAllMethods
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Store start Y directly on the view to prevent memory leaks
                    view.setTag(123456789, event.rawY)
                }
                MotionEvent.ACTION_UP -> {
                    val startY = view.getTag(123456789) as? Float ?: return@hookBeforeAllMethods
                    view.setTag(123456789, null) // Clear

                    val deltaY = startY - event.rawY
                    val thresholdPx = SWIPE_THRESHOLD_DP * view.context.resources.displayMetrics.density

                    if (deltaY > thresholdPx) {
                        logD("IconSwipeAction", "Swipe up detected, opening App Drawer")
                        param.result = true // Consume event (prevents app launch)
                        
                        // Open the App Drawer using MIUI's native method
                        val launcher = view.context
                        runCatching {
                            launcher.javaClass.getMethod("showAllApps", Boolean::class.java).invoke(launcher, true)
                        }.onFailure {
                            logD("IconSwipeAction", "showAllApps failed: ${it.message}")
                        }
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.setTag(123456789, null) // Clear
                }
            }
        }
        logD("IconSwipeAction", "Hook installed")
    }
}
