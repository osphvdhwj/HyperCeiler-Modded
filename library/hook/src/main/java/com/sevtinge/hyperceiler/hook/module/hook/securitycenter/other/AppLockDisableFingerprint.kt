package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.other

import android.app.Activity
import android.os.Bundle
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookAfterMethod
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods

object AppLockDisableFingerprint : BaseHook() {
    private var isTargetAppDisabled = false

    override fun init() {
        val confirmAccessControlClass = findClassIfExists("com.miui.applicationlock.ConfirmAccessControl") ?: return

        confirmAccessControlClass.hookAfterMethod("onCreate", Bundle::class.java) { param ->
            val activity = param.thisObject as Activity
            val intent = activity.intent
            val pkgName = intent.getStringExtra("android.intent.extra.PACKAGE_NAME") 
                          ?: intent.getStringExtra("package_name") 
                          ?: intent.getStringExtra("packageName")

            if (pkgName != null) {
                val disabledApps = mPrefsMap.getStringSet("security_center_disable_applock_fingerprint_apps", emptySet())
                isTargetAppDisabled = disabledApps.contains(pkgName)
            } else {
                isTargetAppDisabled = false
            }
        }

        val fingerprintHelperClass = findClassIfExists("com.miui.applicationlock.FingerprintHelperImpl") ?: return

        val cancelFingerprintHook: (de.robv.android.xposed.XC_MethodHook.MethodHookParam) -> Unit = { param ->
            if (isTargetAppDisabled) {
                param.result = false
            }
        }

        fingerprintHelperClass.hookBeforeAllMethods("hasEnrolledFingerprintsAppLock", callback = cancelFingerprintHook)
        fingerprintHelperClass.hookBeforeAllMethods("isHardwareDetectedAppLock", callback = cancelFingerprintHook)
    }
}
