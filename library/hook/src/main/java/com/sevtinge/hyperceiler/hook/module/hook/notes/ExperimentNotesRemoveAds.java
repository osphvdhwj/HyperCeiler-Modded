package com.sevtinge.hyperceiler.hook.module.hook.notes;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentNotesRemoveAds extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.notes.ui.AdManager", "shouldShowAd", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
