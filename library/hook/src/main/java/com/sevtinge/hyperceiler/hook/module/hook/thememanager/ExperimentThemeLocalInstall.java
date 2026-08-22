package com.sevtinge.hyperceiler.hook.module.hook.thememanager;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentThemeLocalInstall extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.thememanager.basemodule.localtheme.model.LocalThemeUtils", 
                "isLocalThemeLegal", "android.content.Context", "java.lang.String", new MethodHook() {
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
