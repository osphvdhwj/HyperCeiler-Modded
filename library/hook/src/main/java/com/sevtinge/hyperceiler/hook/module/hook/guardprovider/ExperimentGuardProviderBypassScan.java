package com.sevtinge.hyperceiler.hook.module.hook.guardprovider;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentGuardProviderBypassScan extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.guardprovider.service.SecurityScanService", "isSafe",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.service.SecurityScanService", "needScan",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.service.SecurityScanService", "isVirus",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.service.SecurityScanService", "isRisk",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.manager.ScanManager", "needScan",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.manager.ScanManager", "isRiskApp",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.manager.ScanManager", "checkAppSafe",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.sdk.android.pojo.p", "isIntercept",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.sdk.android.pojo.networkdomain.ResultReportData", "getThreatLevel",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(0);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.sdk.android.pojo.networkdomain.ResultReportData", "getThreatName",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(null);
                }
            });

        findAndHookMethodSilently("com.miui.guardprovider.sdk.android.pojo.networkdomain.TrustCert", "getStatus",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(1);
                }
            });
    }
}
