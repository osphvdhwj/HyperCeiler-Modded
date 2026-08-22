package com.sevtinge.hyperceiler.hook.module.hook.camera;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Increase maximum digital zoom beyond stock limit (100x).
 */
public class ExperimentCameraMaxZoom extends BaseHook {
    private static final float MAX_ZOOM = 100.0f;

    @Override
    public void init() {
        findAndHookMethodSilently("com.android.camera.CameraCapabilities", "getMaxZoomRatio",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(MAX_ZOOM);
                }
            });
        findAndHookMethodSilently("com.android.camera.zoom.ZoomController", "getMaxZoom",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(MAX_ZOOM);
                }
            });
        findAndHookMethodSilently("com.android.camera.MiCameraCapability", "getMaxZoomValue",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    Object result = param.getResult();
                    if (result instanceof Float && (Float) result < MAX_ZOOM) {
                        param.setResult(MAX_ZOOM);
                    }
                }
            });
    }
}
