package com.sevtinge.hyperceiler.hook.module.hook.systemui

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

/**
 * [Experiment] Control Center blur intensity customization.
 *
 * Verified via dex analysis of /system_ext/priv-app/MiuiSystemUI/MiuiSystemUI.apk:
 *   - WRONG: com.android.systemui.statusbar.policy.BlurUtils (has extra .policy. subpackage)
 *   - REAL:  com.android.systemui.statusbar.BlurUtils ✅
 *   - ALSO:  com.android.systemui.statusbar.policy.BlurUtilsExt ✅ (extension class)
 *   - Method: blurRadiusOfRatio(Float) ✅ EXISTS in real class
 *   - Other real blur methods: applyBlurRatio, applyBlurInternal, blurRadius, blurRadiusDp
 */
object ExperimentControlCenterBlurIntensity : BaseHook() {
    override fun init() {
        val classLoader = lpparam?.classLoader ?: return
        val intensity = mPrefsMap.getFloat("experiment_control_center_blur_intensity", 1.0f)

        // Primary: hook real BlurUtils.blurRadiusOfRatio (verified in dex)
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.systemui.statusbar.BlurUtils",
                classLoader,
                "blurRadiusOfRatio",
                Float::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val ratio = param.args[0] as? Float ?: return
                        param.result = ratio * intensity
                    }
                }
            )
        }

        // Secondary: BlurUtilsExt wraps BlurUtils; hook its applyBlurRatio too (verified)
        runCatching {
            XposedHelpers.findAndHookMethod(
                "com.android.systemui.statusbar.policy.BlurUtilsExt",
                classLoader,
                "applyBlurRatio",
                Float::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val ratio = param.args[0] as? Float ?: return
                        param.args[0] = ratio * intensity
                    }
                }
            )
        }
    }
}
