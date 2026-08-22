package com.sevtinge.hyperceiler.hook.module.hook.thememanager;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentThemeUnlockPremium extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.thememanager.basemodule.resource.model.Resource", 
                "isPremium", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            });
            
            findAndHookMethod("com.android.thememanager.basemodule.resource.model.Resource", 
                "isBuy", new MethodHook() {
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
