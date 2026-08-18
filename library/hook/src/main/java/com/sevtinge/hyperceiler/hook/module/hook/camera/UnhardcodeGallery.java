package com.sevtinge.hyperceiler.hook.module.hook.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class UnhardcodeGallery extends BaseHook {
    @Override
    public void init() {
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] instanceof Intent) {
                    Intent intent = (Intent) param.args[0];
                    if ("com.android.camera.action.REVIEW".equals(intent.getAction()) || 
                        "com.miui.gallery".equals(intent.getPackage())) {
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setPackage(null);
                        intent.setComponent(null);
                    }
                }
            }
        };

        findAndHookMethod(Activity.class, "startActivity", Intent.class, hook);
        findAndHookMethod(Activity.class, "startActivity", Intent.class, Bundle.class, hook);
        findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, hook);
        findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, Bundle.class, hook);
    }
}
