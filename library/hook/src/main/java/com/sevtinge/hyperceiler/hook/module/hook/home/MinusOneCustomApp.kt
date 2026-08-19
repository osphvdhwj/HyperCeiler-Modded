package com.sevtinge.hyperceiler.hook.module.hook.home

import android.app.Activity
import android.content.Context
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods

object MinusOneCustomApp : BaseHook() {
    override fun init() {
        val launcherClass = findClassIfExists("com.miui.home.launcher.Launcher") ?: return
        
        launcherClass.hookBeforeAllMethods("showOverlay") { param ->
            val customAppSet = mPrefsMap.getStringSet("home_minus_one_custom_app", emptySet())
            val customAppPkg = customAppSet.firstOrNull()
            
            if (!customAppPkg.isNullOrEmpty()) {
                // Cancel default overlay (Google/AppVault)
                param.result = null
                
                // Launch the selected app
                val context = param.thisObject as? Activity ?: return@hookBeforeAllMethods
                val intent = context.packageManager.getLaunchIntentForPackage(customAppPkg)
                if (intent != null) {
                    context.startActivity(intent)
                }
            }
        }
    }
}
