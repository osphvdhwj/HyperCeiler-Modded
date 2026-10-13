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

    fun build(ctx: Context): List<SearchResult> {
        val out = ArrayList<SearchResult>(MAX)
        val la = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return out
        val um = ctx.getSystemService(Context.USER_SERVICE) as? UserManager ?: return out
        val icon = runCatching {
            androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_section_shortcut)
        }.getOrNull()

        for (user in um.userProfiles) {
            val handle: UserHandle = user.userHandle ?: continue
            val packages = runCatching {
                la.getActivityList(null, handle).map { it.applicationInfo.packageName }.distinct()
            }.getOrDefault(emptyList())

            for (pkg in packages) {
                if (out.size >= MAX) break
                val shortcuts = runCatching {
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
                }.getOrDefault(emptyList())

                for (s: ShortcutInfo in shortcuts) {
                    val label = s.shortLabel?.toString()
                        ?: s.longLabel?.toString()
                        ?: continue
                    val launch = runCatching {
                        la.getShortcutIntent(pkg, s.id, null, handle)
                    }.getOrNull() ?: continue
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    out.add(
                        SearchResult(
                            type = ResultType.SHORTCUT,
                            label = label,
                            subtitle = s.`package`,
                            icon = icon,
                            intent = launch,
                        )
                    )
                }
            }
        }
        logD(TAG, "shortcuts: ${out.size}")
        return out
    }
}
