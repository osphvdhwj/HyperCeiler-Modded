package com.sevtinge.hyperceiler.hook.module.hook.home.recent

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object RecentShortcutMenu : BaseHook() {
    override fun init() {
        // Target the isValid method in all SystemShortcutMenuItem subclasses
        val baseClass = findClassIfExists("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem") ?: return
        
        // We loop through all declared subclasses of SystemShortcutMenuItem
        val subclasses = baseClass.declaredClasses
        for (clazz in subclasses) {
            if (clazz.simpleName.endsWith("ShortcutMenuItem")) {
                try {
                    clazz.hookBeforeAllMethods("isValid") { param ->
                        // Force the button to be visible
                        logD("RecentShortcutMenu", "Forcing valid: ${clazz.simpleName}")
                        param.result = true
                    }
                } catch (t: Throwable) {
                    // Ignore if the subclass doesn't have isValid
                }
            }
        }
    }
}
