package com.sevtinge.hyperceiler.hook.module.hook.powerkeeper;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentGameModeBoost extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.powerkeeper.gamemode.GameModeManager", "isPerformanceModeEnforced", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
