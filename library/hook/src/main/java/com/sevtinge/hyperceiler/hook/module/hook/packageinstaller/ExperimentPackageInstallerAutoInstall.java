package com.sevtinge.hyperceiler.hook.module.hook.packageinstaller;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Skip countdown timers on install buttons and automatically confirm installation in Package Installer.
 */
public class ExperimentPackageInstallerAutoInstall extends BaseHook {

    @Override
    public void init() {
        // Bypass countdown delay on countdown buttons
        findAndHookMethodSilently("com.miui.packageinstaller.ui.view.CountDownButton", "startCountDown", int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                param.args[0] = 0;
            }
            @Override
            protected void after(MethodHookParam param) {
                if (param.thisObject instanceof View) {
                    ((View) param.thisObject).setEnabled(true);
                }
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.view.CountDownButton", "startCountDown", int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                param.args[0] = 0;
            }
            @Override
            protected void after(MethodHookParam param) {
                if (param.thisObject instanceof View) {
                    ((View) param.thisObject).setEnabled(true);
                }
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.ui.widget.CountDownButton", "startCountDown", int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                param.args[0] = 0;
            }
            @Override
            protected void after(MethodHookParam param) {
                if (param.thisObject instanceof View) {
                    ((View) param.thisObject).setEnabled(true);
                }
            }
        });
        findAndHookMethodSilently("com.miui.packageinstaller.ui.view.CountDownView", "startCountDown", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // Auto click install button in InstallConfirmActivity / NewInstallConfirmActivity
        findAndHookMethodSilently("com.miui.packageinstaller.ui.InstallConfirmActivity", "onResume", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                if (param.thisObject instanceof Activity) {
                    Activity activity = (Activity) param.thisObject;
                    View installBtn = activity.findViewById(activity.getResources().getIdentifier("ok_button", "id", activity.getPackageName()));
                    if (installBtn == null) {
                        installBtn = activity.findViewById(activity.getResources().getIdentifier("btn_install", "id", activity.getPackageName()));
                    }
                    if (installBtn != null && installBtn.isEnabled() && installBtn.getVisibility() == View.VISIBLE) {
                        installBtn.performClick();
                    }
                }
            }
        });

        findAndHookMethodSilently("com.miui.packageinstaller.ui.NewInstallConfirmActivity", "onResume", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                if (param.thisObject instanceof Activity) {
                    Activity activity = (Activity) param.thisObject;
                    View installBtn = activity.findViewById(activity.getResources().getIdentifier("ok_button", "id", activity.getPackageName()));
                    if (installBtn == null) {
                        installBtn = activity.findViewById(activity.getResources().getIdentifier("btn_install", "id", activity.getPackageName()));
                    }
                    if (installBtn != null && installBtn.isEnabled() && installBtn.getVisibility() == View.VISIBLE) {
                        installBtn.performClick();
                    }
                }
            }
        });

        // Presenter auto install
        findAndHookMethodSilently("com.miui.packageinstaller.ui.NormalInstallPresenter", "onScanCompleted", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                safeFindAndHookMethod("com.miui.packageinstaller.ui.NormalInstallPresenter", "startInstall");
            }
        });
    }
}
