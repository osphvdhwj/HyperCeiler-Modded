package com.sevtinge.hyperceiler.hook.module.hook.home.gesture

import android.view.MotionEvent
import android.view.View
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object IconSwipeAction : BaseHook() {
    override fun init() {
        // Target BubbleTextView (the standard app icon view in MIUI)
        val bubbleClass = findClassIfExists("com.miui.home.launcher.BubbleTextView") ?: return
        
        val downY = HashMap<View, Float>()

        bubbleClass.hookBeforeAllMethods("onTouchEvent") { param ->
            val view = param.thisObject as? View ?: return@hookBeforeAllMethods
            val event = param.args[0] as? MotionEvent ?: return@hookBeforeAllMethods

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downY[view] = event.rawY
                }
                MotionEvent.ACTION_UP -> {
                    val startY = downY[view] ?: return@hookBeforeAllMethods
                    val deltaY = startY - event.rawY

                    if (deltaY > 80) { // Swiped up by at least 80 pixels
                        logD("IconSwipeAction", "Swipe Up detected on icon")
                        downY.remove(view)
                        
                        // Consume the event so it doesn't launch the app
                        param.result = true
                        
                        // TODO: Execute action (e.g., Open App Drawer, Lock Screen)
                        // For now, we just log it to verify it works.
                    } else {
                        downY.remove(view)
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    downY.remove(view)
                }
            }
        }
        logD("IconSwipeAction", "Hook installed")
    }
}
