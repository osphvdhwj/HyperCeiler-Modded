package com.sevtinge.hyperceiler.hook.module.hook.thememanager;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentThemeUnlockPremium extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.thememanager.basemodule.resource.model.Resource", 
                "isPremium", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(false);
                }
            });
            
            findAndHookMethod("com.android.thememanager.basemodule.resource.model.Resource", 
                "isBuy", new XC_MethodHook() {
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
