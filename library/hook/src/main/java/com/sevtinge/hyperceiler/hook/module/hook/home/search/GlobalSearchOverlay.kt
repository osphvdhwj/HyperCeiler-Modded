package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object GlobalSearchOverlay : BaseHook() {

    private const val TAG = "GlobalSearchOverlay"
    private const val PREF = "home_layout_global_search_enable"

    @Volatile
    private var overlay: View? = null

    override fun init() {
        if (!mPrefsMap.getBoolean(PREF)) {
            logD(TAG, "pref disabled, skip")
            return
        }
        hookSearchBar()
        hookSearchBarContainer()
    }

    private fun hookSearchBar() {
        val cls = findClassIfExists("com.miui.home.launcher.SearchBar")
        if (cls == null) {
            logD(TAG, "SearchBar class not found")
            return
        }
        cls.hookBeforeAllMethods("openSearch") { param ->
            logD(TAG, "SearchBar.openSearch intercepted")
            param.result = null
            val self = param.thisObject as? View ?: return@hookBeforeAllMethods
            showOverlay(self)
        }
    }

    private fun hookSearchBarContainer() {
        val cls = findClassIfExists("com.miui.home.launcher.SearchBarContainer")
        if (cls == null) {
            logD(TAG, "SearchBarContainer class not found")
            return
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

        val container = FrameLayout(ctx).apply {
            setBackgroundColor(0xE6000000.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true
        }

        val input = EditText(ctx).apply {
            hint = "Search apps, settings, contacts, files…"
            setTextColor(Color.WHITE)
            setHintTextColor(0x99FFFFFF.toInt())
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(48, 48, 48, 48)
            isSingleLine = true
            textSize = 18f
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP
                topMargin = 120
                marginStart = 40
                marginEnd = 40
            }
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    dismiss(); true
                } else false
            }
        }

        container.addView(input)
        root.addView(container)
        overlay = container
        input.requestFocus()
    }

    private fun dismiss() {
        val v = overlay ?: return
        (v.parent as? ViewGroup)?.removeView(v)
        overlay = null
    }
}
