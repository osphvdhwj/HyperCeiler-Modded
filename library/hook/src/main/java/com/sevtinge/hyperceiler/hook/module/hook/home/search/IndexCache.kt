package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object IndexCache {

    private const val TAG = "IndexCache"

    @Volatile var apps: List<SearchResult>? = null; private set
    @Volatile var settings: List<SearchResult>? = null; private set
    @Volatile var shortcuts: List<SearchResult>? = null; private set
    @Volatile var media: List<SearchResult>? = null; private set
    @Volatile var contacts: List<SearchResult>? = null; private set
    @Volatile var calendar: List<SearchResult>? = null; private set
    @Volatile var files: List<SearchResult>? = null; private set

    private var receiverRegistered = false

    fun warm(ctx: Context, includeFiles: Boolean) {
        registerReceiver(ctx)
        
        if (apps == null) {
            Thread {
                SearchIndex.ensureBuilt(ctx)
                apps = SearchIndex.apps()
            }.start()
        }
        if (settings == null) {
            Thread { settings = SettingsIndex.build(ctx) }.start()
        }
        if (shortcuts == null) {
            Thread { shortcuts = runCatching { ShortcutsIndex.build(ctx) }.getOrNull() }.start()
        }
        if (media == null) {
            Thread { media = runCatching { MediaIndex.build(ctx) }.getOrNull() }.start()
        }
        if (contacts == null) {
            Thread { contacts = runCatching { ContactsIndex.build(ctx) }.getOrNull() }.start()
        }
        if (calendar == null) {
            Thread { calendar = runCatching { CalendarIndex.build(ctx) }.getOrNull() }.start()
        }
        if (includeFiles && files == null) {
            Thread { files = runCatching { FilesIndex.build(ctx) }.getOrNull() }.start()
        }
    }

    fun invalidateApps() { apps = null }
    fun invalidateAll() {
        apps = null; settings = null; shortcuts = null
        media = null; contacts = null; calendar = null; files = null
    }

    private fun registerReceiver(ctx: Context) {
        if (receiverRegistered) return
        synchronized(this) {
            if (receiverRegistered) return
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            runCatching {
                ctx.applicationContext.registerReceiver(object : BroadcastReceiver() {
                    override fun onReceive(c: Context?, i: Intent?) {
                        logD(TAG, "package change → invalidate apps")
                        invalidateApps()
                    }
                }, filter)
                receiverRegistered = true
            }
        }
    }
}
