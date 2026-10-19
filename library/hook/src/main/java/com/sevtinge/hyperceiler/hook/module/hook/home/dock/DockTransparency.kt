package com.sevtinge.hyperceiler.hook.module.hook.home.dock

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookAfterAllMethods
import com.sevtinge.hyperceiler.hook.utils.hookAllMethods
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object DockTransparency : BaseHook() {
    override fun init() {
        // HotSeats is the class that manages the Dock (including its background)
        val hotSeatsClass = findClassIfExists("com.miui.home.launcher.hotseats.HotSeats") ?: return
        
        hotSeatsClass.hookAfterAllMethods("onFinishInflate") { param ->
            val self = param.thisObject as? ViewGroup ?: return@hookAfterAllMethods
            try {
                // Make the background completely transparent
                self.background = ColorDrawable(Color.TRANSPARENT)
                
                // Also check for a child background view that some ROMs use
                val bgChild = self.findViewById<View>(
                    self.context.resources.getIdentifier("dock_background", "id", "com.miui.home")
                )
                bgChild?.background = ColorDrawable(Color.TRANSPARENT)
                
                logD("DockTransparency", "Dock background cleared")
            } catch (t: Throwable) {
                logD("DockTransparency", "Failed: ${t.message}")
            }
        }
    }
}
