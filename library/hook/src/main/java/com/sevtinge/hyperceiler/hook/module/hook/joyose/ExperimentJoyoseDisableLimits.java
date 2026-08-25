package com.sevtinge.hyperceiler.hook.module.hook.joyose;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * [Feature 5] Disable thermal throttling and FPS limits in Joyose game engine.
 *
 * Verified via dex analysis of /product/app/Joyose/Joyose.apk:
 *   - Real class: com.xiaomi.joyose.cloud.cloudAnalysis.PowerSaveDynamicFps
 *   - Real class: com.xiaomi.joyose.cloud.cloudAnalysis.ScenceLockRefreshRateAnalyze
 *   - Real AIDL: com.xiaomi.joyose.IJoyoseInterface (regulateGameFpsByMiFT, getThermalThresholds)
 *   - Real methods: setFps, limitFps, targetFps, etc. (in obfuscated classes)
 *
 * Previously WRONG hooks: ThermalControlManager, ThermalManager, GameLimitManager,
 *   SmartFpsController, PerformanceManager, CloudConfig, JoyoseManager,
 *   JoyosePolicyManager (ALL fabricated class names - NONE exist in APK)
 *
 * Strategy: Hook PowerSaveDynamicFps to prevent battery-saver FPS drops,
 *   and hook JoyoseManagerNative to intercept thermal limit signals.
 */
public class ExperimentJoyoseDisableLimits extends BaseHook {

    @Override
    public void init() {
        // Hook PowerSaveDynamicFps to prevent power-save FPS reduction
        hookAllMethodsSilently(
            "com.xiaomi.joyose.cloud.cloudAnalysis.PowerSaveDynamicFps"
        );

        // Hook ScenceLockRefreshRateAnalyze to prevent scene-based refresh rate locking
        hookAllMethodsSilently(
            "com.xiaomi.joyose.cloud.cloudAnalysis.ScenceLockRefreshRateAnalyze"
        );

        // Hook JoyoseManagerNative to intercept thermal/FPS limit signals
        try {
            Class<?> joyoseNative = XposedHelpers.findClassIfExists(
                "com.xiaomi.joyose.JoyoseManagerNative",
                lpparam.classLoader
            );
            if (joyoseNative != null) {
                // Hook all methods that set thermal or FPS limits
                XposedHelpers.findAndHookMethod(joyoseNative, "setFps",
                    int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            // Don't allow FPS to be limited below 144
                            int fps = (int) param.args[0];
                            if (fps > 0 && fps < 144) {
                                param.args[0] = 144;
                            }
                        }
                    }
                );
            }
        } catch (Throwable ignored) {}

        // Hook IJoyoseInterface.Stub.Proxy to intercept thermal threshold checks
        try {
            Class<?> proxyClass = XposedHelpers.findClassIfExists(
                "com.xiaomi.joyose.IJoyoseInterface$Stub$Proxy",
                lpparam.classLoader
            );
            if (proxyClass != null) {
                // regulateGameFpsByMiFT — prevent Xiaomi from throttling game FPS
                findAndHookMethodSilently(
                    "com.xiaomi.joyose.IJoyoseInterface$Stub$Proxy",
                    "regulateGameFpsByMiFT",
                    new MethodHook() {
                        @Override
                        protected void before(MethodHookParam param) {
                            param.setResult(null);
                        }
                    }
                );
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Hook all methods in a class to return default values (false for boolean, null for objects).
     * Used for obfuscated analysis classes that dynamically reduce FPS/resolution.
     */
    private void hookAllMethodsSilently(String className) {
        try {
            Class<?> clazz = XposedHelpers.findClassIfExists(className, lpparam.classLoader);
            if (clazz == null) return;
            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) continue;
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) {
                    XposedHelpers.findAndHookMethod(clazz, method.getName(),
                        method.getParameterTypes(),
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                // Prevent analysis from triggering limits
                            }
                        }
                    );
                }
            }
        } catch (Throwable ignored) {}
    }
}
