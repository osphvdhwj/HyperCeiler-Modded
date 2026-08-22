package com.sevtinge.hyperceiler.hook.module.hook.mms;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentMmsRemoveCharLimit extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.mms.ui.MessageUtils", "getSmsToMmsTextThreshold", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(9999);
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
