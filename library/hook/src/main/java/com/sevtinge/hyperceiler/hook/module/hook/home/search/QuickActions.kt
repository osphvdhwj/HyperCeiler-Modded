package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.UserHandle

object QuickActions {

    private const val MAX = 4
    private val cache = HashMap<String, List<ShortcutInfo>>()

    fun forPackage(ctx: Context, pkg: String, user: UserHandle?): List<ShortcutInfo> {
        val key = "$pkg@${user?.hashCode() ?: 0}"
        cache[key]?.let { return it }
        val la = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            ?: return emptyList()
        val q = LauncherApps.ShortcutQuery()
            .setPackage(pkg)
            .setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                    or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                    or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
            )
        val list = runCatching { la.getShortcuts(q, user ?: android.os.Process.myUserHandle()) }
            .getOrDefault(emptyList())
            .filter { it.shortLabel != null || it.longLabel != null }
            .take(MAX)
        cache[key] = list
        return list
    }

    fun invalidate() { cache.clear() }
}
