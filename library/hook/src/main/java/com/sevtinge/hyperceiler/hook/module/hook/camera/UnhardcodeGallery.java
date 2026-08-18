package com.sevtinge.hyperceiler.hook.module.hook.camera;

import android.content.Intent;
import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class UnhardcodeGallery extends BaseHook {
    @Override
    public void init() {
        findAndHookMethod(Intent.class, "setPackage", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if ("com.miui.gallery".equals(param.args[0])) {
                    param.args[0] = null;
                }
            }
        });
    }
}
