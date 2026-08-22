package com.sevtinge.hyperceiler.hook.module.hook.powerkeeper;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentGameModeBoost extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.powerkeeper.gamemode.GameModeManager", "isPerformanceModeEnforced", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
