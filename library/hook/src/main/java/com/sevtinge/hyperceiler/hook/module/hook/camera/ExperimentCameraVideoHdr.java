package com.sevtinge.hyperceiler.hook.module.hook.camera;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Feature 3] Force Video HDR / HDR10 / HDR10+ recording in Xiaomi Camera.
 *
 * Verified via dex analysis of /product/priv-app/MiuiCamera/MiuiCamera.apk:
 *   - Real methods in DataCloudItemFeature:
 *     isSupportVideoHdr() -> boolean
 *     isSupportHdr10Video() -> boolean
 *     isSupportedHdr10() -> boolean
 *     isSupportedHdr10Plus() -> boolean
 *     isSupportQcomVideoHdr() -> boolean
 *
 * Previously WRONG hooks: isHDRVideoSupported, isSupportHDRVideo, isVideoHDR10Supported,
 *   isHDR10VideoSupported, isVideoHDR10PlusSupported, isSupportVideoHdr10,
 *   isSupportVideoHdr10Plus (method names were wrong casing/format)
 */
public class ExperimentCameraVideoHdr extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently(
            "com.android.camera.data.cloud.DataCloudItemFeature",
            "isSupportVideoHdr",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        findAndHookMethodSilently(
            "com.android.camera.data.cloud.DataCloudItemFeature",
            "isSupportHdr10Video",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        findAndHookMethodSilently(
            "com.android.camera.data.cloud.DataCloudItemFeature",
            "isSupportedHdr10",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        findAndHookMethodSilently(
            "com.android.camera.data.cloud.DataCloudItemFeature",
            "isSupportedHdr10Plus",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        findAndHookMethodSilently(
            "com.android.camera.data.cloud.DataCloudItemFeature",
            "isSupportQcomVideoHdr",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );
    }
}
