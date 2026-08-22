package com.sevtinge.hyperceiler.hook.module.hook.screenshot;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

/**
 * [Experiment] Remove the page limit in scrollshot (long screenshot) capture.
 * Allows capturing unlimited scroll height beyond the default cap.
 */
public class ExperimentScrollShotUnlimited extends BaseHook {
    @Override
    public void init() {
        // Hook 1: Override scrollshot page limit
        findAndHookMethod("com.miui.screenshot.LongScreenshotController",
            "getMaxPageCount", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = Integer.MAX_VALUE;
                }
            });
        // Hook 2: Override max scroll height in pixels
        findAndHookMethod("com.miui.screenshot.LongScreenshotController",
            "getMaxScrollHeight", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = Integer.MAX_VALUE;
                }
            });
        // Hook 3: Bypass bitmap size limit check
        findAndHookMethod("com.miui.screenshot.ScrollCaptureManager",
            "isScrollLimitReached", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = false;
                }
            });
    }
}
