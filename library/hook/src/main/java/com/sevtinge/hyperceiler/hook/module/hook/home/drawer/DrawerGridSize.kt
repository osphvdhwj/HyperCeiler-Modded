package com.sevtinge.hyperceiler.hook.module.hook.home.drawer

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logI

object DrawerGridSize : BaseHook() {
    override fun init() {
        val vertical = mPrefsMap.getBoolean("prefs_key_home_drawer_vertical")
        val cols = if (vertical) 1 else mPrefsMap.getInt("prefs_key_home_drawer_cols", 4)
        if (cols <= 0 || (cols == 4 && !vertical)) return

        logI("DrawerGridSize", "Forcing drawer column count: $cols")

        // MIUI Home 6.x computes the drawer column count in its own layout
        // logic (DrawerAppsListFragment & category fragments) and applies it
        // through AllAppsGridAdapter.refreshSpanCount — the old
        // DeviceConfig.getAllAppsColumnCount / GridConfig hooks do not exist
        // on this generation.
        findClassIfExists("com.miui.home.launcher.allapps.AllAppsGridAdapter")
            ?.hookBeforeAllMethods("refreshSpanCount") { param ->
                param.args[0] = cols
            }
        findClassIfExists("com.miui.home.launcher.allapps.category.AppCategorySelectAdapter")
            ?.hookBeforeAllMethods("refreshSpanCount") { param ->
                param.args[0] = cols
            }
    }
}
