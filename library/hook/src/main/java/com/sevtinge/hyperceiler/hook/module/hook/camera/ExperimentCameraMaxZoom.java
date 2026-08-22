package com.sevtinge.hyperceiler.hook.module.hook.camera;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

/**
 * [Experiment] Increase maximum digital zoom beyond stock limit.
 * Forces the maximum zoom ratio to 100x regardless of device model.
 */
public class ExperimentCameraMaxZoom extends BaseHook {
    private static final float MAX_ZOOM = 100.0f;

    @Override
    public void init() {
        // Hook 1: Override getMaxZoomRatio
        findAndHookMethod("com.android.camera.CameraCapabilities", "getMaxZoomRatio",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = MAX_ZOOM;
                }
            });
        // Hook 2: Override zoom ratio clamp in ZoomController
        findAndHookMethod("com.android.camera.zoom.ZoomController", "getMaxZoom",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = MAX_ZOOM;
                }
            });
        // Hook 3: MiCamera zoom limit
        findAndHookMethod("com.android.camera.MiCameraCapability", "getMaxZoomValue",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object result = param.getResult();
                    if (result instanceof Float && (Float) result < MAX_ZOOM) {
                        param.result = MAX_ZOOM;
                    }
                }
            });
    }
}
