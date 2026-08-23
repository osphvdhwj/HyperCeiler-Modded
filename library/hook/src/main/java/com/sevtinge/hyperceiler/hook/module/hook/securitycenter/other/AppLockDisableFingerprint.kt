package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.other

import android.app.Activity
import android.os.Bundle
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.hookAfterMethod
import com.sevtinge.hyperceiler.hook.utils.hookBeforeAllMethods
import de.robv.android.xposed.XposedHelpers

/**
 * [Security App] Disable Fingerprint authentication for selected App Lock apps.
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

    override fun init() {
        val confirmAccessControlClass = findClassIfExists("com.miui.applicationlock.ConfirmAccessControl")
        confirmAccessControlClass?.let { clazz ->
            runCatching {
                clazz.hookAfterMethod("onCreate", Bundle::class.java) { param ->
                    val activity = param.thisObject as? Activity ?: return@hookAfterMethod
                    val intent = activity.intent
                    val pkgFromIntent = intent?.getStringExtra("mBehindAppLockPkg")
                                  ?: intent?.getStringExtra("android.intent.extra.PACKAGE_NAME") 
                                  ?: intent?.getStringExtra("package_name") 
                                  ?: intent?.getStringExtra("packageName")
                                  ?: intent?.getStringExtra("mTargetPkg")
                                  ?: intent?.getStringExtra("target_pkg")
                    
                    val pkgFromField = runCatching {
                        XposedHelpers.getObjectField(activity, "mBehindAppLockPkg") as? String
                            ?: XposedHelpers.getObjectField(activity, "mPackageName") as? String
                            ?: XposedHelpers.getObjectField(activity, "mTargetPkg") as? String
                    }.getOrNull()

                    currentPkgName = pkgFromIntent ?: pkgFromField
                }
            }
            runCatching {
                clazz.hookAfterMethod("onResume") { param ->
                    val activity = param.thisObject as? Activity ?: return@hookAfterMethod
                    val intent = activity.intent
                    val pkgFromIntent = intent?.getStringExtra("mBehindAppLockPkg")
                                  ?: intent?.getStringExtra("android.intent.extra.PACKAGE_NAME")
                                  ?: intent?.getStringExtra("package_name")
                                  ?: intent?.getStringExtra("packageName")

                    val pkgFromField = runCatching {
                        XposedHelpers.getObjectField(activity, "mBehindAppLockPkg") as? String
                            ?: XposedHelpers.getObjectField(activity, "mPackageName") as? String
                            ?: XposedHelpers.getObjectField(activity, "mTargetPkg") as? String
                    }.getOrNull()

                    val target = pkgFromIntent ?: pkgFromField
                    if (target != null) {
                        currentPkgName = target
                    }
                }
            }

            // Hook ConfirmAccessControl fingerprint availability methods directly
            runCatching {
                clazz.hookBeforeAllMethods("isFingerprintEnable") { param ->
                    val disabledApps = getDisabledFingerprintApps()
                    val target = currentPkgName
                    if (target != null && disabledApps.any { it.equals(target, ignoreCase = true) }) {
                        param.result = false
                    }
                }
            }
            runCatching {
                clazz.hookBeforeAllMethods("shouldUseFingerprint") { param ->
                    val disabledApps = getDisabledFingerprintApps()
                    val target = currentPkgName
                    if (target != null && disabledApps.any { it.equals(target, ignoreCase = true) }) {
                        param.result = false
                    }
                }
            }
        }

        val fingerprintHelperClass = findClassIfExists("com.miui.applicationlock.FingerprintHelperImpl") 
            ?: findClassIfExists("com.miui.applicationlock.FingerprintHelper") 
            ?: return

        val cancelFingerprintHook: (de.robv.android.xposed.XC_MethodHook.MethodHookParam) -> Unit = { param ->
            val disabledApps = getDisabledFingerprintApps()
            val target = currentPkgName
            if (target != null && disabledApps.any { it.equals(target, ignoreCase = true) }) {
                param.result = false
            }
        }

        runCatching { fingerprintHelperClass.hookBeforeAllMethods("hasEnrolledFingerprintsAppLock", hooker = cancelFingerprintHook) }
        runCatching { fingerprintHelperClass.hookBeforeAllMethods("isHardwareDetectedAppLock", hooker = cancelFingerprintHook) }
        runCatching { fingerprintHelperClass.hookBeforeAllMethods("isFingerprintEnable", hooker = cancelFingerprintHook) }
        runCatching { fingerprintHelperClass.hookBeforeAllMethods("isFingerprintUnlockEnable", hooker = cancelFingerprintHook) }
        runCatching { fingerprintHelperClass.hookBeforeAllMethods("startAuthenticate", hooker = hooker@{ param ->
            val disabledApps = getDisabledFingerprintApps()
            val target = currentPkgName
            if (target != null && disabledApps.any { it.equals(target, ignoreCase = true) }) {
                param.result = null
            }
        }) }
        runCatching { fingerprintHelperClass.hookBeforeAllMethods("authenticate", hooker = hooker@{ param ->
            val disabledApps = getDisabledFingerprintApps()
            val target = currentPkgName
            if (target != null && disabledApps.any { it.equals(target, ignoreCase = true) }) {
                param.result = null
            }
        }) }
    }
}
