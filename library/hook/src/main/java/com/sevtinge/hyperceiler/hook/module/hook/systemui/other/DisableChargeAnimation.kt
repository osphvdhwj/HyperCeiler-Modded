package com.sevtinge.hyperceiler.hook.module.hook.systemui.other

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.getObjectFieldAs
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods

object DisableChargeAnimation : BaseHook() {
    override fun init() {
        val chargeControllerClass = findClassIfExists("com.miui.charge.MiuiChargeController") ?: return

        // Hook the main methods that trigger the charging animation
        val hookCallback: (de.robv.android.xposed.XC_MethodHook.MethodHookParam) -> Unit = { param ->
            val context = param.thisObject.getObjectFieldAs<Context>("mContext") ?: return@hookCallback
            
            val disableInGame = mPrefsMap.getBoolean("system_ui_disable_charge_anim_in_game")
            val disabledApps = mPrefsMap.getStringSet("system_ui_disable_charge_anim_apps", emptySet())
            
            if (disableInGame || disabledApps.isNotEmpty()) {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasks = am.getRunningTasks(1)
                if (tasks.isNotEmpty()) {
                    val topActivity = tasks[0].topActivity
                    if (topActivity != null) {
                        val topPackageName = topActivity.packageName
                        
                        // Check specific apps list
                        if (disabledApps.contains(topPackageName)) {
                            param.result = null // Cancel animation
                            return@hookCallback
                        }

                        // Check if it's a game
                        if (disableInGame) {
                            try {
                                val pm = context.packageManager
                                val appInfo = pm.getApplicationInfo(topPackageName, 0)
                                val isGame = (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0 || 
                                             appInfo.category == ApplicationInfo.CATEGORY_GAME
                                if (isGame) {
                                    param.result = null // Cancel animation
                                }
                            } catch (e: PackageManager.NameNotFoundException) {
                                // Ignore
                            }
                        }
                    }
                }
            }
        }
        
        chargeControllerClass.hookBeforeAllMethods("dealWithAnimationShow", callback = hookCallback)
        chargeControllerClass.hookBeforeAllMethods("showChargeAnimation", callback = hookCallback)
    }
}
