package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object SearchContextMenu {

    fun show(ctx: Context, result: SearchResult) {
        val label = result.label
        val pkg = result.intent.component?.packageName
        val items = ArrayList<String>()
        val actions = ArrayList<() -> Unit>()

        if (pkg != null) {
            items.add("App info")
            actions.add {
                ctx.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            items.add("Open")
            actions.add { ctx.startActivity(result.intent) }
        } else {
            items.add("Open")
            actions.add { ctx.startActivity(result.intent) }
        }

        AlertDialog.Builder(ctx)
            .setTitle(label)
            .setItems(items.toTypedArray()) { _, which -> actions[which].invoke() }
            .show()
    }
}
