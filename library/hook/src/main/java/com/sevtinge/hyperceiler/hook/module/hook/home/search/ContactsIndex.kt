package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.sevtinge.hyperceiler.hook.R
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object ContactsIndex {

    private const val TAG = "ContactsIndex"
    private const val MAX = 500

    fun build(ctx: Context): List<SearchResult> {
        val out = ArrayList<SearchResult>(MAX)
        val icon = runCatching {
            androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_section_apps)
        }.getOrNull()
        val cr: ContentResolver = ctx.contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )
        cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
        )?.use { c ->
            val idIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val seen = HashSet<Long>()
            while (c.moveToNext() && out.size < MAX) {
                val id = c.getLong(idIdx)
                if (!seen.add(id)) continue
                val name = c.getString(nameIdx) ?: continue
                val number = c.getString(numIdx).orEmpty()
                val view = Intent(Intent.ACTION_VIEW)
                    .setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id.toString()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                out.add(
                    SearchResult(
                        type = ResultType.CONTACT,
                        label = name,
                        subtitle = number.ifBlank { "Contact" },
                        icon = icon,
                        intent = view,
                    )
                )
            }
        }
        logD(TAG, "contacts: ${out.size}")
        return out
    }
}
