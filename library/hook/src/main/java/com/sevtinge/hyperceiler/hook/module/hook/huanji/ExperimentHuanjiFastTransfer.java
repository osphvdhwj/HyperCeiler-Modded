package com.sevtinge.hyperceiler.hook.module.hook.huanji;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Target Feature 3] Unlock 5GHz Wi-Fi Direct, increase buffer/chunk sizes, and bypass speed limits in Mi Mover (Huanji).
 */
public class ExperimentHuanjiFastTransfer extends BaseHook {

    private static final int BUFFER_SIZE = 1024 * 1024; // 1MB buffer
    private static final int CHUNK_SIZE = 2 * 1024 * 1024; // 2MB chunk
    private static final int MAX_THREADS = 8;

    @Override
    public void init() {
        // TransferConfig hooks
        findAndHookMethodSilently("com.miui.huanji.transfer.TransferConfig", "getBufferSize", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(BUFFER_SIZE);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.transfer.TransferConfig", "getChunkSize", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(CHUNK_SIZE);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.transfer.TransferConfig", "getMaxConcurrentThreads", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(MAX_THREADS);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.transfer.TransferConfig", "is5GhzSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.transfer.TransferConfig", "isHighSpeedTransferEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // Config package variants
        findAndHookMethodSilently("com.miui.huanji.config.TransferConfig", "getBufferSize", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(BUFFER_SIZE);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.config.TransferConfig", "getChunkSize", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(CHUNK_SIZE);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.config.TransferConfig", "is5GhzSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // Network Wi-Fi Direct / 5GHz Hotspot
        findAndHookMethodSilently("com.miui.huanji.network.WifiP2pHelper", "isForce5GHz", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.network.WifiP2pHelper", "isFastP2pEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.network.HotspotManager", "isForce5GHz", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // Speed limiter & throttling bypass
        findAndHookMethodSilently("com.miui.huanji.transfer.SpeedLimiter", "isThrottlingEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.transfer.SpeedLimiter", "getMaxTransferSpeed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(Long.MAX_VALUE);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.transfer.RateLimiter", "isThrottlingEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // AppDataTransferManager fast mode
        findAndHookMethodSilently("com.miui.huanji.model.AppDataTransferManager", "isFastTransferMode", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.miui.huanji.model.AppDataTransferManager", "isParallelTransferEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
    }
}
