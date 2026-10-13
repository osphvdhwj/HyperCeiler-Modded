package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object SearchIndex {

    private const val TAG = "SearchIndex"

    @Volatile private var apps: List<SearchResult> = emptyList()
    @Volatile private var built = false

    fun ensureBuilt(ctx: Context) {
        if (built) return
        synchronized(this) {
            if (built) return
            apps = buildApps(ctx)
            built = true
            logD(TAG, "index built: ${apps.size} apps")
        }
    }

    fun invalidate() {
        synchronized(this) { built = false; apps = emptyList() }
    }

    fun apps(): List<SearchResult> = apps

    private fun buildApps(ctx: Context): List<SearchResult> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        val self = ctx.packageName
        val out = ArrayList<SearchResult>(resolved.size)
        for (ri in resolved) {
            val info = ri.activityInfo ?: continue
            val pkg = info.packageName ?: continue
            if (pkg == self) continue
            val label = ri.loadLabel(pm).toString()
            val icon = ri.loadIcon(pm)
            val launch = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setClassName(pkg, info.name)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            out.add(
                SearchResult(
                    type = ResultType.APP,
                    label = label,
                    subtitle = pkg,
                    icon = icon,
                    intent = launch,
                )
            )
        }
        out.sortBy { it.label.lowercase() }
        return out
    }
}
