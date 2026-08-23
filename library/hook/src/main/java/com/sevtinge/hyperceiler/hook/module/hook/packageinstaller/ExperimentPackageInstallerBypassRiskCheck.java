package com.sevtinge.hyperceiler.hook.module.hook.packageinstaller;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Bypass security risk checks, malicious app warnings, harmful app dialogs,
 * and high-risk installation interception in Package Installer.
 */
public class ExperimentPackageInstallerBypassRiskCheck extends BaseHook {

    @Override
    public void init() {
        // RiskInfo model bypass
        findAndHookMethodSilently("com.miui.packageinstaller.model.RiskInfo", "isHighRisk", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.model.RiskInfo", "isHarmful", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.model.RiskInfo", "isRiskApp", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.model.RiskInfo", "hasVirus", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.model.RiskInfo", "getRiskLevel", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(0);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.model.RiskInfo", "isRisk", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.model.RiskInfo", "needInterception", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // SecurityCheckHelper & SafeCheckManager
        findAndHookMethodSilently("com.miui.packageinstaller.compat.SecurityCheckHelper", "isRiskWarningNeeded", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.compat.SecurityCheckHelper", "isBlocked", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.compat.SecurityCheckHelper", "isHarmful", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.safety.SafeCheckManager", "isSafeCheckPassed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.safety.SafeCheckManager", "needSafeTip", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.safety.SafeCheckManager", "isPureModeStrict", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // Business SecurityCheckManager
        findAndHookMethodSilently("com.miui.packageinstaller.business.security.SecurityCheckManager", "checkRisk", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.business.security.SecurityCheckManager", "isHarmful", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.business.security.SecurityCheckManager", "isRiskApp", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.business.security.SecurityCheckManager", "needInterception", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // Scan results
        findAndHookMethodSilently("com.miui.packageinstaller.model.ScanResult", "hasRisk", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.model.ScanResult", "isSafe", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.business.model.ApkScanResult", "isSafe", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.business.model.ApkScanResult", "isHarmful", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // RiskVerifyUtils
        findAndHookMethodSilently("com.miui.packageinstaller.utility.RiskVerifyUtils", "isRiskWarningNeeded", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.utility.RiskVerifyUtils", "isBlocked", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.utility.RiskVerifyUtils", "isHighRiskInstall", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
    }
}
