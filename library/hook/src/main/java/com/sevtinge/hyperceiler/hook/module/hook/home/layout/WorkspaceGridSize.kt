package com.sevtinge.hyperceiler.hook.module.hook.home.layout

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object WorkspaceGridSize : BaseHook() {
    override fun init() {
        val rows = mPrefsMap.getInt("prefs_key_home_layout_workspace_rows", 0)
        val cols = mPrefsMap.getInt("prefs_key_home_layout_workspace_cols", 0)
        val dockCount = mPrefsMap.getInt("prefs_key_home_layout_dock_count", 0)

        if (rows == 0 && cols == 0 && dockCount == 0) return

        val deviceConfig = findClassIfExists("com.miui.home.launcher.DeviceConfig")
        if (deviceConfig != null) {
            if (rows > 0) {
                deviceConfig.hookBeforeAllMethods("getWorkspaceRows") { param -> param.result = rows }
                logD("WorkspaceGridSize", "Forced Workspace Rows: $rows")
            }
            if (cols > 0) {
                deviceConfig.hookBeforeAllMethods("getWorkspaceColumns") { param -> param.result = cols }
                logD("WorkspaceGridSize", "Forced Workspace Columns: $cols")
            }
            if (dockCount > 0) {
                deviceConfig.hookBeforeAllMethods("getHotseatMaxCount") { param -> param.result = dockCount }
                logD("WorkspaceGridSize", "Forced Dock Icon Count: $dockCount")
            }
        }
    }
}
