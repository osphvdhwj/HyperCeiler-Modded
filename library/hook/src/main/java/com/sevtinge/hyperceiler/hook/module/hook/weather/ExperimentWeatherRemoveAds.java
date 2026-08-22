package com.sevtinge.hyperceiler.hook.module.hook.weather;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentWeatherRemoveAds extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.weather2.model.AdInfo", "isAdValid", new MethodHook() {
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
