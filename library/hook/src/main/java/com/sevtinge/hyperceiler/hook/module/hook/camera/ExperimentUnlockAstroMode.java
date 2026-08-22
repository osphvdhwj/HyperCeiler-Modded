package com.sevtinge.hyperceiler.hook.module.hook.camera;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Unlock Astrophotography / Night Sky mode on non-flagship devices.
 */
public class ExperimentUnlockAstroMode extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.android.camera.CameraFeatureConfig", "isAstroSupported",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.android.camera.device.DeviceCapabilityManager", "isSupportAstroMode",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.android.camera.MiCameraFeature", "isAstroPhotoSupported",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
