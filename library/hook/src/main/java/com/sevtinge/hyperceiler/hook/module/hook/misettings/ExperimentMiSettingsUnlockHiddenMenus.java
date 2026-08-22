package com.sevtinge.hyperceiler.hook.module.hook.misettings;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentMiSettingsUnlockHiddenMenus extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.xiaomi.misettings.usagestats.utils.AppUsageStatsFactory", "isDeveloperMode", new XC_MethodHook() {
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
