package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.network

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

/**
 * The network assistant gates the lock-screen traffic monitor behind
 * config checks that are off on many ROMs — force both gates open.
 */
object LockScreenTraffic : BaseHook() {
    override fun init() {
        runCatching {
            loadClass("com.miui.networkassistant.config.CommonConfig").methodFinder()
                .filterByName("isLockScreenTrafficMonitorEnable")
                .single().createHook { returnConstant(true) }
        }
        runCatching {
            loadClass("com.miui.networkassistant.config.SimUserInfo").methodFinder()
                .filterByName("isLockScreenTrafficEnable")
                .single().createHook { returnConstant(true) }
        }
    }
}
