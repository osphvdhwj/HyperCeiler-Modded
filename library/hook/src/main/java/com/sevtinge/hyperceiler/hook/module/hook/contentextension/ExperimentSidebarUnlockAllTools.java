package com.sevtinge.hyperceiler.hook.module.hook.contentextension;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentSidebarUnlockAllTools extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.miui.contentextension.sidebar.SidebarManager", "isToolSupported", "java.lang.String", new MethodHook() {
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
