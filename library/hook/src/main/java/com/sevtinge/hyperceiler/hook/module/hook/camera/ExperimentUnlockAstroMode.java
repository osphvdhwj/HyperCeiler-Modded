package com.sevtinge.hyperceiler.hook.module.hook.camera;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

/**
 * [Experiment] Unlock Astrophotography / Night Sky mode on non-flagship devices.
 * Hooks the camera feature availability check and forces astrophoto to appear enabled.
 */
public class ExperimentUnlockAstroMode extends BaseHook {
    @Override
    public void init() {
        // Hook 1: Force-enable astrophotography feature flag
        findAndHookMethod("com.android.camera.CameraFeatureConfig", "isAstroSupported",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 2: Bypass device whitelist check for night/astro mode
        findAndHookMethod("com.android.camera.device.DeviceCapabilityManager",
            "isSupportAstroMode", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 3: MiCamera variant
        findAndHookMethod("com.android.camera.MiCameraFeature", "isAstroPhotoSupported",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
    }
}
