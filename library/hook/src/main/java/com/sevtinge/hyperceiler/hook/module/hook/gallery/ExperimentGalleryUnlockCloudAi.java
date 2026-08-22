package com.sevtinge.hyperceiler.hook.module.hook.gallery;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Force-enable all cloud AI features in Gallery.
 */
public class ExperimentGalleryUnlockCloudAi extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.gallery.cloud.ai.AICapabilityHelper", "isAICapabilitySupported",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.miui.gallery.feature.FeatureConfig", "isSmartAlbumEnabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.miui.gallery.search.SearchCapabilityManager", "isAISearchEnabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.miui.gallery.util.FeatureUtil", "isAiFeatureEnabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
