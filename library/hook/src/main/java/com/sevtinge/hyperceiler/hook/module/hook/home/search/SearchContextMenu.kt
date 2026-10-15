package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

object SearchContextMenu {

    fun show(ctx: Context, result: SearchResult) {
        val items = ArrayList<Pair<String, () -> Unit>>()

        // Open — always first
        items.add("Open" to { fire(ctx, result.intent) })

        // Type-specific actions
        when (result.type) {
            ResultType.APP -> {
                val pkg = result.intent.component?.packageName
                if (pkg != null) {
                    items.add("App info" to { openAppInfo(ctx, pkg) })
                    items.add("Copy package name" to { copy(ctx, pkg) })
                }
            }
            ResultType.SHORTCUT -> {
                val pkg = result.intent.getStringExtra(ShortcutsIndex.EXTRA_SHORTCUT_PKG)
                if (pkg != null) {
                    items.add("App info" to { openAppInfo(ctx, pkg) })
                }
            }
            ResultType.SETTING -> {
                val action = result.intent.action
                if (action != null) items.add("Copy action" to { copy(ctx, action) })
            }
            ResultType.WEB -> {
                val q = result.intent.getStringExtra("query")
                if (q != null) items.add("Copy query" to { copy(ctx, q) })
            }
            ResultType.MEDIA, ResultType.FILE -> {
                val uri = result.intent.data
                if (uri != null) {
                    items.add("Copy path" to { copy(ctx, uri.toString()) })
                    items.add("Share" to { share(ctx, uri) })
                }
            }
            ResultType.CONTACT -> {
                val uri = result.intent.data
                if (uri != null) items.add("Copy contact URI" to { copy(ctx, uri.toString()) })
            }
            ResultType.CALENDAR -> {
                val uri = result.intent.data
                if (uri != null) items.add("Copy event URI" to { copy(ctx, uri.toString()) })
            }
        }

        // Common tail
        if (result.intent.component == null) {
            items.add("Copy label" to { copy(ctx, result.label) })
        }

        AlertDialog.Builder(ctx)
            .setTitle(result.label)
            .setItems(items.map { it.first }.toTypedArray()) { _, w -> items[w].second.invoke() }
            .show()
    }

    private fun fire(ctx: Context, intent: Intent) {
        runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }
    }

    private fun openAppInfo(ctx: Context, pkg: String) {
        runCatching {
            ctx.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun copy(ctx: Context, text: String) {
        runCatching {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("hyperceiler", text))
            Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun share(ctx: Context, uri: Uri) {
        runCatching {
            val i = Intent(Intent.ACTION_SEND).apply {
                type = ctx.contentResolver.getType(uri) ?: "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(Intent.createChooser(i, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
