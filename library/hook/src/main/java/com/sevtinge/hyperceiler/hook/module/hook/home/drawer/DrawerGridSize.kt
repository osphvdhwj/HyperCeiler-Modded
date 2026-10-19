package com.sevtinge.hyperceiler.hook.module.hook.home.drawer

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object DrawerGridSize : BaseHook() {
    override fun init() {
        val cols = if (mPrefsMap.getBoolean("prefs_key_home_drawer_vertical")) 1 else mPrefsMap.getInt("prefs_key_home_drawer_cols", 4)
        val rows = mPrefsMap.getInt("prefs_key_home_drawer_rows", 6)

        logD("DrawerGridSize", "Forcing Drawer Grid: ${cols}x${rows}")

        // Hook DeviceConfig (Primary source for grid calculations)
        val deviceConfig = findClassIfExists("com.miui.home.launcher.DeviceConfig")
        if (deviceConfig != null) {
            try {
                deviceConfig.hookBeforeAllMethods("getAllAppsColumnCount") { param ->
                    param.result = cols
                }
                deviceConfig.hookBeforeAllMethods("getDrawerColumnCount") { param ->
                    param.result = cols
                }
                deviceConfig.hookBeforeAllMethods("getAllAppsRowCount") { param ->
                    param.result = rows
                }
            } catch (t: Throwable) {
                logD("DrawerGridSize", "DeviceConfig hook failed: ${t.message}")
            }
        }

        // Hook GridConfig (Secondary source, often used by the adapter)
        val gridConfig = findClassIfExists("com.miui.home.launcher.GridConfig")
        if (gridConfig != null) {
            try {
                gridConfig.hookBeforeAllMethods("getCols") { param ->
                    param.result = cols
                }
                gridConfig.hookBeforeAllMethods("getRows") { param ->
                    param.result = rows
                }
            } catch (t: Throwable) {
                logD("DrawerGridSize", "GridConfig hook failed: ${t.message}")
            }
        }
    }
}
