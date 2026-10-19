package com.sevtinge.hyperceiler.hook.module.hook.home.folder

import android.view.View
import android.view.ViewGroup
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object FolderTransparency : BaseHook() {
    override fun init() {
        val alpha = mPrefsMap.getInt("prefs_key_home_folder_bg_alpha", 255)
        if (alpha == 255) return // Default is fully opaque, do nothing

        logD("FolderTransparency", "Applying Folder Alpha: $alpha")

        // Hook the closed FolderIcon and the open Folder view
        val classes = listOf(
            "com.miui.home.launcher.FolderIcon",
            "com.miui.home.launcher.Folder"
        )

        for (className in classes) {
            val cls = findClassIfExists(className) ?: continue
            cls.hookAfterAllMethods("onFinishInflate") { param ->
                val self = param.thisObject as? ViewGroup ?: return@hookAfterAllMethods
                adjustAlpha(self, alpha)
            }
        }
    }

    private fun adjustAlpha(view: ViewGroup, alpha: Int) {
        // MIUI names the background view differently depending on the class
        val bgNames = listOf("mBackgroundView", "mFolderBackground", "mBgView")
        
        for (name in bgNames) {
            try {
                val field = view.javaClass.getDeclaredField(name)
                field.isAccessible = true
                val bgView = field.get(view) as? View ?: continue
                // Reset alpha to default (255) first in case of view recycling, 
                // then apply the user's setting if it's less than 255
                bgView.background?.alpha = if (alpha >= 255) 255 else alpha
            } catch (t: Throwable) {
                // Field doesn't exist in this class, ignore
            }
        }
    }
}
