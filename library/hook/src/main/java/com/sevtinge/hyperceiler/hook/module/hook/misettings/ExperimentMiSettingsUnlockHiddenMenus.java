package com.sevtinge.hyperceiler.hook.module.hook.misettings;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentMiSettingsUnlockHiddenMenus extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.xiaomi.misettings.usagestats.utils.AppUsageStatsFactory", "isDeveloperMode", new MethodHook() {
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
