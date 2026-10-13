package com.sevtinge.hyperceiler.hook.module.hook.home.search

object QueryEngine {

    private const val MAX_RESULTS = 50

    fun query(rawQuery: String, all: List<SearchResult>): List<SearchResult> {
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) return all.take(MAX_RESULTS)
        val scored = ArrayList<SearchResult>(all.size)
        for (item in all) {
            val s = score(q, item.label.lowercase(), item.subtitle?.lowercase() ?: "")
            if (s > 0) scored.add(item.copy(score = s))
        }
        scored.sortWith(compareByDescending<SearchResult> { it.score }.thenBy { it.label.lowercase() })
        return scored.take(MAX_RESULTS)
    }

    private fun score(q: String, label: String, sub: String): Int {
        if (label.startsWith(q)) return 1000 - (label.length - q.length).coerceAtLeast(0)
        var i = 0
        while (i < label.length) {
            if (label[i] == ' ' && label.substring(i + 1).startsWith(q)) return 800 - q.length
            i++
        }
        val pos = label.indexOf(q)
        if (pos >= 0) return 600 - pos
        var qi = 0
        var matched = 0
        for (ch in label) {
            if (qi < q.length && ch == q[qi]) { qi++; matched++ }
        }
        if (qi == q.length) return 300 + matched * 5
        if (sub.contains(q)) return 200
        return 0
    }
}
