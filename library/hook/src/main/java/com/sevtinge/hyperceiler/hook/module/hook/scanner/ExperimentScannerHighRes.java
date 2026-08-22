package com.sevtinge.hyperceiler.hook.module.hook.scanner;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentScannerHighRes extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.xiaomi.scanner.camera.CameraConfig", "getResolution", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(1); // high res
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
