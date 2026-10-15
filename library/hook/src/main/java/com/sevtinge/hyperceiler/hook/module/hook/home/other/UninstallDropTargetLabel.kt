package com.sevtinge.hyperceiler.hook.module.hook.home.other

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object UninstallDropTargetLabel : BaseHook() {

    private const val TAG = "UninstallDropTargetLabel"

    override fun init() {
        val cls = findClassIfExists("com.miui.home.launcher.UninstallDropTarget") ?: run {
            logD(TAG, "UninstallDropTarget not found"); return
        }

        cls.hookAfterMethod("changeIconAndLabel") { param ->
            val self = param.thisObject as? ViewGroup ?: return@hookAfterMethod
            val modeName = runCatching {
                val f = self.javaClass.getDeclaredField("mUninstallDropTargetMode")
                f.isAccessible = true
                (f.get(self))?.javaClass?.simpleName ?: ""
            }.getOrDefault("")

            val label = when (modeName) {
                "Uninstall" -> "Uninstall"
                "Delete" -> "Remove"
                else -> "Remove"
            }

            forceLabel(self, label)
            logD(TAG, "label forced: '$label' (mode=$modeName)")
        }

        logD(TAG, "hook installed")
    }

    private fun forceLabel(v: View, text: String) {
        when (v) {
            is TextView -> {
                if (v.text.isNullOrBlank()) v.text = text
                if (v.visibility != View.VISIBLE) v.visibility = View.VISIBLE
            }
            is ImageView -> {
                if (v.visibility != View.VISIBLE) v.visibility = View.VISIBLE
            }
            is ViewGroup -> for (i in 0 until v.childCount) forceLabel(v.getChildAt(i), text)
        }
    }
}
