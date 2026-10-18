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
    override fun init() {
        val cls = findClassIfExists("com.miui.home.launcher.allapps.AllAppsContainerView") ?: run {
            logD("AlphabetSidebar", "AllAppsContainerView not found")
            return
        }
        
        cls.hookAfterAllMethods("onFinishInflate") { param ->
            val self = param.thisObject as? ViewGroup ?: return@hookAfterAllMethods
            try {
                injectSidebar(self)
            } catch (t: Throwable) {
                logD("AlphabetSidebar", "Inject failed: ${t.message}")
            }
        }
    }

    private fun injectSidebar(container: ViewGroup) {
        if (container.findViewWithTag<View>("hc_alphabet_sidebar") != null) return

        val ctx = container.context
        val sidebar = LinearLayout(ctx).apply {
            tag = "hc_alphabet_sidebar"
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0x33000000) // Semi-transparent black
            setPadding(8, 16, 8, 16)
            
            for (char in 'A'..'Z') {
                val tv = TextView(ctx).apply {
                    text = char.toString()
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setPadding(4, 4, 4, 4)
                }
                addView(tv)
            }
            
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    val y = event.y
                    val heightPerLetter = v.height / 26f
                    val index = (y / heightPerLetter).toInt().coerceIn(0, 25)
                    
                    val rv = findRecyclerView(container)
                    if (rv != null && rv.adapter != null) {
                        val count = rv.adapter!!.itemCount
                        if (count > 0) {
                            // Approximate scroll position
                            val targetPos = (index / 26f * count).toInt()
                            rv.scrollToPosition(targetPos)
                        }
                    }
                }
                true
            }
        }
        
        // Add to container. AllAppsContainerView is usually a FrameLayout.
        if (container is android.widget.FrameLayout) {
            val flParams = android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                rightMargin = 8
            }
            container.addView(sidebar, flParams)
        } else {
            logD("AlphabetSidebar", "Container is not FrameLayout, skipping")
        }
    }

    private fun findRecyclerView(v: View): RecyclerView? {
        if (v is RecyclerView) return v
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                val found = findRecyclerView(v.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }
}
