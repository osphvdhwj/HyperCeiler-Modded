package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.sevtinge.hyperceiler.hook.R

object Calculator {

    private val numRe = Regex("^-?\\d+(\\.\\d+)?$")

    fun build(ctx: Context, query: String): List<SearchResult> {
        val q = query.trim().replace(" ", "")
        if (q.isEmpty()) return emptyList()
        val expr = q.removeSuffix("=")
        if (!looksLikeMath(expr)) return emptyList()
        val value = eval(expr) ?: return emptyList()
        val display = format(value)
        val copyIntent = Intent().apply {
            putExtra("__calc_copy", display)
        }
        return listOf(
            SearchResult(
                type = ResultType.SETTING,
                label = display,
                subtitle = "Calculator · tap to copy",
                icon = runCatching {
                    androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_section_calc)
                }.getOrNull(),
                intent = copyIntent,
            )
        )
    }

    fun handleCopy(ctx: Context, value: String): Boolean {
        return runCatching {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("calc", value))
            true
        }.getOrDefault(false)
    }

    private fun looksLikeMath(s: String): Boolean {
        if (s.isEmpty()) return false
        if (numRe.matches(s)) return false
        return s.any { it in "+-*/%^()" } && s.any { it.isDigit() }
    }

    private fun format(v: Double): String {
        return if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
    }

    private fun eval(input: String): Double? = try {
        val v = Parser(input).parse()
        if (v.isNaN() || v.isInfinite()) null else v
    } catch (_: Throwable) { null }

    private class Parser(private val s: String) {
        private var i = 0
        fun parse(): Double {
            val v = expr(); skipWs(); if (i < s.length) throw IllegalArgumentException()
            return v
        }
        private fun expr(): Double {
            var v = term()
            while (true) {
                skipWs()
                if (i >= s.length) return v
                when (s[i]) {
                    '+' -> { i++; v += term() }
                    '-' -> { i++; v -= term() }
                    else -> return v
                }
            }
        }
        private fun term(): Double {
            var v = factor()
            while (true) {
                skipWs()
                if (i >= s.length) return v
                when (s[i]) {
                    '*' -> { i++; v *= factor() }
                    '/' -> { i++; v /= factor() }
                    '%' -> { i++; v %= factor() }
                    else -> return v
                }
            }
        }
        private fun factor(): Double {
            skipWs()
            if (i < s.length && s[i] == '+') { i++; return factor() }
            if (i < s.length && s[i] == '-') { i++; return -factor() }
            var v = primary()
            skipWs()
            if (i < s.length && s[i] == '^') { i++; v = Math.pow(v, factor()) }
            return v
        }
        private fun primary(): Double {
            skipWs()
            if (i < s.length && s[i] == '(') {
                i++; val v = expr(); skipWs()
                if (i >= s.length || s[i] != ')') throw IllegalArgumentException()
                i++; return v
            }
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            if (start == i) throw IllegalArgumentException()
            return s.substring(start, i).toDouble()
        }
        private fun skipWs() { while (i < s.length && s[i].isWhitespace()) i++ }
    }
}
