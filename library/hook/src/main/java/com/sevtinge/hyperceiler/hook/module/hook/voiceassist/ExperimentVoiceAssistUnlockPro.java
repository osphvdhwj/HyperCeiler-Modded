package com.sevtinge.hyperceiler.hook.module.hook.voiceassist;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentVoiceAssistUnlockPro extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.voiceassist.model.UserModel", "isProUser",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.voiceassist.model.UserModel", "isVip",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.voiceassist.model.UserModel", "isFeatureUnlocked",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.voiceassist.model.UserModel", "isLargeModelEnabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.voiceassist.model.UserModel", "hasVipPrivilege",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.voiceassist.util.VipManager", "isVipUser",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.voiceassist.util.VipManager", "isProSubscriber",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.voiceassist.ai.AiFeatureManager", "isLargeModelSupported",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
