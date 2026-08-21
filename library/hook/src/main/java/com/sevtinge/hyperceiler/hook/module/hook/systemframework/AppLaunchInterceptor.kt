package com.sevtinge.hyperceiler.hook.module.hook.systemframework

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Modifier

class AppLaunchInterceptor : BaseHook() {
    override fun init() {
        try {
            val cls = XposedHelpers.findClass(
                "com.android.server.wm.ActivityTaskManagerService", lpparam.classLoader
            )

            for (method in cls.declaredMethods) {
                if (method.name != "startActivity") continue
                if (!Modifier.isPublic(method.modifiers)) continue

                val paramTypes = method.parameterTypes
                var intentIndex = -1
                for (i in paramTypes.indices) {
                    if (Intent::class.java == paramTypes[i]) {
                        intentIndex = i
                        break
                    }
                }

                if (intentIndex == -1) continue

                val index = intentIndex
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val intent = param.args[index] as? Intent ?: return
                            
                            val targetPkg = intent.component?.packageName ?: intent.getPackage()
                            if (targetPkg == null || targetPkg == "com.harry.hyperhand") return

                            // Read hail apps
                            val appsStr = mPrefsMap.getString("hail_apps", "")
                            if (appsStr == null || appsStr.isEmpty()) return
                            
                            val hailApps = appsStr.split(",").toSet()
                            if (!hailApps.contains(targetPkg)) return
                            
                            // Check for temporary grant using Context
                            val context = XposedHelpers.getObjectField(param.thisObject, "mContext") as? Context
                            if (context != null) {
                                val prefsContext = context.createPackageContext("com.harry.hyperhand", Context.CONTEXT_IGNORE_SECURITY)
                                val grantPrefs = prefsContext.getSharedPreferences("hail_apps_prefs", Context.MODE_PRIVATE)
                                val expireTime = grantPrefs.getLong("grant_$targetPkg", 0L)
                                if (System.currentTimeMillis() < expireTime) {
                                    // Grant is active, let it launch
                                    return
                                }
                            }
                            
                            // It is in the list and no active grant. Intercept!
                            val interceptorIntent = Intent()
                            interceptorIntent.setClassName("com.harry.hyperhand", "com.sevtinge.hyperceiler.ui.hail.InterceptorActivity")
                            interceptorIntent.putExtra("target_pkg", targetPkg)
                            interceptorIntent.putExtra("original_intent", intent)
                            interceptorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            
                            param.args[index] = interceptorIntent

                        } catch (t: Throwable) {
                            logE(TAG, "android", "AppLaunchInterceptor Error - " + Log.getStackTraceString(t))
                        }
                    }
                })
                break
            }
        } catch (t: Throwable) {
            logE(TAG, "android", "AppLaunchInterceptor Failed to hook - " + Log.getStackTraceString(t))
        }
    }
}
