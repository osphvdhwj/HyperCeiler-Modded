package com.sevtinge.hyperceiler.hook.module.hook.screenshot;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Remove the page limit in scrollshot (long screenshot) capture.
 */
public class ExperimentScrollShotUnlimited extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.screenshot.LongScreenshotController", "getMaxPageCount",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(Integer.MAX_VALUE);
                }
            });
        findAndHookMethodSilently("com.miui.screenshot.LongScreenshotController", "getMaxScrollHeight",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(Integer.MAX_VALUE);
                }
            });
        findAndHookMethodSilently("com.miui.screenshot.ScrollCaptureManager", "isScrollLimitReached",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });
    }
}
