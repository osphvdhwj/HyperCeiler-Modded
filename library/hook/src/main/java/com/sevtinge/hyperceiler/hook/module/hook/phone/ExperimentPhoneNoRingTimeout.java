package com.sevtinge.hyperceiler.hook.module.hook.phone;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentPhoneNoRingTimeout extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.phone.CallManager", "getRingTimeout", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(-1); // Infinite or disabled
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
