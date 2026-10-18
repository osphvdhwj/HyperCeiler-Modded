package com.sevtinge.hyperceiler.hook.module.hook.home.drawer

import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object AlphabetSidebar : BaseHook() {

    // Map of "A" -> 0, "B" -> 15, etc.
    private val letterPositions = HashMap<String, Int>()

    override fun init() {
        // 1. Hook the adapter to map letters to positions
        val alphabeticalList = findClassIfExists("com.miui.home.launcher.allapps.BaseAlphabeticalAppsList")
        alphabeticalList?.hookAfterAllMethods("getBucketLabel") { param ->
            val pos = param.args[0] as? Int ?: return@hookAfterAllMethods
            val label = param.result as? String ?: return@hookAfterAllMethods
            letterPositions[label.uppercase()] = pos
        }

        // 2. Inject sidebar
        val cls = findClassIfExists("com.miui.home.launcher.allapps.AllAppsContainerView") ?: return
        cls.hookAfterAllMethods("onFinishInflate") { param ->
            val self = param.thisObject as? ViewGroup ?: return@hookAfterAllMethods
            injectSidebar(self)
        }
    }

    private fun injectSidebar(container: ViewGroup) {
        if (container.findViewWithTag<View>("hc_alphabet_sidebar") != null) return
        val ctx = container.context
        val sidebar = LinearLayout(ctx).apply {
            tag = "hc_alphabet_sidebar"
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0x33000000)
            setPadding(8, 16, 8, 16)
            for (char in 'A'..'Z') {
                addView(TextView(ctx).apply {
                    text = char.toString()
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setPadding(4, 4, 4, 4)
                })
            }
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    val index = ((event.y / v.height) * 26).toInt().coerceIn(0, 25)
                    val letter = ('A' + index).toString()
                    val targetPos = letterPositions[letter] ?: return@setOnTouchListener true
                    
                    val rv = findRecyclerView(container)
                    rv?.scrollToPosition(targetPos)
                }
                true
            }
        }
        if (container is android.widget.FrameLayout) {
            container.addView(sidebar, android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
            ).apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL; rightMargin = 8 })
        }
    }

    private fun findRecyclerView(v: View): RecyclerView? {
        if (v is RecyclerView) return v
        if (v is ViewGroup) for (i in 0 until v.childCount) {
            val found = findRecyclerView(v.getChildAt(i))
            if (found != null) return found
        }
        return null
    }
}
