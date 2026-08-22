package com.sevtinge.hyperceiler.hook.module.hook.screenrecorder;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

/**
 * [Experiment] Remove the maximum bitrate cap in Screen Recorder.
 * Allows recording at full quality beyond the stock 20Mbps limit.
 */
public class ExperimentScreenRecorderMaxBitrate extends BaseHook {
    private static final int MAX_BITRATE = 80_000_000; // 80Mbps

    @Override
    public void init() {
        // Hook 1: Override bitrate limit in ScreenRecordParams
        findAndHookMethod("com.miui.screenrecorder.model.ScreenRecordParams",
            "getBitRate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object result = param.getResult();
                    if (result instanceof Integer && (Integer) result > 0) {
                        param.result = MAX_BITRATE;
                    }
                }
            });
        // Hook 2: Override max bitrate cap check
        findAndHookMethod("com.miui.screenrecorder.RecordSettings",
            "getMaxBitrate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = MAX_BITRATE;
                }
            });
        // Hook 3: Bypass quality selector cap
        findAndHookMethod("com.miui.screenrecorder.RecordSettings",
            "isBitrateAllowed", int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
    }
}
