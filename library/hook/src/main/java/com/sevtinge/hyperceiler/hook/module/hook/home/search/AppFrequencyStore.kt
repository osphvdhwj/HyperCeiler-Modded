package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context

object AppFrequencyStore {

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("hyperceiler_global_search", Context.MODE_PRIVATE)

    fun record(ctx: Context, pkg: String) {
        if (pkg.isEmpty()) return
        val p = prefs(ctx)
        p.edit().putInt(pkg, p.getInt(pkg, 0) + 1).apply()
    }

    fun score(ctx: Context, pkg: String): Int =
        if (pkg.isEmpty()) 0 else prefs(ctx).getInt(pkg, 0)

    fun clear(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }
}
