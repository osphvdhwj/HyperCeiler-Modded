package com.sevtinge.hyperceiler.ui.hail

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class AppListManager(context: Context) {
    
    @SuppressLint("WorldReadableFiles")
    @Suppress("DEPRECATION")
    private val prefs: SharedPreferences = context.getSharedPreferences("hyperceiler_prefs", Context.MODE_WORLD_READABLE)

    fun addApp(pkg: String) {
        val apps = getApps().toMutableSet()
        apps.add(pkg)
        saveApps(apps)
    }

    fun removeApp(pkg: String) {
        val apps = getApps().toMutableSet()
        apps.remove(pkg)
        saveApps(apps)
    }

    private fun saveApps(apps: Set<String>) {
        prefs.edit().putStringSet("hail_apps", apps).apply()
    }

    fun getApps(): Set<String> {
        return prefs.getStringSet("hail_apps", emptySet()) ?: emptySet()
    }

    fun isAppInList(pkg: String): Boolean {
        return getApps().contains(pkg)
    }
}
