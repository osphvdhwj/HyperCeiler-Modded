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

        // Untouched settings: leave the launcher's themed pill alone
        if (cornerRadius == 24 && alpha == 255) {
            logD("SearchBarCustomization", "defaults, skip")
            return
        }

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

            // Reuse the launcher's own drawable so the pill keeps its themed tint
            val existing = view.background?.mutate()
            if (existing is GradientDrawable) {
                existing.cornerRadius = radiusPx.toFloat()
                existing.alpha = alpha
                view.background = existing
                logD("SearchBarCustomization", "Existing background adjusted")
                return
            }

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = radiusPx.toFloat()
                setColor(Color.argb(alpha, 255, 255, 255))
            }
            view.background = drawable
            logD("SearchBarCustomization", "Fallback background applied")
        } catch (t: Throwable) {
            logD("SearchBarCustomization", "Failed: ${t.message}")
        }
    }
}
