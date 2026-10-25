package com.sevtinge.hyperceiler.hook.module.hook.securitycenter

import com.hchen.hooktool.utils.ResInjectTool
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap

/**
 * Sidebar line size & position: replaces the Security app's sidebar
 * dimension resources (width / length / edge margin, in px). A pref of
 * 0 leaves that dimension untouched.
 */
object SidebarSizeCustom : BaseHook() {

    override fun init() {
        val width = mPrefsMap.getInt("security_center_sidebar_line_width", 0)
        val length = mPrefsMap.getInt("security_center_sidebar_line_length", 0)
        val margin = mPrefsMap.getInt("security_center_sidebar_line_margin", 0)

        if (width > 0) {
            ResInjectTool.setResReplacement("com.miui.securitycenter", "dimen",
                "sidebar_line_width_vertical", width)
            ResInjectTool.setResReplacement("com.miui.securitycenter", "dimen",
                "sidebar_line_width_drag", width)
        }
        if (length > 0) {
            ResInjectTool.setResReplacement("com.miui.securitycenter", "dimen",
                "sidebar_line_height_vertical", length)
            ResInjectTool.setResReplacement("com.miui.securitycenter", "dimen",
                "sidebar_line_height_drag", length)
        }
        if (margin > 0) {
            ResInjectTool.setResReplacement("com.miui.securitycenter", "dimen",
                "sidebar_line_margin_start", margin)
        }

        // Expanded panel size and vertical position
        val panelW = mPrefsMap.getInt("security_center_sidebar_assistant_width", 0)
        val panelH = mPrefsMap.getInt("security_center_sidebar_assistant_height", 0)
        val posY = mPrefsMap.getInt("security_center_sidebar_default_y", 0)
        if (panelW > 0) ResInjectTool.setResReplacement("com.miui.securitycenter", "dimen",
            "sidebar_assistant_width", panelW)
        if (panelH > 0) ResInjectTool.setResReplacement("com.miui.securitycenter", "dimen",
            "sidebar_assistant_height", panelH)
        if (posY > 0) ResInjectTool.setResReplacement("com.miui.securitycenter", "dimen",
            "gd_sidebar_default_y", posY)

        // Background colors (-1 = untouched)
        val menuBg = mPrefsMap.getInt("security_center_sidebar_menu_bg_color", -1)
        val wrapperBg = mPrefsMap.getInt("security_center_sidebar_wrapper_color", -1)
        if (menuBg != -1) ResInjectTool.setResReplacement("com.miui.securitycenter", "color",
            "sidebar_shortcut_menu_bg_color", menuBg)
        if (wrapperBg != -1) ResInjectTool.setResReplacement("com.miui.securitycenter", "color",
            "sidebar_wrapper_dark_color", wrapperBg)
    
}
}
