package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Telephony
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap

object MessagesIndex {

    fun build(ctx: Context): List<SearchResult> {
        // The launcher needs READ_SMS for this; without it the query throws
        // and we return empty so the rest of Global Search is unaffected.
        return runCatching {
            val results = ArrayList<SearchResult>()
            val defaultSms = Telephony.Sms.getDefaultSmsPackage(ctx)
            val icon: Drawable? = runCatching {
                ctx.packageManager.getApplicationIcon(defaultSms ?: "com.android.messaging")
            }.getOrNull()

            val cursor = ctx.contentResolver.query(
                Uri.parse("content://sms/inbox"),
                arrayOf("_id", "address", "body", "date"),
                null, null, "date DESC"
            ) ?: return emptyList()
            cursor.use { c ->
                val limit = 200
                var n = 0
                while (c.moveToNext() && n < limit) {
                    val address = c.getString(1) ?: continue
                    val body = c.getString(2) ?: continue
                    val label = if (body.length > 60) body.substring(0, 60) + "…" else body
                    results.add(
                        SearchResult(
                            type = ResultType.MESSAGE,
                            label = label,
                            subtitle = address,
                            icon = icon,
                            intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$address"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    )
                    n++
                }
            }
            results
        }.getOrDefault(emptyList())
    }
}
