package com.sevtinge.hyperceiler.hook.module.hook.incallui;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentInCallUiFullAvatar extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.android.incallui.ContactInfoCache", "isAvatarLoaded",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.ContactInfoCache", "hasFullSizePhoto",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.ContactInfoCache", "isHighResolutionPhoto",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.CallCardPresenter", "isLargeAvatar",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.CallCardPresenter", "isFullAvatar",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.CallCardPresenter", "shouldShowLargeAvatar",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.CallCardPresenter", "isShowBigPhoto",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.CallerInfo", "isFullAvatar",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.view.DoubleCallInfoView", "isAvatarViewVisible",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.view.DoubleCallInfoView", "hasVisibleAvatarView",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.android.incallui.view.VoLTEVideoPreview", "getFullScreenMode",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
