package com.sevtinge.hyperceiler.hook.module.hook.phone;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentPhoneNoRingTimeout extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.phone.CallManager", "getRingTimeout", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(-1); // Infinite or disabled
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
