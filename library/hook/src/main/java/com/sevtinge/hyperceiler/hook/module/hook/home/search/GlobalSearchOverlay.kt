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
        adapter.submit(QueryEngine.query("", SearchIndex.apps()))

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                adapter.submit(QueryEngine.query(q, SearchIndex.apps()))
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

    private class ResultAdapter(
        private val onClick: (SearchResult) -> Unit,
    ) : RecyclerView.Adapter<ResultAdapter.VH>() {

        private val items = ArrayList<SearchResult>()

        fun submit(newItems: List<SearchResult>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val ctx = parent.context
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
            return VH(row, icon, label, sub)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.icon.setImageDrawable(item.icon)
            holder.label.text = item.label
            holder.sub.text = item.subtitle ?: ""
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = items.size

        class VH(
            itemView: View,
            val icon: ImageView,
            val label: TextView,
            val sub: TextView,
        ) : RecyclerView.ViewHolder(itemView)
    }
}
