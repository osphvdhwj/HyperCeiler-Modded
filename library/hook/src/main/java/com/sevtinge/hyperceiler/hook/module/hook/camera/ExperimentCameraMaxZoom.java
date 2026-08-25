package com.sevtinge.hyperceiler.hook.module.hook.camera;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Feature 2] Increase maximum digital zoom to 100x in Xiaomi Camera.
 *
 * Verified via dex analysis of /product/priv-app/MiuiCamera/MiuiCamera.apk:
 *   - Real method: getMaxZoomRatio() in DataCloudItemFeature / CameraCapabilities
 *   - Real method: getSupportedMaxZoomRatio()
 *   - Real method: isSupportExtendedMaxZoom() -> boolean
 *   - Real method: getExtendedMaxZoomCaps()
 *
 * Previously WRONG hooks: ZoomController.getMaxZoom, MiCameraCapability.getMaxZoomValue,
 *   MiCameraCapability.getSuperZoomMaxRatio, DataItemFeature.getSuperZoomMaxRatio (NONE exist)
 */
public class ExperimentCameraMaxZoom extends BaseHook {
    private static final float MAX_ZOOM = 100.0f;

    @Override
    public void init() {
        // Primary: DataCloudItemFeature.getMaxZoomRatio
        findAndHookMethodSilently(
            "com.android.camera.data.cloud.DataCloudItemFeature",
            "getMaxZoomRatio",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    Object result = param.getResult();
                    if (result instanceof Float && (Float) result < MAX_ZOOM) {
                        param.setResult(MAX_ZOOM);
                    }
                }
            }
        );

        // Secondary: getSupportedMaxZoomRatio
        findAndHookMethodSilently(
            "com.android.camera.data.cloud.DataCloudItemFeature",
            "getSupportedMaxZoomRatio",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    Object result = param.getResult();
                    if (result instanceof Float && (Float) result < MAX_ZOOM) {
                        param.setResult(MAX_ZOOM);
                    }
                }
            }
        );

        // Enable ExtendedMaxZoom support flag
        findAndHookMethodSilently(
            "com.android.camera.data.cloud.DataCloudItemFeature",
            "isSupportExtendedMaxZoom",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        // CameraSettings.getMaxZoomRatio (fallback location)
        findAndHookMethodSilently(
            "com.android.camera.CameraSettings",
            "getMaxZoomRatio",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    Object result = param.getResult();
                    if (result instanceof Float && (Float) result < MAX_ZOOM) {
                        param.setResult(MAX_ZOOM);
                    }
                }
            }
        );
    }
}
