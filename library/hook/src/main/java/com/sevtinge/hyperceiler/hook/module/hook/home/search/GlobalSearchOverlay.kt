package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.app.Activity
import android.content.Context
import android.graphics.Color
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

    @Volatile private var overlay: View? = null

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

        val adapter = ResultAdapter { result -> launch(ctx, result) }
        rv.adapter = adapter
        adapter.submit(buildSectioned(QueryEngine.query("", SearchIndex.apps())))

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                adapter.submit(buildSectioned(QueryEngine.query(q, SearchIndex.apps())))
            }
        })

        input.setOnKeyListener { _, code, event ->
            if (code == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dismiss(); true
            } else false
        }

        column.addView(input)
        column.addView(rv)
        container.addView(column)
        root.addView(container)
        overlay = container
        input.requestFocus()
    }

    private fun launch(ctx: Context, result: SearchResult) {
        try {
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
    }

    private fun buildSectioned(results: List<SearchResult>): List<ListItem> {
        val out = ArrayList<ListItem>()
        val grouped = results.groupBy { it.type }
        for (t in ResultType.entries) {
            val items = grouped[t].orEmpty()
            if (items.isEmpty()) continue
            out.add(ListItem.Header(sectionTitle(t), items.size, sectionIcon(t)))
            items.forEach { out.add(ListItem.Row(it)) }
        }
        return out
    }

    private fun sectionTitle(t: ResultType): String = when (t) {
        ResultType.APP -> "Apps"
        ResultType.SETTING -> "Settings"
        ResultType.WEB -> "Web"
    }

    private fun sectionIcon(t: ResultType): Int = when (t) {
        ResultType.APP -> R.drawable.ic_search_section_apps
        ResultType.SETTING -> R.drawable.ic_search_section_settings
        ResultType.WEB -> R.drawable.ic_search_section_web
    }

    private sealed class ListItem {
        data class Header(val title: String, val count: Int, val iconRes: Int) : ListItem()
        data class Row(val result: SearchResult) : ListItem()
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
            }
        }

        override fun getItemCount(): Int = items.size

        companion object {
            const val TYPE_HEADER = 0
            const val TYPE_ROW = 1
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
            }
        }
    }
}
