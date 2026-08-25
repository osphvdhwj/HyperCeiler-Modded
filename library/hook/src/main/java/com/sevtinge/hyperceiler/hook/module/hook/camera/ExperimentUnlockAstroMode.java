package com.sevtinge.hyperceiler.hook.module.hook.camera;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Feature 1] Unlock SuperMoon mode in Xiaomi Camera.
 *
 * Verified via dex analysis of /product/priv-app/MiuiCamera/MiuiCamera.apk:
 *   - Real class: com.android.camera.data.cloud.DataCloudItemFeature
 *   - Real method: isSupportSuperMoonMode() -> boolean
 *   - Real class: com.android.camera.module.SuperMoonModule (exists, gated by above)
 *   - Real class: com.mi.device.ConfigConstant$SuperMoonSupportType (enum)
 *
 * Previously WRONG hooks: isAstroSupported, isSupportAstroMode, isAstroPhotoSupported,
 *   isSupportAstro, isSupportSuperMoon, isSupportNightSky (NONE of these exist in APK)
 */
public class ExperimentUnlockAstroMode extends BaseHook {
    @Override
    public void init() {
        // The ONLY real method that gates SuperMoon mode in this Camera build
        findAndHookMethodSilently(
            "com.android.camera.data.cloud.DataCloudItemFeature",
            "isSupportSuperMoonMode",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );
    }
}
