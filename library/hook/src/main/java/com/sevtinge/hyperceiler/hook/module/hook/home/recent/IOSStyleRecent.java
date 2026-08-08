package com.sevtinge.hyperceiler.hook.module.hook.home.recent;

import android.content.ContentResolver;
import android.provider.Settings;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class IOSStyleRecent extends BaseHook {
    @Override
    public void init() {
        findAndHookMethod(Settings.Global.class, "getInt", ContentResolver.class, String.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String name = (String) param.args[1];
                if ("task_stack_view_layout_style".equals(name)) {
                    param.setResult(2);
                }
            }
        });
    }
}
