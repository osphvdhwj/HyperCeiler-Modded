package com.sevtinge.hyperceiler.hook.module.hook.home

import android.content.Context
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD
import de.robv.android.xposed.XposedHelpers

object NativeFeatureUtils {

    // Cache the result to prevent expensive reflection scans on every UI load
    @Volatile private var cachedIosRecents: Boolean? = null
    @Volatile private var cachedForceStop: Boolean? = null

    fun hasNativeIosRecents(context: Context): Boolean {
        cachedIosRecents?.let { return it }
        synchronized(this) {
            cachedIosRecents?.let { return it }
            val result = try {
                val deviceConfig = XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", context.classLoader)
                val hasMethod = XposedHelpers.findMethodExactIfExists(deviceConfig, "isRecentsIosMode") != null ||
                                XposedHelpers.findMethodExactIfExists(deviceConfig, "getRecentIOSMode") != null
                if (hasMethod) logD("NativeFeatureUtils", "Native iOS Recents detected (cached)")
                hasMethod
            } catch (t: Throwable) {
                false
            }
            cachedIosRecents = result
            return result
        }
    }

    fun hasNativeForceStop(context: Context): Boolean {
        cachedForceStop?.let { return it }
        synchronized(this) {
            cachedForceStop?.let { return it }
            val result = try {
                val sysShortcut = XposedHelpers.findClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem", context.classLoader)
                val list = XposedHelpers.getStaticObjectField(sysShortcut, "sAllSystemShortcutMenuItems") as? List<*>
                val hasForceStop = list?.any { it?.javaClass?.name?.contains("ForceStop") == true } == true
                if (hasForceStop) logD("NativeFeatureUtils", "Native Force Stop detected (cached)")
                hasForceStop
            } catch (t: Throwable) {
                false
            }
            cachedForceStop = result
            return result
        }
    }
}
