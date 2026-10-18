package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.app.Activity
import android.content.Context
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

    @Volatile private var overlay: ViewGroup? = null
    @Volatile private var adapterRef: ResultAdapter? = null
    @Volatile private var forcedFilter: QueryPrefix.Filter = QueryPrefix.Filter.NONE

    private val debounceHandler = Handler(Looper.getMainLooper())
    private var pendingQuery: Runnable? = null

    override fun init() {
        if (!mPrefsMap.getBoolean(PREF)) {
            logD(TAG, "pref disabled, skip")
            return
        }
        try {
            hookSearchBar()
            hookSearchBarContainer()
        } catch (t: Throwable) {
            logD(TAG, "GlobalSearchOverlay init failed: ${t.message}")
        }
    }

    private fun hookSearchBar() {
        val cls = findClassIfExists("com.miui.home.launcher.SearchBar") ?: run {
            logD(TAG, "SearchBar class not found"); return
        }
        cls.hookBeforeAllMethods("openSearch") { param ->
            logD(TAG, "SearchBar.openSearch intercepted")
            Diagnostics.lastSearchBarHook = System.currentTimeMillis()
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
            Diagnostics.lastContainerHook = System.currentTimeMillis()
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

        Diagnostics.lastOverlayOpen = System.currentTimeMillis()
        val tokens = ColorScheme.of(ctx)
        IndexCache.warm(ctx, mPrefsMap.getBoolean("home_layout_global_search_index_files"))

        val container = FrameLayout(ctx).apply {
            setBackgroundColor(tokens.bg)
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
            hint = "Search apps, settings, contacts, files…"
            setTextColor(tokens.text)
            setHintTextColor(tokens.textDim)
            setBackgroundColor(tokens.surface)
            setPadding(48, 32, 48, 32)
            isSingleLine = true
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val emptyView = TextView(ctx).apply {
            setTextColor(tokens.textDim)
            textSize = 14f
            text = "No results"
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 48 }
        }

        val rv = RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply { topMargin = 24 }
        }

        val adapter = ResultAdapter(tokens) { result -> launch(ctx, result) }
        adapterRef = adapter
        rv.adapter = adapter

        val tabs = CategoryTabs(ctx, tokens) { f ->
            forcedFilter = f
            val q = input.text?.toString() ?: ""
            QueryExecutor.submit(
                work = { runQuery(ctx, q) },
                onResult = { items ->
                    emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                    adapterRef?.submit(items)
                }
            )
        }

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                pendingQuery?.let { debounceHandler.removeCallbacks(it) }
                val r = Runnable {
                    QueryExecutor.submit(
                        work = { runQuery(ctx, q) },
                        onResult = { items ->
                            adapter.currentQuery = q
                            adapter.selectedPos = -1
                            emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                            adapter.submit(items)
                        }
                    )
                    if (q.isNotBlank()) RecentsStore.push(ctx, q)
                }
                pendingQuery = r
                debounceHandler.postDelayed(r, 120L)
            }
        })

        input.setOnLongClickListener { Diagnostics.show(ctx); true }

        input.setOnKeyListener { _, code, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
            when (code) {
                KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> { dismiss(); true }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    val p = adapter.moveSelection(1); if (p >= 0) rv.scrollToPosition(p); true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    val p = adapter.moveSelection(-1); if (p >= 0) rv.scrollToPosition(p); true
                }
                KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    val row = adapter.currentRow()
                    if (row != null) { launch(ctx, row); true } else false
                }
                else -> false
            }
        }

        column.addView(input)
        column.addView(tabs.root)
        column.addView(emptyView)
        column.addView(rv)
        container.addView(column)
        root.addView(container)
        overlay = container
        input.requestFocus()

        QueryExecutor.submit(
            work = { runQuery(ctx, "") },
            onResult = { items ->
                emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                adapter.submit(items)
            }
        )
    }

    private fun runQuery(ctx: Context, raw: String): List<ListItem> {
        val t0 = System.currentTimeMillis()
        if (raw.isBlank()) {
            val recents = RecentsStore.asResults(ctx)
            val items = ArrayList<ListItem>()
            if (recents.isNotEmpty()) {
                items.add(ListItem.Header("Recent", recents.size, R.drawable.ic_search_ui_search))
                recents.forEach { items.add(ListItem.Row(it)) }
            }
            val apps = (IndexCache.apps ?: emptyList()).take(SECTION_CAP)
            if (apps.isNotEmpty()) {
                items.add(ListItem.Header("Apps", apps.size, R.drawable.ic_search_section_apps))
                apps.forEach { items.add(ListItem.Row(it)) }
            }
            Diagnostics.lastQueryMs = System.currentTimeMillis() - t0
            Diagnostics.lastQueryLen = raw.length
            Diagnostics.lastResultCount = items.size
            return items
        }

        var parsed = QueryPrefix.parse(raw)
        if (forcedFilter != QueryPrefix.Filter.NONE && parsed.filter == QueryPrefix.Filter.NONE) {
            parsed = QueryPrefix.Parsed(forcedFilter, parsed.text)
        }

        val all = ArrayList<SearchResult>()
        if (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.APPS) {
            IndexCache.apps?.let { all.addAll(it) }
        }
        if (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.SETTINGS) {
            IndexCache.settings?.let { all.addAll(it) }
        }
        if (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.SHORTCUTS) {
            IndexCache.shortcuts?.let { all.addAll(it) }
        }
        if (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.MEDIA) {
            IndexCache.media?.let { all.addAll(it) }
        }
        if (parsed.filter == QueryPrefix.Filter.NONE) {
            IndexCache.contacts?.let { all.addAll(it) }
            IndexCache.calendar?.let { all.addAll(it) }
            IndexCache.files?.let { all.addAll(it) }
        }

        val freq = { r: SearchResult ->
            if (r.type == ResultType.APP)
                AppFrequencyStore.score(ctx, r.intent.component?.packageName ?: "")
            else 0
        }
        val ranked = QueryEngine.query(parsed.text, all, freq).filter {
            QueryPrefix.accepts(parsed.filter, it.type)
        }

        val out = ArrayList<SearchResult>()
        if (parsed.text.isNotBlank() &&
            (parsed.filter == QueryPrefix.Filter.NONE || parsed.filter == QueryPrefix.Filter.WEB)) {
            out.addAll(Calculator.build(ctx, parsed.text))
            out.addAll(WebSearch.build(ctx, parsed.text))
        }
        out.addAll(ranked)

        val r = buildSectioned(out)
        Diagnostics.lastQueryMs = System.currentTimeMillis() - t0
        Diagnostics.lastQueryLen = raw.length
        Diagnostics.lastResultCount = r.size
        return r
    }

    private fun launch(ctx: Context, result: SearchResult) {
        val recent = result.intent.getStringExtra(RecentsStore.EXTRA_RECENT_QUERY)
        if (recent != null) {
            val ov = overlay ?: return
            val et = findInputView(ov)
            et?.setText(recent)
            et?.setSelection(recent.length)
            return
        }
        val copy = result.intent.getStringExtra("__calc_copy")
        if (copy != null) { Calculator.handleCopy(ctx, copy); dismiss(); return }

        val scPkg = result.intent.getStringExtra(ShortcutsIndex.EXTRA_SHORTCUT_PKG)
        val scId = result.intent.getStringExtra(ShortcutsIndex.EXTRA_SHORTCUT_ID)
        if (scPkg != null && scId != null) {
            val user: android.os.UserHandle = result.intent
                .getParcelableExtra<android.os.UserHandle>(ShortcutsIndex.EXTRA_SHORTCUT_USER)
                ?: android.os.Process.myUserHandle()
            val la = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE)
                as? android.content.pm.LauncherApps
            try {
                AppFrequencyStore.record(ctx, scPkg)
                la?.startShortcut(scPkg, scId, null, null, user)
                dismiss()
            } catch (t: Throwable) {
                logD(TAG, "shortcut launch failed: ${t.message}")
            }
            return
        }

        try {
            if (result.type == ResultType.APP) {
                val pkg = result.intent.component?.packageName
                if (!pkg.isNullOrEmpty()) AppFrequencyStore.record(ctx, pkg)
            }
            ctx.startActivity(result.intent)
            dismiss()
        } catch (t: Throwable) {
            logD(TAG, "launch failed: ${t.message}")
        }
    }

    private fun dismiss() {
        val v = overlay ?: return
        (v.parent as? ViewGroup)?.removeView(v)
        overlay = null
        adapterRef = null
        forcedFilter = QueryPrefix.Filter.NONE
        QueryExecutor.invalidate()
    }

    private fun findInputView(root: ViewGroup): EditText? {
        for (i in 0 until root.childCount) {
            val c = root.getChildAt(i)
            if (c is EditText) return c
            if (c is ViewGroup) findInputView(c)?.let { return it }
        }
        return null
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
            for (r in visible) {
                out.add(ListItem.Row(r))
                if (r.type == ResultType.APP) {
                    val pkg = r.intent.component?.packageName
                    if (!pkg.isNullOrEmpty()) out.add(ListItem.QuickActions(pkg, null))
                }
            }
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
        data class QuickActions(val pkg: String, val user: android.os.UserHandle?) : ListItem()
    }

    private class ResultAdapter(
        private val tokens: ColorScheme.Tokens,
        private val onClick: (SearchResult) -> Unit,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var currentQuery: String = ""
        var selectedPos: Int = -1
            set(v) {
                val old = field
                field = v
                if (old >= 0) notifyItemChanged(old)
                if (v >= 0) notifyItemChanged(v)
            }

        private val items = ArrayList<ListItem>()

        fun submit(newItems: List<ListItem>) {
            items.clear(); items.addAll(newItems); notifyDataSetChanged()
        }

        fun moveSelection(delta: Int): Int {
            if (items.isEmpty()) return -1
            var p = selectedPos
            var guard = 0
            while (guard < items.size) {
                p += delta
                if (p < 0) p = items.size - 1
                if (p >= items.size) p = 0
                if (items[p] is ListItem.Row) { selectedPos = p; return p }
                guard++
            }
            return -1
        }

        fun currentRow(): SearchResult? = (items.getOrNull(selectedPos) as? ListItem.Row)?.result

        override fun getItemViewType(position: Int): Int = when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Row -> TYPE_ROW
            is ListItem.ShowAll -> TYPE_SHOW_ALL
            is ListItem.QuickActions -> TYPE_QUICK_ACTIONS
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val ctx = parent.context
            return when (viewType) {
                TYPE_HEADER -> {
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
                    val title = TextView(ctx).apply { setTextColor(tokens.textDim); textSize = 12f }
                    row.addView(icon); row.addView(title)
                    HeaderVH(row, icon, title)
                }
                TYPE_QUICK_ACTIONS -> {
                    val row = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(72, 8, 24, 24)
                        layoutParams = RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    QuickActionsVH(row, tokens)
                }
                TYPE_SHOW_ALL -> {
                    val tv = TextView(ctx).apply {
                        setTextColor(tokens.textDim); textSize = 13f
                        setPadding(72, 24, 24, 32)
                        layoutParams = RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    ShowAllVH(tv)
                }
                else -> {
                    val row = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(24, 24, 24, 24)
                        layoutParams = RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        isClickable = true; isFocusable = true
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
                        setTextColor(tokens.text); textSize = 16f; maxLines = 1
                    }
                    val sub = TextView(ctx).apply {
                        setTextColor(tokens.textDim); textSize = 12f; maxLines = 1
                    }
                    col.addView(label); col.addView(sub)
                    row.addView(icon); row.addView(col)
                    RowVH(row, icon, label, sub)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is ListItem.Header -> (holder as HeaderVH).bind(item)
                is ListItem.Row -> (holder as RowVH).bind(
                    item.result, onClick, currentQuery, tokens.accent, position == selectedPos
                )
                is ListItem.ShowAll -> (holder as ShowAllVH).bind(item)
                is ListItem.QuickActions -> (holder as QuickActionsVH).bind(item)
            }
        }

        override fun getItemCount(): Int = items.size

        companion object {
            const val TYPE_HEADER = 0
            const val TYPE_ROW = 1
            const val TYPE_SHOW_ALL = 2
            const val TYPE_QUICK_ACTIONS = 3
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
            fun bind(r: SearchResult, onClick: (SearchResult) -> Unit, q: String, accent: Int, selected: Boolean) {
                icon.setImageDrawable(r.icon)
                label.text = MatchHighlighter.apply(r.label, q, accent)
                sub.text = r.subtitle ?: ""
                itemView.setBackgroundColor(if (selected) 0x33FFFFFF else 0x00000000)
                itemView.setOnClickListener { onClick(r) }
                itemView.setOnLongClickListener {
                    SearchContextMenu.show(itemView.context, r); true
                }
            }
        }

        class ShowAllVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(s: ListItem.ShowAll) {
                (itemView as TextView).text = "Show ${s.remaining} more ${s.type.name.lowercase()}s"
            }
        }

        class QuickActionsVH(
            private val row: LinearLayout,
            private val tokens: ColorScheme.Tokens,
        ) : RecyclerView.ViewHolder(row) {
            fun bind(item: ListItem.QuickActions) {
                row.removeAllViews()
                val ctx = row.context
                val la = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE)
                    as? android.content.pm.LauncherApps
                val shortcuts = QuickActions.forPackage(ctx, item.pkg, item.user)
                for (s in shortcuts) {
                    val label = s.shortLabel?.toString() ?: s.longLabel?.toString() ?: continue
                    val chip = TextView(ctx).apply {
                        text = label
                        textSize = 12f
                        setTextColor(tokens.text)
                        setPadding(28, 14, 28, 14)
                        background = android.graphics.drawable.GradientDrawable().apply {
                            cornerRadius = 40f; setColor(tokens.surface)
                        }
                        isClickable = true; isFocusable = true
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { marginEnd = 12 }
                        setOnClickListener {
                            val u = item.user ?: android.os.Process.myUserHandle()
                            runCatching { la?.startShortcut(item.pkg, s.id, null, null, u) }
                        }
                    }
                    row.addView(chip)
                }
            }
        }
    }
}
