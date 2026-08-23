package com.sevtinge.hyperceiler.hook.module.hook.aod;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

/**
 * [Experiment] Unlock all Always On Display clock styles, super wallpaper AOD modes, and signature effects.
 */
public class ExperimentAodUnlockAllStyles extends BaseHook {

    @Override
    public void init() {
        // AodStyleManager & StyleFilter capability hooks
        findAndHookMethodSilently("com.miui.aod.util.AodStyleManager", "isStyleSupported", String.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.util.AodStyleManager", "isSuperWallpaperAodSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.util.AodStyleManager", "isAllStyleUnlocked", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.util.AodStyleManager", "isDualClockSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.util.AodStyleManager", "isSignatureSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.util.AodStyleManager", "isCustomImageSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.AodStyleManager", "isStyleSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.AodStyleManager", "isPremiumStyle", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // AODUtils & FeatureUtils
        findAndHookMethodSilently("com.miui.aod.util.AODUtils", "isSupportAod", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.util.AODUtils", "isSupportSuperWallpaper", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.util.AODUtils", "isOledScreen", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.util.FeatureUtils", "isSupportAod", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.util.FeatureUtils", "isLowRamDevice", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // AODSettings hooks
        findAndHookMethodSilently("com.miui.aod.widget.AODSettings", "isSupportAod", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.aod.widget.AODSettings", "isSupportSuperWallpaper", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // Static boolean flags
        try {
            Class<?> aodUtils = findClassIfExists("com.miui.aod.Utils");
            if (aodUtils != null) {
                XposedHelpers.setStaticBooleanField(aodUtils, "SUPPORT_AOD_AON", true);
                XposedHelpers.setStaticBooleanField(aodUtils, "SUPPORT_SUPER_WALLPAPER", true);
            }
        } catch (Throwable ignored) {
        }
    }
}
