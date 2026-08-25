package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.other

import android.app.Activity
import android.os.Bundle
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookAfterMethod
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import de.robv.android.xposed.XposedHelpers

/**
 * [Feature 6] Disable Fingerprint authentication for selected App Lock apps in Security Center.
 *
 * Verified via dex analysis of /product/priv-app/SecurityCenter/SecurityCenter.apk:
 *   - Real class: com.miui.applicationlock.ConfirmAccessControl (confirmed with inner $a..$r)
 *   - Real field: mPackageName (confirmed from log "not allow start app lock, mPackageName:")
 *   - Real intent extra: "file_target_pkg" (NOT "mBehindAppLockPkg")
 *   - Real helper: FingerprintHelperImpl (confirmed from "mFingerprintHelper.hasEnrolledFingerprintsAppLock()")
 *   - Real methods: hasEnrolledFingerprintsAppLock, isHardwareDetectedAppLock, isHandleFingerprintDialog
 *
 * Previously WRONG: mBehindAppLockPkg, mTargetPkg, target_pkg (field names that don't exist)
 */
object AppLockDisableFingerprint : BaseHook() {
    private var currentPkgName: String? = null

    private fun getDisabledFingerprintApps(): Set<String> {
        val apps = mutableSetOf<String>()
        runCatching {
            apps.addAll(mPrefsMap.getStringSet("security_center_disable_applock_fingerprint_apps"))
            apps.addAll(mPrefsMap.getStringSet("prefs_key_security_center_disable_applock_fingerprint_apps"))
        }
        return apps
    }

    /**
     * Extract the locked app's package name from ConfirmAccessControl activity.
     * Uses VERIFIED field "mPackageName" and intent extra "file_target_pkg".
     */
    private fun extractPkg(thisObj: Any?): String? {
        // Try intent extra first (verified: "file_target_pkg")
        if (thisObj is Activity) {
            val intent = thisObj.intent
            val pkgFromIntent = intent?.getStringExtra("file_target_pkg")
                ?: intent?.getStringExtra("extra_pkg")
                ?: intent?.getStringExtra("EXTRA_PACKAGE_NAME")
                ?: intent?.getStringExtra("android.intent.extra.PACKAGE_NAME")
            if (!pkgFromIntent.isNullOrEmpty()) {
                currentPkgName = pkgFromIntent
                return pkgFromIntent
            }
        }

        // Try field access (verified: "mPackageName")
        val pkgFromField = runCatching {
            if (thisObj != null) {
                XposedHelpers.getObjectField(thisObj, "mPackageName") as? String
            } else null
        }.getOrNull()

        if (!pkgFromField.isNullOrEmpty()) {
            currentPkgName = pkgFromField
            return pkgFromField
        }
        return currentPkgName
    }

    private fun shouldDisableFingerprint(thisObj: Any?): Boolean {
        val target = extractPkg(thisObj) ?: return false
        val disabledApps = getDisabledFingerprintApps()
        return disabledApps.any { it.equals(target, ignoreCase = true) }
    }

    override fun init() {
        val confirmClass = findClassIfExists("com.miui.applicationlock.ConfirmAccessControl")
        confirmClass?.let { clazz ->
            // Capture package name on activity creation
            runCatching {
                clazz.hookAfterMethod("onCreate", Bundle::class.java) { param ->
                    extractPkg(param.thisObject)
                }
            }
            runCatching {
                clazz.hookAfterMethod("onResume") { param ->
                    extractPkg(param.thisObject)
                }
            }

            // Hook fingerprint dialog handling (verified method)
            runCatching {
                clazz.hookBeforeAllMethods("isHandleFingerprintDialog") { param ->
                    if (shouldDisableFingerprint(param.thisObject)) {
                        param.result = false
                    }
                }
            }
        }

        // Hook FingerprintHelperImpl (verified from dex strings)
        val fpHelper = findClassIfExists("com.miui.applicationlock.FingerprintHelperImpl")
            ?: return

        // hasEnrolledFingerprintsAppLock (verified)
        runCatching {
            fpHelper.hookBeforeAllMethods("hasEnrolledFingerprintsAppLock") { param ->
                if (shouldDisableFingerprint(param.thisObject)) {
                    param.result = false
                }
            }
        }

        // isHardwareDetectedAppLock (verified)
        runCatching {
            fpHelper.hookBeforeAllMethods("isHardwareDetectedAppLock") { param ->
                if (shouldDisableFingerprint(param.thisObject)) {
                    param.result = false
                }
            }
        }
    }
}
