package com.sevtinge.hyperceiler.hook.module.hook.updater;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Target Feature 6] Force enable local update package selection ("Choose update package") and bypass ROM validation in Updater.
 */
public class ExperimentUpdaterForceLocalUpdate extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.android.updater.util.RomUtils", "isLocalUpdateAllowed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.util.RomUtils", "isDeveloperMenuEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.UpdateActivity", "isLocalUpdateSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.UpdateActivity", "isPackageValid", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.VerifyPackageService", "verifyRomPackage", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
    }
}
