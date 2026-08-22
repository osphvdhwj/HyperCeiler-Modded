package com.sevtinge.hyperceiler.hook.module.hook.browser;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentBrowserNightMode extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.browser.BrowserSettings", "isNightMode", new MethodHook() {
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
