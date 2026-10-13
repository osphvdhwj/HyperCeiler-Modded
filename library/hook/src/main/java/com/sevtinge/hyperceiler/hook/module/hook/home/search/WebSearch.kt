package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import com.sevtinge.hyperceiler.hook.R

object WebSearch {

    private const val ACTION_WEB_SEARCH = "android.intent.action.WEB_SEARCH"
    private const val EXTRA_QUERY = "query"

    fun build(ctx: Context, query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()
        val q = query.trim()
        val out = ArrayList<SearchResult>(2)

        val webIntent = Intent(ACTION_WEB_SEARCH)
            .putExtra(EXTRA_QUERY, q)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (ctx.packageManager.resolveActivity(webIntent, 0) != null) {
            out.add(
                SearchResult(
                    type = ResultType.WEB,
                    label = "Search \"$q\"",
                    subtitle = "Web",
                    icon = icon(ctx),
                    intent = webIntent,
                )
            )
        } else {
            val uriIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(q)))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (ctx.packageManager.resolveActivity(uriIntent, 0) != null) {
                out.add(
                    SearchResult(
                        type = ResultType.WEB,
                        label = "Search \"$q\"",
                        subtitle = "Web",
                        icon = icon(ctx),
                        intent = uriIntent,
                    )
                )
            }
        }
        return out
    }

    private fun icon(ctx: Context): Drawable? = runCatching {
        androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_section_web)
    }.getOrNull()
}
