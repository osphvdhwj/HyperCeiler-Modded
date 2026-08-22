package com.sevtinge.hyperceiler.hook.module.hook.powerkeeper;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentPowerKeeperNoThrottle extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.powerkeeper.statemachine.PowerStateMachine", "isThermalThrottlingEnabled", new XC_MethodHook() {
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
