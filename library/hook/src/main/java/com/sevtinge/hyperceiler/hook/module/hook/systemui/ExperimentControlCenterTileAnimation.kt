package com.sevtinge.hyperceiler.hook.module.hook.systemui

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object ExperimentControlCenterTileAnimation : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.systemui.qs.tileimpl.QSTileViewImpl",
                classLoader,
                "handleStateChanged",
                "com.android.systemui.plugins.qs.QSTile.State",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? android.view.View ?: return
                        view.animate().translationY(0f).setDuration(300).start()
                    }
                }
            )
        }
    }
}
