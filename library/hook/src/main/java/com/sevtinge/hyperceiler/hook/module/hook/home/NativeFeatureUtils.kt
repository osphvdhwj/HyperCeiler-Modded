package com.sevtinge.hyperceiler.hook.module.hook.home

import android.content.Context
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD
import de.robv.android.xposed.XposedHelpers

object NativeFeatureUtils {

    /**
     * Checks if the port ROM natively supports iOS Stacked Recents.
     * (We look for the native method in DeviceConfig or LauncherState).
     */
    fun hasNativeIosRecents(context: Context): Boolean {
        return try {
            val deviceConfig = XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", context.classLoader)
            val hasMethod = XposedHelpers.findMethodExactIfExists(deviceConfig, "isRecentsIosMode") != null ||
                            XposedHelpers.findMethodExactIfExists(deviceConfig, "getRecentIOSMode") != null
            if (hasMethod) logD("NativeFeatureUtils", "Native iOS Recents detected")
            hasMethod
        } catch (t: Throwable) {
            false
        }
    }

    /**
     * Checks if the port ROM natively has Force Stop in the Recents menu.
     * We check if the native SystemShortcutMenuItem list already contains it.
     */
    fun hasNativeForceStop(context: Context): Boolean {
        return try {
            val sysShortcut = XposedHelpers.findClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem", context.classLoader)
            val list = XposedHelpers.getStaticObjectField(sysShortcut, "sAllSystemShortcutMenuItems") as? List<*>
            // Check if any item in the list is a ForceStop instance
            val hasForceStop = list?.any { it?.javaClass?.name?.contains("ForceStop") == true } == true
            if (hasForceStop) logD("NativeFeatureUtils", "Native Force Stop detected")
            hasForceStop
        } catch (t: Throwable) {
            false
        }
    }
}
