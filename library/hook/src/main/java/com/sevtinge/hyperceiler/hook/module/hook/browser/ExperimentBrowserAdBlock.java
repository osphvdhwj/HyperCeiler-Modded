package com.sevtinge.hyperceiler.hook.module.hook.browser;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentBrowserAdBlock extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.browser.AdBlock", "isBlocked", "java.lang.String", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String url = (String) param.args[0];
                    if (url != null && (url.contains("ad") || url.contains("tracker"))) {
                        param.setResult(true);
                    }
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
