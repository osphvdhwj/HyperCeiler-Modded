package com.sevtinge.hyperceiler.hook.module.hook.browser;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentBrowserDesktopMode extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.browser.BrowserSettings", "getUserAgent", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(2); // desktop
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
