package com.sevtinge.hyperceiler.hook.module.hook.systemframework

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.api.ProjectApi
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

class ForceStopInterceptor : BaseHook() {
    override fun init() {
        try {
            val amsClass = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader)
            
            XposedBridge.hookAllMethods(amsClass, "forceStopPackage", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val pkgName = param.args[0] as? String ?: return
                    val context = XposedHelpers.getObjectField(param.thisObject, "mContext") as? Context ?: return
                    
                    // Don't prompt for system apps
                    try {
                        val pm = context.packageManager
                        val appInfo = pm.getApplicationInfo(pkgName, 0)
                        if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
                            return
                        }
                    } catch (e: Exception) {
                        return
                    }

                    // Check if it's already in the list
                    try {
                        val prefsContext = context.createPackageContext(ProjectApi.mAppModulePkg, Context.CONTEXT_IGNORE_SECURITY)
                        val prefs = prefsContext.getSharedPreferences("hyperceiler_prefs", Context.MODE_PRIVATE)
                        val hailApps = prefs.getStringSet("hail_apps", emptySet())
                        if (hailApps != null && hailApps.contains(pkgName)) {
                            return // Already in list
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }

                    // Show a toast or notification since starting an activity from AMS might be disruptive,
                    // but wait, the plan says "intercept" force stop?
                    // "Integrate with system launcher"
                    // Let's send a broadcast to our app, so our app can show a notification asking to add it!
                    val intent = Intent("com.sevtinge.hyperceiler.intent.action.PROMPT_ADD_HAIL")
                    intent.setClassName(ProjectApi.mAppModulePkg, "com.sevtinge.hyperceiler.ui.hail.PackageInstallReceiver")
                    intent.putExtra("extra_pkg", pkgName)
                    context.sendBroadcast(intent)
                }
            })
        } catch (t: Throwable) {
            logE(TAG, "android", "ForceStopInterceptor Failed to hook - " + android.util.Log.getStackTraceString(t))
        }
    }
}
