package com.sevtinge.hyperceiler.hook.module.hook.home.other

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createBeforeHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull

/**
 * Always show drag-to-remove and drag-to-uninstall drop targets on home screen.
 *
 * Verified via dex analysis of /product/priv-app/MiuiHome/MiuiHome.apk:
 *   - com.miui.home.launcher.DeleteDropTarget: ❌ DOES NOT EXIST in this build
 *   - com.miui.home.launcher.UninstallDropTarget: ✅ EXISTS (with UninstallDropTargetMode)
 *   - com.miui.home.launcher.ButtonDropTarget: ✅ EXISTS (base class for all drop targets)
 *   - com.miui.home.launcher.DropTargetBar: ✅ EXISTS
 *
 * DeleteDropTarget was removed in this HyperOS version and merged into UninstallDropTarget.
 * The real class that gates supportsDrop is ButtonDropTarget (parent) and UninstallDropTarget.
 */
object AlwaysShowDropTargets : BaseHook() {
    override fun init() {
        // Hook ButtonDropTarget.supportsDrop (base class for all drop targets, verified in dex)
        loadClassOrNull("com.miui.home.launcher.ButtonDropTarget")?.let { clazz ->
            clazz.methodFinder()
                .filterByName("supportsDrop")
                .firstOrNull()?.createBeforeHook { param ->
                    param.result = true
                }
        }

        // Hook UninstallDropTarget.supportsDrop (verified in dex)
        loadClassOrNull("com.miui.home.launcher.UninstallDropTarget")?.let { clazz ->
            clazz.methodFinder()
                .filterByName("supportsDrop")
                .firstOrNull()?.createBeforeHook { param ->
                    param.result = true
                }
        }

        // NOTE: com.miui.home.launcher.DeleteDropTarget does NOT exist in this build.
        // It was merged into UninstallDropTarget in HyperOS.
    }
}
