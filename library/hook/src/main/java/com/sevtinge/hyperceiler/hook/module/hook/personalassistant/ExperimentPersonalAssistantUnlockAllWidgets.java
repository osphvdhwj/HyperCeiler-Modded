package com.sevtinge.hyperceiler.hook.module.hook.personalassistant;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

/**
 * [Experiment] Unlock all widgets in App Vault / Personal Assistant, bypassing device, pad, and third-party restrictions.
 */
public class ExperimentPersonalAssistantUnlockAllWidgets extends BaseHook {

    @Override
    public void init() {
        // WidgetManager capability checks
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetManager", "isWidgetSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetManager", "isWidgetAvailable", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetManager", "checkWidgetSupport", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetManager", "isThirdPartyWidgetAllowed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // Widget filter & restriction bypass
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetFilter", "isBlocked", new MethodHook() {
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
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetFilter", "isWidgetRestricted", new MethodHook() {
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

        // DeviceUtils & WidgetConfig checks
        findAndHookMethodSilently("com.miui.personalassistant.util.DeviceUtils", "isPad", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.util.DeviceUtils", "isFoldable", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.util.DeviceUtils", "isSupportWidget", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.widget.WidgetConfig", "isWidgetSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.personalassistant.picker.WidgetPickerAdapter", "isWidgetLocked", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // Ensure Build.IS_TABLET or isTablet() returns true if queried by personalassistant
        try {
            Class<?> buildClass = findClassIfExists("miui.os.Build");
            if (buildClass != null) {
                XposedHelpers.setStaticBooleanField(buildClass, "IS_TABLET", true);
            }
        } catch (Throwable ignored) {
        }
    }
}
