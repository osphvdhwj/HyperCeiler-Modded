package com.sevtinge.hyperceiler.hook.module.hook.various;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class UnhardcodeNotes extends BaseHook {
    @Override
    public void init() {
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args.length > 0 && param.args[0] instanceof Intent) {
                    Intent intent = (Intent) param.args[0];
                    if ("com.miui.notes".equals(intent.getPackage()) ||
                        (intent.getComponent() != null && "com.miui.notes".equals(intent.getComponent().getPackageName()))) {
                        
                        // Change package to Google Keep
                        intent.setPackage("com.google.android.keep");
                        
                        // If it had a specific component in miui notes, clear it so Keep can resolve the action naturally
                        if (intent.getComponent() != null) {
                            intent.setComponent(null);
                        }
                    }
                }
            }
        };

        try {
            findAndHookMethod(Activity.class, "startActivity", Intent.class, hook);
        } catch (Throwable t) { }
        
        try {
            findAndHookMethod(Activity.class, "startActivity", Intent.class, Bundle.class, hook);
        } catch (Throwable t) { }
        
        try {
            findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, hook);
        } catch (Throwable t) { }
        
        try {
            findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, Bundle.class, hook);
        } catch (Throwable t) { }
    }
}
