package com.sevtinge.hyperceiler.hook.module.hook.mediaeditor;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Unlock all AI-based filters and visual styles in MediaEditor.
 */
public class ExperimentUnlockAllAiFilters extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.mediaeditor.api.AISupportItem", "isSupported",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.miui.mediaeditor.api.MediaEditorApiHelper", "isStyleTransferAvailable",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.miui.mediaeditor.api.MediaEditorApiHelper", "isPortraitRetouchAvailable",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        findAndHookMethodSilently("com.miui.mediaeditor.feature.FeatureManager", "isFeatureEnabled",
            String.class,
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
