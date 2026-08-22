package com.sevtinge.hyperceiler.hook.module.hook.gallery;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

/**
 * [Experiment] Force-enable all cloud AI features in Gallery (Smart Album, AI Search, etc.)
 * Bypasses region/device locks for AI Gallery capabilities.
 */
public class ExperimentGalleryUnlockCloudAi extends BaseHook {
    @Override
    public void init() {
        // Hook 1: Cloud AI feature gate
        findAndHookMethod("com.miui.gallery.cloud.ai.AICapabilityHelper",
            "isAICapabilitySupported", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 2: Smart album AI classification
        findAndHookMethod("com.miui.gallery.feature.FeatureConfig",
            "isSmartAlbumEnabled", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 3: AI search capability unlock
        findAndHookMethod("com.miui.gallery.search.SearchCapabilityManager",
            "isAISearchEnabled", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 4: Gallery AI features region lock bypass
        findAndHookMethod("com.miui.gallery.util.FeatureUtil",
            "isAiFeatureEnabled", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
    }
}
