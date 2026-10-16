package com.sevtinge.hyperceiler.hook.module.hook.home.search

object QueryEngine {

    private const val MAX_RESULTS = 50
    private const val FREQ_CAP = 5
    private const val FREQ_MULT = 10

    fun query(rawQuery: String, all: List<SearchResult>): List<SearchResult> =
        query(rawQuery, all, null)

    fun query(
        rawQuery: String,
        all: List<SearchResult>,
        freqBoost: ((SearchResult) -> Int)?,
    ): List<SearchResult> {
        val tokens = rawQuery.trim().lowercase()
            .split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return all.take(MAX_RESULTS)

        val scored = ArrayList<SearchResult>(all.size)
        for (item in all) {
            val label = item.label.lowercase()
            val sub = item.subtitle?.lowercase() ?: ""
            var total = 0
            var ok = true
            for (t in tokens) {
                val s = scoreToken(t, label, sub)
                if (s == 0) { ok = false; break }
                total += s
            }
            if (!ok) continue
            val boost = ((freqBoost?.invoke(item) ?: 0).coerceAtMost(FREQ_CAP)) * FREQ_MULT
            scored.add(item.copy(score = total + boost))
        }
        scored.sortWith(
            compareByDescending<SearchResult> { it.score }
                .thenBy { it.label.lowercase() }
        )
        return scored.take(MAX_RESULTS)
    }

    private fun scoreToken(q: String, label: String, sub: String): Int {
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
