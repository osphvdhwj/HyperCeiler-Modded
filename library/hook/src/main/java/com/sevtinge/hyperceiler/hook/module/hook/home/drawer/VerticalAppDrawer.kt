package com.sevtinge.hyperceiler.hook.module.hook.home.drawer

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object VerticalAppDrawer : BaseHook() {
    override fun init() {
        // 1. Hook DeviceConfig (handles layout generation)
        val deviceConfig = findClassIfExists("com.miui.home.launcher.DeviceConfig")
        if (deviceConfig != null) {
            try {
                deviceConfig.hookAllMethods("getAllAppsColumnCount") { param ->
                    param.result = 1
                }
                deviceConfig.hookAllMethods("getDrawerColumnCount") { param ->
                    param.result = 1
                }
                logD("VerticalAppDrawer", "Hooked DeviceConfig column count")
            } catch (t: Throwable) {}
        }

        // 2. Hook LauncherGridConfig (sometimes overrides DeviceConfig)
        val gridConfig = findClassIfExists("com.miui.home.launcher.GridConfig")
        if (gridConfig != null) {
            try {
                gridConfig.hookAllMethods("getCols") { param ->
                    val mode = param.args.firstOrNull()
                    // Usually 0 or 1 is the drawer mode
                    if (mode == 0 || mode == 1) param.result = 1
                }
            } catch (t: Throwable) {}
        }

        // 3. Hook AllAppsGridAdapter (last line of defense)
        val gridAdapter = findClassIfExists("com.miui.home.launcher.allapps.AllAppsGridAdapter")
        if (gridAdapter != null) {
            try {
                gridAdapter.hookAllMethods("getSpanCount") { param ->
                    param.result = 1
                }
            } catch (t: Throwable) {}
        }
    }
}
