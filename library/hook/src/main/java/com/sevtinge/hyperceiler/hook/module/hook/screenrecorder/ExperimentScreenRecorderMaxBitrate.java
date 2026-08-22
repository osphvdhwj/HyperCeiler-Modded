package com.sevtinge.hyperceiler.hook.module.hook.screenrecorder;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Remove the maximum bitrate cap in Screen Recorder (up to 80Mbps).
 */
public class ExperimentScreenRecorderMaxBitrate extends BaseHook {
    private static final int MAX_BITRATE = 80_000_000; // 80Mbps

    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.screenrecorder.model.ScreenRecordParams", "getBitRate",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    Object result = param.getResult();
                    if (result instanceof Integer && (Integer) result > 0) {
                        param.setResult(MAX_BITRATE);
                    }
                }
            });
        findAndHookMethodSilently("com.miui.screenrecorder.RecordSettings", "getMaxBitrate",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(MAX_BITRATE);
                }
            });
        findAndHookMethodSilently("com.miui.screenrecorder.RecordSettings", "isBitrateAllowed",
            int.class,
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
