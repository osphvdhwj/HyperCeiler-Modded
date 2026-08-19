package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.other

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods

object DisableChargeSound : BaseHook() {
    override fun init() {
        val disableSound = mPrefsMap.getBoolean("system_ui_disable_charge_sound")
        val disableVibration = mPrefsMap.getBoolean("system_ui_disable_charge_vibration")
        
        if (!disableSound && !disableVibration) return
        
        val powerNoticeUIClass = findClassIfExists("com.miui.powercenter.provider.PowerNoticeUI")
            ?: findClassIfExists("com.miui.powercenter.battery.PowerNoticeUI")
            ?: findClassIfExists("com.miui.powercenter.PowerNoticeUI")
            ?: findClassIfExists("com.miui.securitycenter.power.PowerNoticeUI")
            ?: return

        if (disableSound) {
            powerNoticeUIClass.hookBeforeAllMethods("playBatterySound") { param ->
                param.result = null
            }
            powerNoticeUIClass.hookBeforeAllMethods("playPluggedSound") { param ->
                param.result = null
            }
        }

        if (disableVibration) {
            powerNoticeUIClass.hookBeforeAllMethods("playBatteryVibrate") { param ->
                param.result = null
            }
            powerNoticeUIClass.hookBeforeAllMethods("playPluggedVibrate") { param ->
                param.result = null
            }
            // Add a generic hook for playRing in case it uses that
            powerNoticeUIClass.hookBeforeAllMethods("playRing") { param ->
                param.result = null
            }
        }
    }
}
