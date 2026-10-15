package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.graphics.Color
import android.util.TypedValue

object ColorScheme {

    data class Tokens(
        val bg: Int,
        val surface: Int,
        val text: Int,
        val textDim: Int,
        val accent: Int,
        val rowPressed: Int,
    )

    fun of(ctx: Context): Tokens {
        val tv = TypedValue()
        val theme = ctx.theme

        val accent = resolve(theme, tv, android.R.attr.colorAccent, 0xFF5C6BC0.toInt())
        val text = resolve(theme, tv, android.R.attr.textColorPrimary, Color.WHITE)
        val textDim = (text and 0x00FFFFFF) or 0x99000000.toInt()

        return Tokens(
            bg = 0xE8101014.toInt(),
            surface = 0x33FFFFFF,
            text = text,
            textDim = textDim,
            accent = accent,
            rowPressed = 0x22FFFFFF,
        )
    }

    private fun resolve(theme: android.content.res.Resources.Theme, tv: TypedValue, attr: Int, def: Int): Int {
        return if (theme.resolveAttribute(attr, tv, true)) {
            if (tv.resourceId != 0) {
                runCatching { theme.resources.getColor(tv.resourceId, theme) }.getOrDefault(tv.data)
            } else tv.data
        } else def
    }
}
