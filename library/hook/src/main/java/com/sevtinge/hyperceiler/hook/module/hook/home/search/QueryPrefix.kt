package com.sevtinge.hyperceiler.hook.module.hook.home.search

object QueryPrefix {

    enum class Filter { NONE, APPS, SETTINGS, SHORTCUTS, FILES, WEB, MEDIA }

    data class Parsed(val filter: Filter, val text: String)

    fun parse(raw: String): Parsed {
        val s = raw.trimStart()
        if (s.isEmpty()) return Parsed(Filter.NONE, "")
        return when (s[0]) {
            '>' -> Parsed(Filter.APPS, s.substring(1).trimStart())
            '=' -> Parsed(Filter.SETTINGS, s.substring(1).trimStart())
            '#' -> Parsed(Filter.SHORTCUTS, s.substring(1).trimStart())
            '.' -> Parsed(Filter.FILES, s.substring(1).trimStart())
            '?' -> Parsed(Filter.WEB, s.substring(1).trimStart())
            '@' -> Parsed(Filter.MEDIA, s.substring(1).trimStart())
            else -> Parsed(Filter.NONE, s)
        }
    }

    fun accepts(filter: Filter, type: ResultType): Boolean = when (filter) {
        Filter.NONE -> true
        Filter.APPS -> type == ResultType.APP
        Filter.SETTINGS -> type == ResultType.SETTING
        Filter.SHORTCUTS -> false
        Filter.FILES -> false
        Filter.WEB -> type == ResultType.WEB
        Filter.MEDIA -> false
    }
}
