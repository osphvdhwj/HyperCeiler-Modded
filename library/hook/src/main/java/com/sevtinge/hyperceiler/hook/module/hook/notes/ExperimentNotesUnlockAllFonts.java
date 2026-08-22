package com.sevtinge.hyperceiler.hook.module.hook.notes;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentNotesUnlockAllFonts extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.notes.font.FontManager", "isFontPremium", new MethodHook() {
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
