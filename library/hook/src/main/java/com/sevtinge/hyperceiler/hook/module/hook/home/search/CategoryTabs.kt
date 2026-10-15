package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

class CategoryTabs(
    private val ctx: Context,
    private val tokens: ColorScheme.Tokens,
    private val onSelect: (QueryPrefix.Filter) -> Unit,
) {

    data class Tab(val filter: QueryPrefix.Filter, val label: String)

    private val tabs = listOf(
        Tab(QueryPrefix.Filter.NONE, "All"),
        Tab(QueryPrefix.Filter.APPS, "Apps"),
        Tab(QueryPrefix.Filter.SETTINGS, "Settings"),
        Tab(QueryPrefix.Filter.SHORTCUTS, "Shortcuts"),
        Tab(QueryPrefix.Filter.MEDIA, "Media"),
        Tab(QueryPrefix.Filter.FILES, "Files"),
        Tab(QueryPrefix.Filter.WEB, "Web"),
    )

    private var current: QueryPrefix.Filter = QueryPrefix.Filter.NONE
    private val views = HashMap<QueryPrefix.Filter, TextView>()

    val root: View get() = build()

    private fun build(): View {
        val scroll = HorizontalScrollView(ctx).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16 }
        }
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        for (t in tabs) {
            val tv = TextView(ctx).apply {
                text = t.label
                textSize = 13f
                setPadding(36, 20, 36, 20)
                isClickable = true
                isFocusable = true
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 12 }
                setOnClickListener { select(t.filter) }
            }
            views[t.filter] = tv
            row.addView(tv)
        }
        scroll.addView(row)
        applyStyles()
        return scroll
    }

    private fun select(f: QueryPrefix.Filter) {
        current = f
        applyStyles()
        onSelect(f)
    }

    private fun applyStyles() {
        for ((f, tv) in views) {
            if (f == current) {
                tv.setTextColor(tokens.bg)
                tv.background = pill(tokens.accent)
            } else {
                tv.setTextColor(tokens.text)
                tv.background = pill(tokens.surface)
            }
        }
    }

    private fun pill(fillColor: Int) = GradientDrawable().apply {
        cornerRadius = 48f
        setColor(fillColor)
    }

    companion object {
        // Matches prefix keys so the user can also type them
        fun filterForKey(key: Char): QueryPrefix.Filter? = when (key) {
            '>' -> QueryPrefix.Filter.APPS
            '=' -> QueryPrefix.Filter.SETTINGS
            '#' -> QueryPrefix.Filter.SHORTCUTS
            '.' -> QueryPrefix.Filter.FILES
            '?' -> QueryPrefix.Filter.WEB
            '@' -> QueryPrefix.Filter.MEDIA
            else -> null
        }
    }
}
