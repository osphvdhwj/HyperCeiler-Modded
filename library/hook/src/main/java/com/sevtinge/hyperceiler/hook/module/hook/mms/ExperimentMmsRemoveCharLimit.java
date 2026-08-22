package com.sevtinge.hyperceiler.hook.module.hook.mms;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentMmsRemoveCharLimit extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.mms.ui.MessageUtils", "getSmsToMmsTextThreshold", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(9999);
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
