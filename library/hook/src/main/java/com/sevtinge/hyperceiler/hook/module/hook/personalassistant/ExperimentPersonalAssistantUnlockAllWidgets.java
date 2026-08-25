package com.sevtinge.hyperceiler.hook.module.hook.personalassistant;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Target Feature 5] Unlock all widget types, tablet widgets, and restricted widgets in App Vault (Personal Assistant).
 */
public class ExperimentPersonalAssistantUnlockAllWidgets extends BaseHook {

    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetManager", "isWidgetSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetManager", "isTabletWidgetAllowed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetManager", "isWidgetRestricted", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetManager", "isWidgetBlacklisted", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetFilter", "isRestricted", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetFilter", "isBlacklisted", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.util.DeviceUtils", "isLowEndDevice", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
    }
}
