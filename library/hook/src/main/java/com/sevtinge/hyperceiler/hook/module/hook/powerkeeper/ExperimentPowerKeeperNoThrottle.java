package com.sevtinge.hyperceiler.hook.module.hook.powerkeeper;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentPowerKeeperNoThrottle extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.powerkeeper.statemachine.PowerStateMachine", "isThermalThrottlingEnabled", new MethodHook() {
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
