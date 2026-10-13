package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.sevtinge.hyperceiler.hook.R
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object CalendarIndex {

    private const val TAG = "CalendarIndex"
    private const val MAX = 300

    fun build(ctx: Context): List<SearchResult> {
        val out = ArrayList<SearchResult>(MAX)
        val icon = runCatching {
            androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_section_calc)
        }.getOrNull()
        val now = System.currentTimeMillis() - 24L * 60 * 60 * 1000
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
        )
        val selection = "${CalendarContract.Events.DTSTART} >= ?"
        ctx.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection, selection, arrayOf(now.toString()),
            "${CalendarContract.Events.DTSTART} ASC"
        )?.use { c ->
            val idIdx = c.getColumnIndex(CalendarContract.Events._ID)
            val titleIdx = c.getColumnIndex(CalendarContract.Events.TITLE)
            val startIdx = c.getColumnIndex(CalendarContract.Events.DTSTART)
            while (c.moveToNext() && out.size < MAX) {
                val id = c.getLong(idIdx)
                val title = c.getString(titleIdx) ?: continue
                val start = c.getLong(startIdx)
                val whenStr = android.text.format.DateFormat
                    .format("MMM dd HH:mm", start).toString()
                val view = Intent(Intent.ACTION_VIEW)
                    .setData(CalendarContract.Events.CONTENT_URI.buildUpon().appendPath(id.toString()).build())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                out.add(
                    SearchResult(
                        type = ResultType.CALENDAR,
                        label = title,
                        subtitle = whenStr,
                        icon = icon,
                        intent = view,
                    )
                )
            }
        }
        logD(TAG, "events: ${out.size}")
        return out
    }
}
