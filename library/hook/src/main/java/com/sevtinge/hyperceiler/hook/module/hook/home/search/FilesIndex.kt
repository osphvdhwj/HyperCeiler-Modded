package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import com.sevtinge.hyperceiler.hook.R
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD
import java.io.File

object FilesIndex {

    private const val TAG = "FilesIndex"
    private const val MAX = 2000
    private const val MAX_DEPTH = 6

    private val SKIP_DIRS = setOf(
        "Android", ".thumbnails", ".cache", "cache", "log", "logs",
        ".trash", ".Trash", "MIUI", ".miui"
    )

    fun build(ctx: Context): List<SearchResult> {
        val out = ArrayList<SearchResult>(512)
        val root = Environment.getExternalStorageDirectory() ?: return out
        val icons = mapOf(
            "image" to load(ctx, R.drawable.ic_search_file_image),
            "video" to load(ctx, R.drawable.ic_search_file_video),
            "audio" to load(ctx, R.drawable.ic_search_file_audio),
            "pdf" to load(ctx, R.drawable.ic_search_file_pdf),
            "doc" to load(ctx, R.drawable.ic_search_file_doc),
            "archive" to load(ctx, R.drawable.ic_search_file_archive),
            "other" to load(ctx, R.drawable.ic_search_section_file),
        )
        walk(root, 0, ctx, out, icons)
        logD(TAG, "files: ${out.size}")
        return out
    }

    private fun walk(
        dir: File, depth: Int, ctx: Context,
        out: MutableList<SearchResult>,
        icons: Map<String, android.graphics.drawable.Drawable?>,
    ) {
        if (depth > MAX_DEPTH || out.size >= MAX) return
        val children = dir.listFiles() ?: return
        for (f in children) {
            if (out.size >= MAX) return
            if (f.isDirectory) {
                if (f.name in SKIP_DIRS || f.name.startsWith(".")) continue
                walk(f, depth + 1, ctx, out, icons)
            } else {
                if (f.name.startsWith(".")) continue
                if (f.length() == 0L) continue
                val ext = f.extension.lowercase()
                val kind = classify(ext)
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                    ?: "application/octet-stream"
                val view = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.fromFile(f), mime)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                out.add(
                    SearchResult(
                        type = ResultType.FILE,
                        label = f.name,
                        subtitle = f.parentFile?.name ?: kind,
                        icon = icons[kind] ?: icons["other"],
                        intent = view,
                    )
                )
            }
        }
    }

    private fun classify(ext: String): String = when (ext) {
        "jpg", "jpeg", "png", "gif", "webp", "heic", "bmp" -> "image"
        "mp4", "mkv", "mov", "avi", "webm", "3gp" -> "video"
        "mp3", "m4a", "aac", "flac", "ogg", "wav" -> "audio"
        "pdf" -> "pdf"
        "doc", "docx", "txt", "rtf", "odt", "md" -> "doc"
        "zip", "rar", "7z", "tar", "gz" -> "archive"
        else -> "other"
    }

    private fun load(ctx: Context, res: Int): android.graphics.drawable.Drawable? = runCatching {
        androidx.core.content.ContextCompat.getDrawable(ctx, res)
    }.getOrNull()
}
