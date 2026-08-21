package com.sevtinge.hyperceiler.hook.module.hook.home.other

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createBeforeHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull

object AlwaysShowDropTargets : BaseHook() {
    override fun init() {
        // Hook the Remove/Delete target
        loadClassOrNull("com.miui.home.launcher.DeleteDropTarget")?.let { clazz ->
            clazz.methodFinder()
                .filterByName("supportsDrop")
                .firstOrNull()?.createBeforeHook { param ->
                    param.result = true
                }
        }

        // Hook the Uninstall target
        loadClassOrNull("com.miui.home.launcher.UninstallDropTarget")?.let { clazz ->
            clazz.methodFinder()
                .filterByName("supportsDrop")
                .firstOrNull()?.createBeforeHook { param ->
                    param.result = true
                }
        }
    }
}

