package com.sevtinge.hyperceiler.hook.module.hook.camera;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

/**
 * [Experiment] Force HDR video recording mode.
 * Unlocks HDR10/HDR10+ video recording capability regardless of stock feature gate.
 */
public class ExperimentCameraVideoHdr extends BaseHook {
    @Override
    public void init() {
        // Hook 1: Force HDR video support flag
        findAndHookMethod("com.android.camera.CameraFeatureConfig", "isHDRVideoSupported",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 2: Unlock HDR profile availability
        findAndHookMethod("com.android.camera.device.DeviceCapabilityManager",
            "isSupportHDRVideo", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 3: Force HDR10 profile selection
        findAndHookMethod("com.android.camera.CameraCapabilities", "isVideoHDR10Supported",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
    }
}
