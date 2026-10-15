package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.UserHandle
import android.os.UserManager
import com.sevtinge.hyperceiler.hook.R
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object ShortcutsIndex {

    private const val TAG = "ShortcutsIndex"
    private const val MAX = 200

    const val EXTRA_SHORTCUT_PKG = "__shortcut_pkg"
    const val EXTRA_SHORTCUT_ID = "__shortcut_id"
    const val EXTRA_SHORTCUT_USER = "__shortcut_user"

    fun build(ctx: Context): List<SearchResult> {
        val out = ArrayList<SearchResult>(MAX)
        val la = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return out
        val um = ctx.getSystemService(Context.USER_SERVICE) as? UserManager ?: return out
        val icon = runCatching {
            androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_section_shortcut)
        }.getOrNull()

        val users: List<UserHandle> = um.userProfiles
        for (handle in users) {
            if (out.size >= MAX) break
            val packages = runCatching {
                la.getActivityList(null, handle).map { it.applicationInfo.packageName }.distinct()
            }.getOrDefault(emptyList())

            for (pkg in packages) {
                if (out.size >= MAX) break
                val shortcuts: List<ShortcutInfo> = runCatching {
                    la.getShortcuts(
                        LauncherApps.ShortcutQuery()
                            .setPackage(pkg)
                            .setQueryFlags(
                                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                                    or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                                    or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                            ),
                        handle
                    )
                }.getOrNull() ?: emptyList()

                for (s: ShortcutInfo in shortcuts) {
                    val label = s.shortLabel?.toString()
                        ?: s.longLabel?.toString()
                        ?: continue
                    val stub = Intent().apply {
                        putExtra(EXTRA_SHORTCUT_PKG, pkg)
                        putExtra(EXTRA_SHORTCUT_ID, s.id)
                        putExtra(EXTRA_SHORTCUT_USER, handle)
                    }
                    out.add(
                        SearchResult(
                            type = ResultType.SHORTCUT,
                            label = label,
                            subtitle = pkg,
                            icon = icon,
                            intent = stub,
                        )
                    )
                }
            }
        }
        logD(TAG, "shortcuts: ${out.size}")
        return out
    }
}
