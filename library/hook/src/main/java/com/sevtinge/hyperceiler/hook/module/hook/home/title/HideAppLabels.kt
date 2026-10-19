package com.sevtinge.hyperceiler.hook.module.hook.home.title

import android.view.View
import android.widget.TextView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object HideAppLabels : BaseHook() {
    override fun init() {
        // Hook BubbleTextView (Home Screen & Drawer icons)
        val bubbleClass = findClassIfExists("com.miui.home.launcher.BubbleTextView") ?: return
        
        // Intercept when the text is set, and immediately hide it
        bubbleClass.hookAfterAllMethods("setText") { param ->
            val self = param.thisObject as? TextView ?: return@hookAfterAllMethods
            self.visibility = View.GONE
        }
        logD("HideAppLabels", "Labels will be hidden (View.GONE)")
    }
}
