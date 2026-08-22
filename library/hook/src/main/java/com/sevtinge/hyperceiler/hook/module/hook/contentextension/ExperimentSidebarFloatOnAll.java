package com.sevtinge.hyperceiler.hook.module.hook.contentextension;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentSidebarFloatOnAll extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.contentextension.sidebar.SidebarManager", "isAppWhitelisted", "java.lang.String", new XC_MethodHook() {
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
