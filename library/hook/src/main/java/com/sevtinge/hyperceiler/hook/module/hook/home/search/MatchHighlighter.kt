package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan

object MatchHighlighter {

    fun apply(label: String, query: String, accent: Int): CharSequence {
        if (query.isBlank()) return label
        val l = label.lowercase()
        val q = query.lowercase()
        val span = SpannableString(label)

        val start = l.indexOf(q)
        if (start >= 0) {
            span.setSpan(
                ForegroundColorSpan(accent), start, start + q.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            span.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD), start, start + q.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return span
        }

        var qi = 0
        var lastEnd = -1
        var firstStart = -1
        for (i in l.indices) {
            if (qi < q.length && l[i] == q[qi]) {
                span.setSpan(
                    ForegroundColorSpan(accent), i, i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                if (firstStart < 0) firstStart = i
                lastEnd = i + 1
                qi++
            }
        }
        if (firstStart >= 0 && lastEnd > firstStart) {
            span.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD), firstStart, lastEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return span
    }
}
