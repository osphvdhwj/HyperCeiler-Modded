package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.sevtinge.hyperceiler.hook.R
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object MediaIndex {

    private const val TAG = "MediaIndex"
    private const val MAX = 300

    fun build(ctx: Context): List<SearchResult> {
        val out = ArrayList<SearchResult>(MAX)
        val resolver = ctx.contentResolver
        val imgIcon = runCatching {
            androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_file_image)
        }.getOrNull()
        val vidIcon = runCatching {
            androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_file_video)
        }.getOrNull()
        val audIcon = runCatching {
            androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_search_file_audio)
        }.getOrNull()

        collect(resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.MIME_TYPE),
            null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC"
        ), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, out, imgIcon, "Photo", ctx)

        if (out.size < MAX) collect(resolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.MIME_TYPE),
            null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC"
        ), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, out, vidIcon, "Video", ctx)

        if (out.size < MAX) collect(resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.MIME_TYPE),
            "${MediaStore.Audio.Media.IS_MUSIC} != 0", null, "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        ), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, out, audIcon, "Audio", ctx)

        logD(TAG, "media: ${out.size}")
        return out
    }

    private fun collect(
        c: Cursor?,
        base: Uri,
        out: MutableList<SearchResult>,
        icon: android.graphics.drawable.Drawable?,
        subtitle: String,
        ctx: Context,
    ) {
        if (c == null) return
        c.use {
            val idIdx = c.getColumnIndex("_id")
            val nameIdx = c.getColumnIndex("_display_name")
            while (c.moveToNext() && out.size < MAX) {
                val id = c.getLong(idIdx)
                val name = c.getString(nameIdx) ?: continue
                val uri = Uri.withAppendedPath(base, id.toString())
                val view = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, c.getString(c.getColumnIndex("mime_type")) ?: "*/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                out.add(
                    SearchResult(
                        type = ResultType.MEDIA,
                        label = name,
                        subtitle = subtitle,
                        icon = icon,
                        intent = view,
                    )
                )
            }
        }
    }
}
