package com.sevtinge.hyperceiler.hook.module.hook.systemui

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object ExperimentLockscreenCustomClock : BaseHook() {
    override fun init() {
        try {
            XposedHelpers.findAndHookMethod(
                "com.android.keyguard.clock.KeyguardClockSwitch",
                lpparam.classLoader,
                "onFinishInflate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                    }
                }
            )
        } catch (e: Throwable) {
        }
    }
}
