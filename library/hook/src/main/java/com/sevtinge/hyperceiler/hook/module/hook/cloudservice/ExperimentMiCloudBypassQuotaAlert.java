package com.sevtinge.hyperceiler.hook.module.hook.cloudservice;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentMiCloudBypassQuotaAlert extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.cloudservice.notification.QuotaNotificationManager", "shouldShowQuotaNotification",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.cloudservice.notification.QuotaNotificationManager", "isQuotaExceededAlertDisabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.cloudservice.notification.QuotaNotificationManager", "needShowQuotaWarning",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.cloudservice.storage.StorageManager", "isOutOfStorage",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.cloudservice.storage.StorageManager", "isQuotaFull",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.cloudservice.sync.QuotaAlertController", "shouldAlertQuotaLimit",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.cloudservice.sync.QuotaAlertController", "needShowStorageDialog",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });
    }
}
