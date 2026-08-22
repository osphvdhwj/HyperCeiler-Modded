package com.sevtinge.hyperceiler.hook.module.hook.weather;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentWeatherRemoveAds extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.weather2.model.AdInfo", "isAdValid", new XC_MethodHook() {
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
