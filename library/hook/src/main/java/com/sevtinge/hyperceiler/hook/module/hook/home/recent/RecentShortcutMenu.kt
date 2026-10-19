package com.sevtinge.hyperceiler.hook.module.hook.home.recent

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
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
                        // Only force valid for safe, non-destructive actions
                        val name = clazz.simpleName
                        if (name.contains("Share") || name.contains("AppDetails") || name.contains("Pin")) {
                            logD("RecentShortcutMenu", "Forcing valid: $name")
                            param.result = true
                        }
                    }
                } catch (t: Throwable) {
                    // Ignore if the subclass doesn't have isValid
                }
            }
        }
    }
}
