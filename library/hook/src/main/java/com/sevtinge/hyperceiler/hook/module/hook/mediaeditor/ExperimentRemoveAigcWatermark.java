package com.sevtinge.hyperceiler.hook.module.hook.mediaeditor;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Remove AIGC (AI-Generated Content) watermark from exported media.
 */
public class ExperimentRemoveAigcWatermark extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.mediaeditor.api.MediaEditorApiHelper", "shouldAddAIGCWatermark",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });
        findAndHookMethodSilently("com.miui.mediaeditor.export.ExportManager", "addAigcLabel",
            new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            });
        findAndHookMethodSilently("com.miui.mediaeditor.ui.widget.AigcWatermarkView", "setVisibility",
            int.class,
            new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.args[0] = 8; // GONE
                }
            });
    }
}
