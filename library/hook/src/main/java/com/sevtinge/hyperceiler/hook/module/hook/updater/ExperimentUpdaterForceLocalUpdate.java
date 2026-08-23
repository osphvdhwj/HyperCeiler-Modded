package com.sevtinge.hyperceiler.hook.module.hook.updater;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Force-enable local update package selection ("Choose update package") and bypass validation checks in Updater.
 */
public class ExperimentUpdaterForceLocalUpdate extends BaseHook {

    @Override
    public void init() {
        // RomUtils & updater utilities
        findAndHookMethodSilently("com.android.updater.common.RomUtils", "isLocalUpdateAllowed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.common.RomUtils", "supportLocalUpdate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.common.RomUtils", "isChoosePackageEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.common.RomUtils", "isDeveloperOptionEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.common.RomUtils", "isAdvanceMenuVisible", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.util.RomUtils", "isLocalUpdateAllowed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.util.RomUtils", "supportLocalUpdate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // UI Activity menu visibility
        findAndHookMethodSilently("com.android.updater.ui.UpdateActivity", "showLocalUpdate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.ui.UpdateActivity", "isChoosePackageMenuVisible", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.MainActivity", "isLocalUpgradeAllowed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // Verification bypass
        findAndHookMethodSilently("com.android.updater.service.VerifyPackageService", "verifyPackage", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.service.OtaVerifier", "checkZipValid", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.updater.service.OtaVerifier", "isPackageValid", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // FeatureParser hook
        findAndHookMethodSilently("miui.util.FeatureParser", "hasFeature", String.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                if ("support_ota_validate".equals(param.args[0])) {
                    param.setResult(false);
                } else if ("support_local_update".equals(param.args[0])) {
                    param.setResult(true);
                }
            }
        });
    }
}
