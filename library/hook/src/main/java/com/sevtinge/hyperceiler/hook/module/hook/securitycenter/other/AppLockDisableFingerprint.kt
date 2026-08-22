package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.other

import android.app.Activity
import android.os.Bundle
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookAfterMethod
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods

object AppLockDisableFingerprint : BaseHook() {
    private var currentPkgName: String? = null

    override fun init() {
        val confirmAccessControlClass = findClassIfExists("com.miui.applicationlock.ConfirmAccessControl")
        confirmAccessControlClass?.hookAfterMethod("onCreate", Bundle::class.java) { param ->
            val activity = param.thisObject as Activity
            val intent = activity.intent
            currentPkgName = intent.getStringExtra("android.intent.extra.PACKAGE_NAME") 
                          ?: intent.getStringExtra("package_name") 
                          ?: intent.getStringExtra("packageName")
        }

        val fingerprintHelperClass = findClassIfExists("com.miui.applicationlock.FingerprintHelperImpl") ?: return

        val cancelFingerprintHook: (de.robv.android.xposed.XC_MethodHook.MethodHookParam) -> Unit = { param ->
            val disabledApps = mPrefsMap.getStringSet("security_center_disable_applock_fingerprint_apps")
            val target = currentPkgName
            if (target != null && disabledApps.contains(target)) {
                param.result = false
            }
        }

        fingerprintHelperClass.hookBeforeAllMethods("hasEnrolledFingerprintsAppLock", hooker = cancelFingerprintHook)
        fingerprintHelperClass.hookBeforeAllMethods("isHardwareDetectedAppLock", hooker = cancelFingerprintHook)
    }
}
