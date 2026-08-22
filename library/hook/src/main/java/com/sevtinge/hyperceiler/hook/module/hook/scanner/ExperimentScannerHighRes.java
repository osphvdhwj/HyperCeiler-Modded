package com.sevtinge.hyperceiler.hook.module.hook.scanner;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentScannerHighRes extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.xiaomi.scanner.camera.CameraConfig", "getResolution", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(1); // high res
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
