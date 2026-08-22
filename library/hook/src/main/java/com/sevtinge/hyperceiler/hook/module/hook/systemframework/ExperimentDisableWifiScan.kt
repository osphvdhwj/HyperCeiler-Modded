package com.sevtinge.hyperceiler.hook.module.hook.systemframework

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object ExperimentDisableWifiScan : BaseHook() {
    override fun init() {
        try {
            XposedHelpers.findAndHookMethod(
                "com.android.server.wifi.WifiServiceImpl",
                lpparam.classLoader,
                "startScan",
                String::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = false
                    }
                }
            )
        } catch (e: Throwable) {
        }
    }
}
