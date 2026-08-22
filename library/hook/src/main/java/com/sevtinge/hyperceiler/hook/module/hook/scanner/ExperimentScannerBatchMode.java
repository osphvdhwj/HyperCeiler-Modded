package com.sevtinge.hyperceiler.hook.module.hook.scanner;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentScannerBatchMode extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.xiaomi.scanner.config.ConfigModel", "isBatchModeEnabled", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
