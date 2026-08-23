package com.sevtinge.hyperceiler.hook.module.hook.incallui;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentInCallUiShowCallInfo extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.android.incallui.CallCardPresenter", "isSupportCallLocation",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.CallCardPresenter", "shouldShowCallDetails",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.CallCardPresenter", "isSupportVoLteCallInfo",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.CallCardPresenter", "isSupport5GCallInfo",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.InCallPresenter", "isSupportCallLocation",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.InCallPresenter", "isSupportVoLteCallInfo",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.InCallPresenter", "isSupportHdVoiceCallInfo",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.InCallPresenter", "isSupport5GCallInfo",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.InCallPresenter", "getCallInfoVisibility",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(0); // View.VISIBLE
                }
            });

        findAndHookMethodSilently("com.android.incallui.Call", "isHdCall",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.Call", "isHighQualityVoiceCall",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
