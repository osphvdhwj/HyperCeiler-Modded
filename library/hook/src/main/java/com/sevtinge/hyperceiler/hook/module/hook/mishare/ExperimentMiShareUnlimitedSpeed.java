package com.sevtinge.hyperceiler.hook.module.hook.mishare;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentMiShareUnlimitedSpeed extends BaseHook {

    private static final int MAX_BANDWIDTH_SPEED = 100_000_000; // 100 MB/s

    @Override
    public void init() {
        findAndHookMethodSilently("com.miui.mishare.connectivity.MiShareTask", "isTurboSpeedEnabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.mishare.connectivity.MiShareTask", "getMaxTransferSpeed",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(MAX_BANDWIDTH_SPEED);
                }
            });

        findAndHookMethodSilently("com.miui.mishare.connectivity.MiShareTask", "isSpeedLimitDisabled",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.mishare.connectivity.transfer.TransferManager", "isSpeedLimited",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(false);
                }
            });

        findAndHookMethodSilently("com.miui.mishare.connectivity.transfer.TransferManager", "getMaxBandwidth",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(MAX_BANDWIDTH_SPEED);
                }
            });

        findAndHookMethodSilently("com.miui.mishare.connectivity.device.DeviceHelper", "isSupport5GhzP2P",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });

        findAndHookMethodSilently("com.miui.mishare.connectivity.device.DeviceHelper", "isTurboSpeedSupported",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            });
    }
}
