package com.sevtinge.hyperceiler.hook.module.hook.home.title
import com.sevtinge.hyperceiler.hook.utils.hookAfterAllMethods
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import com.sevtinge.hyperceiler.hook.utils.getObjectField

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
            if (self.visibility != View.GONE) {
                self.visibility = View.GONE
                self.requestLayout()
            }
        }
        logD("HideAppLabels", "Labels will be hidden (View.GONE)")
    }
}
