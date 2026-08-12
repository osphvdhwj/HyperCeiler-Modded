package com.harry.hyperhand.hook.module.hook.home.recent

import android.graphics.Canvas
import android.view.ViewGroup
import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.utils.hookBeforeMethod
import kotlin.math.max

object IosRecentStack : BaseHook() {
    override fun init() {
        val enableIosStack = mPrefsMap.getBoolean("home_recent_ios_stack_enable", true)
        if (!enableIosStack) return

        "com.miui.home.recents.views.RecentsView".hookBeforeMethod(
            "dispatchDraw", Canvas::class.java
        ) {
            val recentsView = it.thisObject as ViewGroup
            val screenCenter = recentsView.width / 2f
            val childCount = recentsView.childCount

            for (i in 0 until childCount) {
                val taskView = recentsView.getChildAt(i)
                
                // Exclude the empty text view and other non-task overlays
                if (!taskView.javaClass.name.contains("TaskView", ignoreCase = true)) continue

                // Base center of the child view
                val taskCenter = taskView.left + (taskView.width / 2f)
                val distanceFromCenter = taskCenter - screenCenter
                
                // Normalize distance relative to task width
                val ratio = distanceFromCenter / taskView.width

                if (ratio < 0) {
                    // Task is on the left (stacking behind)
                    val scale = max(0.85f, 1f + (ratio * 0.15f))
                    taskView.scaleX = scale
                    taskView.scaleY = scale

                    // Compress X translation for the iOS overlap effect
                    val overlapFactor = 0.6f 
                    taskView.translationX = -(ratio * taskView.width * overlapFactor)

                    // Fix Z-elevation so tasks further left are under the center tasks
                    taskView.z = i.toFloat()
                    
                    // Fade out tasks that go too far back
                    taskView.alpha = max(0f, 1f + ratio)
                } else {
                    // Task is centered or on the right
                    taskView.scaleX = 1f
                    taskView.scaleY = 1f
                    taskView.translationX = 0f
                    
                    // Standard Z-order
                    taskView.z = i.toFloat()
                    taskView.alpha = 1f
                }
            }
        }
    }
}
