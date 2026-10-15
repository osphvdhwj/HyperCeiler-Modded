package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.app.AlertDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Diagnostics {

    private val fmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    @Volatile var lastSearchBarHook: Long = 0L
    @Volatile var lastContainerHook: Long = 0L
    @Volatile var lastOverlayOpen: Long = 0L
    @Volatile var lastQueryMs: Long = 0L
    @Volatile var lastQueryLen: Int = 0
    @Volatile var lastResultCount: Int = 0

    fun show(ctx: Context) {
        val sb = StringBuilder()
        sb.appendLine("Hooks")
        sb.appendLine("  SearchBar.openSearch:      ${ts(lastSearchBarHook)}")
        sb.appendLine("  SearchBarContainer.open:   ${ts(lastContainerHook)}")
        sb.appendLine("  Overlay.lastOpen:          ${ts(lastOverlayOpen)}")
        sb.appendLine()
        sb.appendLine("Index caches")
        sb.appendLine("  apps:     ${n(IndexCache.apps)}")
        sb.appendLine("  settings: ${n(IndexCache.settings)}")
        sb.appendLine("  shortcuts:${n(IndexCache.shortcuts)}")
        sb.appendLine("  media:    ${n(IndexCache.media)}")
        sb.appendLine("  contacts: ${n(IndexCache.contacts)}")
        sb.appendLine("  calendar: ${n(IndexCache.calendar)}")
        sb.appendLine("  files:    ${n(IndexCache.files)}")
        sb.appendLine()
        sb.appendLine("Last query")
        sb.appendLine("  duration: ${lastQueryMs}ms")
        sb.appendLine("  length:   $lastQueryLen")
        sb.appendLine("  results:  $lastResultCount")

        AlertDialog.Builder(ctx)
            .setTitle("Global Search — Diagnostics")
            .setMessage(sb.toString())
            .setPositiveButton("Close", null)
            .show()
    }

    private fun ts(t: Long): String =
        if (t == 0L) "(never)" else fmt.format(Date(t))

    private fun n(l: List<*>?): String = l?.size?.toString() ?: "(null)"
}
