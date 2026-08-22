package com.sevtinge.hyperceiler.hook.module.hook.browser;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentBrowserDesktopMode extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.browser.BrowserSettings", "getUserAgent", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(2); // desktop
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
