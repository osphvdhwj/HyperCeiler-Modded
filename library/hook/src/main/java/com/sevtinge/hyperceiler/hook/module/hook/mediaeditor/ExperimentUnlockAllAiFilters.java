package com.sevtinge.hyperceiler.hook.module.hook.mediaeditor;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Feature 4/24] Unlock all AI filters, style transfers, and portrait retouching modes in MediaEditor.
 */
public class ExperimentUnlockAllAiFilters extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.mediaeditor.api.AISupportItem", "isSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.mediaeditor.api.MediaEditorApiHelper", "isStyleTransferAvailable", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.mediaeditor.api.MediaEditorApiHelper", "isPortraitRetouchAvailable", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.mediaeditor.api.MediaEditorApiHelper", "isAiFilterAvailable", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.mediaeditor.feature.FeatureManager", "isFeatureEnabled", String.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.gallery.editor.photo.core.imports.filter.FilterManager", "isFilterSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
    }
}
