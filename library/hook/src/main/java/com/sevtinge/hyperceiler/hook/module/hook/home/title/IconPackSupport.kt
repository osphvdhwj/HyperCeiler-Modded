package com.sevtinge.hyperceiler.hook.module.hook.home.title
import com.sevtinge.hyperceiler.hook.utils.hookAfterAllMethods
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import com.sevtinge.hyperceiler.hook.utils.getObjectField

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.AndroidAppHelper
import com.sevtinge.hyperceiler.hook.module.base.tool.HookTool.mPrefsMap
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logD

object IconPackSupport : BaseHook() {

    private const val TAG = "IconPackSupport"
    private const val PREF_PKG = "prefs_key_home_title_icon_pack_pkg"

    private var iconPackPkg: String? = null
    private var packResources: android.content.res.Resources? = null
    private var appFilter: MutableMap<String, String> = HashMap()

    override fun init() {
        iconPackPkg = mPrefsMap.getString(PREF_PKG, "")
        if (iconPackPkg.isNullOrEmpty()) {
            logD(TAG, "No icon pack selected")
            return
        }

        try {
            val ctx = android.app.de.robv.android.xposed.AndroidAppHelper.currentApplication()
            packResources = ctx.packageManager.getResourcesForApplication(iconPackPkg ?: return)
            Thread { loadAppFilter(ctx) }.start()
        } catch (t: Throwable) {
            logD(TAG, "Failed to load icon pack: ${t.message}")
            return
        }

        // Hook the MIUI Icon Cache to return our custom drawable
        val cacheClass = findClassIfExists("com.miui.home.launcher.LauncherIconCache") ?: return
        cacheClass.hookBeforeAllMethods("getIcon") { param ->
            val shortcutInfo = param.args[0] ?: return@hookBeforeAllMethods
            val componentName = runCatching {
                val intent = shortcutInfo.getObjectField("intent") as android.content.Intent
                intent.component
            }.getOrNull() ?: return@hookBeforeAllMethods

            val customIcon = getCustomIcon(componentName)
            if (customIcon != null) {
                param.result = customIcon
            }
        }
        logD(TAG, "Icon pack hooked: $iconPackPkg")
    }

    private fun getCustomIcon(component: ComponentName): Drawable? {
        val res = packResources ?: return null
        // 1. Try AppFilter (ComponentName -> Drawable Name)
        val drawableName = appFilter["${component.packageName}/${component.className}"]
            ?: appFilter["${component.packageName}/*"]

        if (drawableName != null) {
            val id = res.getIdentifier(drawableName, "drawable", iconPackPkg)
            if (id != 0) return runCatching { res.getDrawable(id, null) }.getOrNull()
        }

        // 2. Fallback to package name matching (e.g. com.whatsapp -> com.whatsapp.png)
        val fallbackId = res.getIdentifier(component.packageName, "drawable", iconPackPkg)
        if (fallbackId != 0) return runCatching { res.getDrawable(fallbackId, null) }.getOrNull()

        return null
    }

    private fun loadAppFilter(ctx: Context) {
        val res = packResources ?: return
        val id = res.getIdentifier("appfilter", "xml", iconPackPkg)
        if (id == 0) return

        runCatching {
            val parser = res.getXml(id)
            var eventType = parser.eventType
            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (component != null && drawable != null) {
                        appFilter[component] = drawable
                    }
                }
                eventType = parser.next()
            }
        }
    }
}
