package com.sevtinge.hyperceiler.hook.module.hook.mediaeditor;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

/**
 * [Experiment] Unlock all AI-based filters and visual styles in MediaEditor / Gallery Editor.
 * Forces all AI effect availability checks to return true regardless of device tier.
 */
public class ExperimentUnlockAllAiFilters extends BaseHook {
    @Override
    public void init() {
        // Hook 1: Unlock AI Filter availability in MediaEditor
        findAndHookMethod("com.miui.mediaeditor.api.AISupportItem",
            "isSupported", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 2: Unlock AI Style transfer
        findAndHookMethod("com.miui.mediaeditor.api.MediaEditorApiHelper",
            "isStyleTransferAvailable", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 3: Unlock AI portrait retouch
        findAndHookMethod("com.miui.mediaeditor.api.MediaEditorApiHelper",
            "isPortraitRetouchAvailable", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 4: Generic feature gate bypass
        findAndHookMethod("com.miui.mediaeditor.feature.FeatureManager",
            "isFeatureEnabled", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
    }
}
