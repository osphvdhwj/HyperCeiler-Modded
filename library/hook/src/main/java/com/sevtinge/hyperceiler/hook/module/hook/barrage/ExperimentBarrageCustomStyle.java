package com.sevtinge.hyperceiler.hook.module.hook.barrage;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentBarrageCustomStyle extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.xiaomi.barrage.settings.BarrageSettingsPreferences", "getTextScaleSize",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    Object result = param.getResult();
                    if (result instanceof Float && (Float) result < 1.5f) {
                        param.setResult(1.5f);
                    }
                }
            });

        findAndHookMethodSilently("com.xiaomi.barrage.utils.UiUtils", "getFontSizeScale",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    Object result = param.getResult();
                    if (result instanceof Float && (Float) result < 1.5f) {
                        param.setResult(1.5f);
                    }
                }
            });

        findAndHookMethodSilently("com.xiaomi.barrage.settings.BarrageSettingsPreferences", "getBubbleStyleSelectedPosition",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    // Unlock style selection
                }
            });

        findAndHookMethodSilently("com.xiaomi.barrage.ui.widget.BarrageView", "isCustomStyleEnabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.xiaomi.barrage.ui.widget.BarrageView", "isVipStyleUnlocked",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
