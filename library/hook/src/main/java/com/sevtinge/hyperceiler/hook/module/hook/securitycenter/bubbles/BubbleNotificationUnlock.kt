package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.bubbles

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

/**
 * Bubble (floating) notifications are gated by device/support checks in
 * the Security app — force the support and switch checks to true so the
 * feature can be enabled from its settings.
 */
object BubbleNotificationUnlock : BaseHook() {
    override fun init() {
        for (cls in listOf(
            "com.miui.bubbles.settings.BubblesSettings",
            "com.miui.bubbles.utils.BubbleUpManager",
        )) {
            for (method in listOf("isBubbleNotificationSupport", "isBubbleSwitchOpen")) {
                runCatching {
                    loadClass(cls).methodFinder()
                        .filterByName(method)
                        .single().createHook { returnConstant(true) }
                }
            }
        }
    }
}
