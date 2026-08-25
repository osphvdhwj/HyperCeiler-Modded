package com.sevtinge.hyperceiler.hook.module.hook.packageinstaller;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Feature 4] Bypass virus scanning and risk check delays in Package Installer.
 *
 * Verified via dex analysis of /product/priv-app/MiuiPackageInstaller/MiuiPackageInstaller.apk:
 *   - Real namespace: com.miui.packageInstaller (capital I)
 *   - Real class: com.miui.packageInstaller.installer.InstallTask (contains startVirusScan)
 *   - Real class: com.miui.packageInstaller.NewVirusScanner
 *   - Real class: com.miui.packageInstaller.util.XiaoMiSafe
 *   - Real methods: getInstallerCloseSafetyModel, getInstallerOpenSafetyModel
 *   - Real methods: startVirusScan, checkPackageSafe, getRiskContent
 *
 * Previously WRONG hooks: com.miui.packageinstaller.model.RiskInfo (doesn't exist - lowercase i),
 *   SecurityCheckHelper, SafeCheckManager, RiskVerifyUtils (all fabricated class names)
 */
public class ExperimentPackageInstallerBypassRiskCheck extends BaseHook {

    @Override
    public void init() {
        // Skip virus scanning in InstallTask
        findAndHookMethodSilently(
            "com.miui.packageInstaller.installer.InstallTask",
            "startVirusScan",
            new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        // NewVirusScanner bypass
        findAndHookMethodSilently(
            "com.miui.packageInstaller.NewVirusScanner",
            "scan",
            new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        // XiaoMiSafe check bypass
        findAndHookMethodSilently(
            "com.miui.packageInstaller.util.XiaoMiSafe",
            "checkPackageSafe",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        // Bypass risk content display
        findAndHookMethodSilently(
            "com.miui.packageInstaller.model.PositiveButtonRules",
            "getRiskContent",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        // Bypass safety model enforcement
        findAndHookMethodSilently(
            "com.miui.packageInstaller.model.PositiveButtonRules",
            "getInstallerCloseSafetyModel",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );
    }
}
