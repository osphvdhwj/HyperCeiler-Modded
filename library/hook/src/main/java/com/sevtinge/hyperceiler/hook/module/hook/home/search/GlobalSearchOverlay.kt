package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sevtinge.hyperceiler.hook.R
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object GlobalSearchOverlay : BaseHook() {

    private const val TAG = "GlobalSearchOverlay"
    private const val PREF = "home_layout_global_search_enable"
    private const val SECTION_CAP = 5

    @Volatile private var overlay: View? = null
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var pendingQuery: Runnable? = null
    @Volatile private var settingsCache: List<SearchResult>? = null
    @Volatile private var shortcutsCache: List<SearchResult>? = null
    @Volatile private var mediaCache: List<SearchResult>? = null
    @Volatile private var contactsCache: List<SearchResult>? = null
    @Volatile private var calendarCache: List<SearchResult>? = null
    @Volatile private var filesCache: List<SearchResult>? = null

    override fun init() {
        if (!mPrefsMap.getBoolean(PREF)) {
            logD(TAG, "pref disabled, skip")
            return
        }
        hookSearchBar()
        hookSearchBarContainer()
    }

    private fun hookSearchBar() {
        val cls = findClassIfExists("com.miui.home.launcher.SearchBar") ?: run {
            logD(TAG, "SearchBar class not found"); return
        }
        cls.hookBeforeAllMethods("openSearch") { param ->
            logD(TAG, "SearchBar.openSearch intercepted")
            param.result = null
            val self = param.thisObject as? View ?: return@hookBeforeAllMethods
            showOverlay(self)
        }
    }

    private fun hookSearchBarContainer() {
        val cls = findClassIfExists("com.miui.home.launcher.SearchBarContainer") ?: run {
            logD(TAG, "SearchBarContainer class not found"); return
        }
        cls.hookBeforeAllMethods("openSearch") { param ->
            logD(TAG, "SearchBarContainer.openSearch intercepted")
            param.result = null
            val self = param.thisObject as? View ?: return@hookBeforeAllMethods
            showOverlay(self)
        }
    }

    private fun showOverlay(anchor: View) {
        if (overlay != null) return
        val ctx = anchor.context ?: return
        val activity = ctx as? Activity ?: return
        val root = activity.window?.decorView as? ViewGroup ?: return

        SearchIndex.ensureBuilt(ctx)
        settingsCache = SettingsIndex.build(ctx)
        Thread {
            shortcutsCache = runCatching { ShortcutsIndex.build(ctx) }.getOrNull()
        }.start()
        Thread {
            mediaCache = runCatching { MediaIndex.build(ctx) }.getOrNull()
        }.start()
        Thread {
            contactsCache = runCatching { ContactsIndex.build(ctx) }.getOrNull()
        }.start()
        Thread {
            calendarCache = runCatching { CalendarIndex.build(ctx) }.getOrNull()
        }.start()
        if (mPrefsMap.getBoolean("home_layout_global_search_index_files")) {
            Thread {
                filesCache = runCatching { FilesIndex.build(ctx) }.getOrNull()
            }.start()
        }

        val container = FrameLayout(ctx).apply {
            setBackgroundColor(0xF2000000.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true
        }

        val column = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 96, 32, 32)
        }

        val input = EditText(ctx).apply {
            hint = "Search apps…"
            setTextColor(Color.WHITE)
            setHintTextColor(0x99FFFFFF.toInt())
            setBackgroundColor(0x22FFFFFF)
            setPadding(48, 32, 48, 32)
            isSingleLine = true
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val rv = RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply { topMargin = 24 }
        }

        val emptyView = TextView(ctx).apply {
            setTextColor(0x66FFFFFF.toInt())
            textSize = 14f
            text = "No results"
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 48 }
        }

        val adapter = ResultAdapter { result -> launch(ctx, result) }
        rv.adapter = adapter
        adapter.submit(runQuery(ctx, ""))

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                pendingQuery?.let { debounceHandler.removeCallbacks(it) }
                val r = Runnable {
                    adapter.submit(runQuery(ctx, q))
                    if (q.isNotBlank()) RecentsStore.push(ctx, q)
                }
                pendingQuery = r
                debounceHandler.postDelayed(r, 120L)
            }
        })

        input.setOnKeyListener { _, code, event ->
            if (code == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dismiss(); true
            } else false
        }

        column.addView(input)
        column.addView(emptyView)
        column.addView(rv)
        container.addView(column)
        root.addView(container)
        overlay = container
        input.requestFocus()
    }

    private fun runQuery(ctx: Context, raw: String): List<ListItem> {
        if (raw.isBlank()) {
            val recents = RecentsStore.asResults(ctx)
            if (recents.isNotEmpty()) {
                val items = ArrayList<ListItem>()
                items.add(ListItem.Header("Recent", recents.size, R.drawable.ic_search_ui_search))
                recents.forEach { items.add(ListItem.Row(it)) }
                val apps = QueryEngine.query("", SearchIndex.apps())
                val grouped = apps.groupBy { it.type }
                val appItems = grouped[ResultType.APP].orEmpty().take(SECTION_CAP)
                if (appItems.isNotEmpty()) {
                    items.add(ListItem.Header("Apps", appItems.size, R.drawable.ic_search_section_apps))
                    appItems.forEach { items.add(ListItem.Row(it)) }
                }
                return items
            }
        }
        val parsed = QueryPrefix.parse(raw)
        val all = ArrayList<SearchResult>()
        if (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.APPS) {
            all.addAll(SearchIndex.apps())
        }
        if (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.SETTINGS) {
            settingsCache?.let { all.addAll(it) }
        }
        if (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.SHORTCUTS) {
            shortcutsCache?.let { all.addAll(it) }
        }
        if (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.MEDIA) {
            mediaCache?.let { all.addAll(it) }
        }
        if (parsed.filter == QueryPrefix.Filter.NONE) {
            contactsCache?.let { all.addAll(it) }
            calendarCache?.let { all.addAll(it) }
            filesCache?.let { all.addAll(it) }
        }
        val ranked = QueryEngine.query(parsed.text, all).filter {
            QueryPrefix.accepts(parsed.filter, it.type)
        }
        val out = ArrayList<SearchResult>()
        if (parsed.text.isNotBlank() &&
            (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.WEB)) {
            out.addAll(Calculator.build(ctx, parsed.text))
            out.addAll(WebSearch.build(ctx, parsed.text))
        }
        out.addAll(ranked)
        return buildSectioned(out)
    }

    private fun launch(ctx: Context, result: SearchResult) {
        val recent = result.intent.getStringExtra(RecentsStore.EXTRA_RECENT_QUERY)
        if (recent != null) {
            val ov = overlay as? ViewGroup ?: return
            val et = findInputView(ov)
            et?.setText(recent)
            et?.setSelection(recent.length)
            return
        }
        val copy = result.intent.getStringExtra("__calc_copy")
        if (copy != null) {
            Calculator.handleCopy(ctx, copy)
            dismiss()
            return
        }
        val scPkg = result.intent.getStringExtra(ShortcutsIndex.EXTRA_SHORTCUT_PKG)
        val scId = result.intent.getStringExtra(ShortcutsIndex.EXTRA_SHORTCUT_ID)
        if (scPkg != null && scId != null) {
            val user = result.intent.getParcelableExtra<android.os.UserHandle>(
                ShortcutsIndex.EXTRA_SHORTCUT_USER
            )
            val la = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE)
                as? android.content.pm.LauncherApps
            try {
                la?.startShortcut(scPkg, scId, null, null, user)
                dismiss()
            } catch (t: Throwable) {
                logD(TAG, "shortcut launch failed: ${t.message}")
            }
            return
        }
        try {
            ctx.startActivity(result.intent)
            dismiss()
        } catch (t: Throwable) {
            logD(TAG, "launch failed: ${t.message}")
        }
    }

    private fun findInputView(root: ViewGroup): android.widget.EditText? {
        for (i in 0 until root.childCount) {
            val c = root.getChildAt(i)
            if (c is android.widget.EditText) return c
            if (c is ViewGroup) findInputView(c)?.let { return it }
        }
        return null
    }

    private fun dismiss() {
        val v = overlay ?: return
        (v.parent as? ViewGroup)?.removeView(v)
        overlay = null
    }

    private val SECTION_ORDER = listOf(
        ResultType.SETTING, ResultType.WEB,
        ResultType.APP, ResultType.SHORTCUT,
        ResultType.CONTACT, ResultType.CALENDAR,
        ResultType.MEDIA, ResultType.FILE,
    )

    private fun buildSectioned(results: List<SearchResult>): List<ListItem> {
        val out = ArrayList<ListItem>()
        val grouped = results.groupBy { it.type }
        for (t in SECTION_ORDER) {
            val items = grouped[t].orEmpty()
            if (items.isEmpty()) continue
            out.add(ListItem.Header(sectionTitle(t), items.size, sectionIcon(t)))
            val visible = items.take(SECTION_CAP)
            visible.forEach { out.add(ListItem.Row(it)) }
            if (items.size > SECTION_CAP) {
                out.add(ListItem.ShowAll(t, items.size - SECTION_CAP))
            }
        }
        return out
    }

    private fun sectionTitle(t: ResultType): String = when (t) {
        ResultType.APP -> "Apps"
        ResultType.SETTING -> "Settings"
        ResultType.WEB -> "Web"
        ResultType.SHORTCUT -> "Shortcuts"
        ResultType.MEDIA -> "Media"
        ResultType.FILE -> "Files"
        ResultType.CONTACT -> "Contacts"
        ResultType.CALENDAR -> "Calendar"
    }

    private fun sectionIcon(t: ResultType): Int = when (t) {
        ResultType.APP -> R.drawable.ic_search_section_apps
        ResultType.SETTING -> R.drawable.ic_search_section_settings
        ResultType.WEB -> R.drawable.ic_search_section_web
        ResultType.SHORTCUT -> R.drawable.ic_search_section_shortcut
        ResultType.MEDIA -> R.drawable.ic_search_section_photo
        ResultType.FILE -> R.drawable.ic_search_section_file
        ResultType.CONTACT -> R.drawable.ic_search_section_apps
        ResultType.CALENDAR -> R.drawable.ic_search_section_calc
    }

    private sealed class ListItem {
        data class Header(val title: String, val count: Int, val iconRes: Int) : ListItem()
        data class Row(val result: SearchResult) : ListItem()
        data class ShowAll(val type: ResultType, val remaining: Int) : ListItem()
    }

    private class ResultAdapter(
        private val onClick: (SearchResult) -> Unit,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val items = ArrayList<ListItem>()

        fun submit(newItems: List<ListItem>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int = when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Row -> TYPE_ROW
            is ListItem.ShowAll -> TYPE_SHOW_ALL
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val ctx = parent.context
            return if (viewType == TYPE_HEADER) {
                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(24, 40, 24, 12)
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                val icon = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(48, 48).apply { marginEnd = 16 }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                val title = TextView(ctx).apply {
                    setTextColor(0xAAFFFFFF.toInt())
                    textSize = 12f
                    letterSpacing = 0.1f
                }
                row.addView(icon); row.addView(title)
                HeaderVH(row, icon, title)
            } else if (viewType == TYPE_SHOW_ALL) {
                val tv = TextView(ctx).apply {
                    setTextColor(0x99FFFFFF.toInt())
                    textSize = 13f
                    setPadding(72, 24, 24, 32)
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                ShowAllVH(tv)
            } else {
                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(24, 24, 24, 24)
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    isClickable = true
                    isFocusable = true
                }
                val icon = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(96, 96).apply { marginEnd = 32 }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                val col = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                val label = TextView(ctx).apply {
                    setTextColor(Color.WHITE); textSize = 16f; maxLines = 1
                }
                val sub = TextView(ctx).apply {
                    setTextColor(0x88FFFFFF.toInt()); textSize = 12f; maxLines = 1
                }
                col.addView(label); col.addView(sub)
                row.addView(icon); row.addView(col)
                RowVH(row, icon, label, sub)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is ListItem.Header -> (holder as HeaderVH).bind(item)
                is ListItem.Row -> (holder as RowVH).bind(item.result, onClick)
                is ListItem.ShowAll -> (holder as ShowAllVH).bind(item)
            }
        }

        override fun getItemCount(): Int = items.size

        companion object {
            const val TYPE_HEADER = 0
            const val TYPE_ROW = 1
            const val TYPE_SHOW_ALL = 2
        }

        class ShowAllVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(s: ListItem.ShowAll) {
                (itemView as TextView).text = "Show ${s.remaining} more ${s.type.name.lowercase()}s"
            }
        }

        class HeaderVH(itemView: View, val icon: ImageView, val title: TextView) :
            RecyclerView.ViewHolder(itemView) {
            fun bind(h: ListItem.Header) {
                title.text = "${h.title}  ·  ${h.count}"
                if (h.iconRes != 0) icon.setImageResource(h.iconRes) else icon.setImageDrawable(null)
            }
        }

        class RowVH(itemView: View, val icon: ImageView, val label: TextView, val sub: TextView) :
            RecyclerView.ViewHolder(itemView) {
            fun bind(r: SearchResult, onClick: (SearchResult) -> Unit) {
                icon.setImageDrawable(r.icon)
                label.text = r.label
                sub.text = r.subtitle ?: ""
                itemView.setOnClickListener { onClick(r) }
                itemView.setOnLongClickListener {
                    if (r.type == ResultType.APP) {
                        SearchContextMenu.show(itemView.context, r)
                        true
                    } else false
                }
            }
        }
    }
}
