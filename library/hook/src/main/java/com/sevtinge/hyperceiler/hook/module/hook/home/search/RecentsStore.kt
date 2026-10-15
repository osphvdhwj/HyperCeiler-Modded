package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.Intent

object RecentsStore {

    private const val PREF = "global_search_recents"
    private const val SEP = "\u0001"
    private const val MAX = 10

    const val EXTRA_RECENT_QUERY = "__recent_query"

    fun load(ctx: Context): List<String> {
        val raw = prefs(ctx).getString(PREF, "") ?: return emptyList()
        if (raw.isEmpty()) return emptyList()
        return raw.split(SEP).filter { it.isNotBlank() }.take(MAX)
    }

    fun asResults(ctx: Context): List<SearchResult> {
        val icon = runCatching {
            androidx.core.content.ContextCompat.getDrawable(
                ctx, com.sevtinge.hyperceiler.hook.R.drawable.ic_search_ui_search
            )
        }.getOrNull()
        return load(ctx).map { q ->
            SearchResult(
                type = ResultType.SETTING,
                label = q,
                subtitle = "Recent",
                icon = icon,
                intent = Intent().putExtra(EXTRA_RECENT_QUERY, q),
            )
        }
    }

    fun push(ctx: Context, query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        val existing = load(ctx).filter { it != q }.toMutableList()
        existing.add(0, q)
        while (existing.size > MAX) existing.removeAt(existing.size - 1)
        prefs(ctx).edit().putString(PREF, existing.joinToString(SEP)).apply()
    }

    fun clear(ctx: Context) {
        prefs(ctx).edit().remove(PREF).apply()
    }

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("hyperceiler_global_search", Context.MODE_PRIVATE)
}
