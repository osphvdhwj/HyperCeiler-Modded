package com.sevtinge.hyperceiler.hook.module.hook.home.recent

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logI
import de.robv.android.xposed.XposedHelpers

/**
 * Diagnostics for the recents hold menu: logs every shortcut menu item
 * bind (item class, title, validity) and the layer's menu instance, so
 * the "add more buttons" feature can be built against verified data.
 */
object RecentsMenuTrace : BaseHook() {

    override fun init() {
        // The layer that shows the menu for a held recents task.
        runCatching {
            findClassIfExists("com.miui.home.launcher.ShortcutMenuLayer")?.let { cls ->
                XposedHelpers.findAndHookMethod(
                    cls, "getShortcutMenu",
                    object : MethodHook() {
                        override fun after(param: MethodHookParam) {
                            val menu = param.result ?: return
                            logI(TAG, "getShortcutMenu -> " + menu.javaClass.name)
                        }
                    }
                )
            }
        }

        // Every item row bound into a shortcut menu (icon menu AND recents
        // menu share this container).
        runCatching {
            findClassIfExists("com.miui.home.launcher.shortcuts.IconAndTitleShortcutMenuItemContainer")
                ?.let { cls ->
                    XposedHelpers.findAndHookMethod(
                        cls, "bindShortcutMenuItem",
                        object : MethodHook() {
                            override fun after(param: MethodHookParam) {
                                val item = param.args.firstOrNull() ?: return
                                val title = runCatching {
                                    XposedHelpers.callMethod(item, "getShortTitle")
                                }.getOrNull()
                                val valid = runCatching {
                                    XposedHelpers.callMethod(item, "isValid", *emptyArray())
                                }.getOrNull()
                                logI(
                                    TAG, "bindShortcutMenuItem: item=" +
                                    item.javaClass.simpleName + " title=" + title +
                                    " valid=" + valid
                                )
                            }
                        }
                    )
                }
        }
    }
}
