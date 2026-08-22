package com.sevtinge.hyperceiler.hook.module.hook.notes;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentNotesUnlockAllFonts extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.notes.font.FontManager", "isFontPremium", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(false);
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
