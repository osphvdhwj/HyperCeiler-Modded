package com.sevtinge.hyperceiler.hook.module.hook.home.drawer
import com.sevtinge.hyperceiler.hook.utils.hookAfterAllMethods
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import com.sevtinge.hyperceiler.hook.utils.getObjectField

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD
import android.content.Context

object DrawerIconSize : BaseHook() {
    override fun init() {
        val iconSizeDp = mPrefsMap.getInt("prefs_key_home_drawer_icon_size", 0)
        val fontScale = mPrefsMap.getInt("prefs_key_home_drawer_font_size", 0)

        if (iconSizeDp == 0 && fontScale == 0) return

        logD("DrawerIconSize", "Applying Drawer Icon: ${iconSizeDp}dp, Font: ${fontScale}%")

        // Hook AllAppsGridAdapter (Drawer grid items)
        val adapterClass = findClassIfExists("com.miui.home.launcher.allapps.AllAppsGridAdapter")
        if (adapterClass != null) {
            try {
                // MIUI uses createIconView or onBindViewHolder to set sizes
                adapterClass.hookAfterAllMethods("onBindViewHolder") { param ->
                    val holder = param.args[0]
                    val itemView = runCatching {
                        holder.getObjectField("itemView") as android.view.View
                    }.getOrNull() ?: return@hookAfterAllMethods

                    if (iconSizeDp > 0) {
                        val iconView = itemView.findViewById<android.view.View>(android.app.itemView.context.resources.getIdentifier("icon", "id", "com.miui.home"))
                        iconView?.let {
                            val params = it.layoutParams
                            val px = (iconSizeDp * it.context.resources.displayMetrics.density).toInt()
                            params.width = px
                            params.height = px
                            it.layoutParams = params
                        }
                    }

                    if (fontScale > 0) {
                        val textView = itemView as? android.widget.TextView
                        textView?.textSize = fontScale.toFloat()
                    }
                }
                logD("DrawerIconSize", "Hooked AllAppsGridAdapter")
            } catch (t: Throwable) {
                logD("DrawerIconSize", "Failed: ${t.message}")
            }
        }
    }
}
