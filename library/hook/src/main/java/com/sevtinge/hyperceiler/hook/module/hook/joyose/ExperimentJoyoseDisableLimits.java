package com.sevtinge.hyperceiler.hook.module.hook.joyose;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Disable thermal throttling, FPS limiters, resolution downscaling, and background performance limits in Joyose.
 */
public class ExperimentJoyoseDisableLimits extends BaseHook {

    @Override
    public void init() {
        // Thermal manager limit hooks
        findAndHookMethodSilently("com.xiaomi.joyose.thermal.ThermalControlManager", "isThermalThrottlingEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.thermal.ThermalControlManager", "needLimitFps", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.thermal.ThermalControlManager", "getFpsLimit", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(144);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.thermal.ThermalManager", "isThermalThrottlingEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.thermal.ThermalManager", "needLimitFps", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // Game limit & smart FPS controller
        findAndHookMethodSilently("com.xiaomi.joyose.game.GameLimitManager", "isDownscaleEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.game.GameLimitManager", "isFpsCapEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.game.GameLimitManager", "shouldThrottle", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.game.GameLimitManager", "isGameLimitEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.smartfps.SmartFpsController", "isFpsCapEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.smartfps.SmartFpsController", "shouldThrottle", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // Performance manager
        findAndHookMethodSilently("com.xiaomi.joyose.performance.PerformanceManager", "isThrottlingActive", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.xiaomi.joyose.performance.PerformanceManager", "needLimitPerformance", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // Cloud config limits
        findAndHookMethodSilently("com.xiaomi.joyose.config.CloudConfig", "isLimitEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
    }
}
