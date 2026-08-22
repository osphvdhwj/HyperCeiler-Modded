package com.sevtinge.hyperceiler.hook.module.hook.mediaeditor;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

/**
 * [Experiment] Remove AIGC (AI-Generated Content) watermark from exported media.
 * Hooks the watermark insertion logic in MediaEditor to suppress AIGC tags.
 */
public class ExperimentRemoveAigcWatermark extends BaseHook {
    @Override
    public void init() {
        // Hook 1: Suppress AIGC watermark flag
        findAndHookMethod("com.miui.mediaeditor.api.MediaEditorApiHelper",
            "shouldAddAIGCWatermark", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = false;
                }
            });
        // Hook 2: Override AIGC label insertion
        findAndHookMethod("com.miui.mediaeditor.export.ExportManager",
            "addAigcLabel", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(null); // skip watermark injection
                }
            });
        // Hook 3: Suppress watermark visibility in preview
        findAndHookMethod("com.miui.mediaeditor.ui.widget.AigcWatermarkView",
            "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = 8; // GONE
                }
            });
    }
}
