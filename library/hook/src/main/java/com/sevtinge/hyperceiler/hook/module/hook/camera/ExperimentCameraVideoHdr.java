package com.sevtinge.hyperceiler.hook.module.hook.camera;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Force HDR video recording mode (HDR10/HDR10+).
 */
public class ExperimentCameraVideoHdr extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.android.camera.CameraFeatureConfig", "isHDRVideoSupported",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.android.camera.device.DeviceCapabilityManager", "isSupportHDRVideo",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.android.camera.CameraCapabilities", "isVideoHDR10Supported",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
