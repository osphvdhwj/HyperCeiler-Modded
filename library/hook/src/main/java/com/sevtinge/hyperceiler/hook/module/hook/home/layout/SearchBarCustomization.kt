package com.sevtinge.hyperceiler.hook.module.hook.home.layout

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookAfterAllMethods
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object SearchBarCustomization : BaseHook() {
    override fun init() {
        val cornerRadius = mPrefsMap.getInt("prefs_key_home_searchbar_radius", 24)
        val alpha = mPrefsMap.getInt("prefs_key_home_searchbar_alpha", 255)

        logD("SearchBarCustomization", "Applying: radius=${cornerRadius}dp, alpha=$alpha")

        // Hook the SearchBar view
        val searchBarClass = findClassIfExists("com.miui.home.launcher.SearchBar") ?: return
        
        searchBarClass.hookAfterAllMethods("onFinishInflate") { param ->
            val self = param.thisObject as? View ?: return@hookAfterAllMethods
            applyCustomBackground(self, cornerRadius, alpha)
        }

        // Sometimes MIUI resets the background on configuration changes
        searchBarClass.hookAfterAllMethods("onConfigurationChanged") { param ->
            val self = param.thisObject as? View ?: return@hookAfterAllMethods
            applyCustomBackground(self, cornerRadius, alpha)
        }
    }

    private fun applyCustomBackground(view: View, radiusDp: Int, alpha: Int) {
        try {
            val ctx = view.context
            val radiusPx = (radiusDp * ctx.resources.displayMetrics.density).toInt()
            
            // Create a new drawable based on the user's preference
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = radiusPx.toFloat()
                
                // Use a semi-transparent white/dark color based on current alpha
                // If alpha is 0, it's fully transparent
                val color = Color.argb(alpha, 255, 255, 255) // White base
                setColor(color)
            }
            
            view.background = drawable
            logD("SearchBarCustomization", "Background applied")
        } catch (t: Throwable) {
            logD("SearchBarCustomization", "Failed: ${t.message}")
        }
    }
}
