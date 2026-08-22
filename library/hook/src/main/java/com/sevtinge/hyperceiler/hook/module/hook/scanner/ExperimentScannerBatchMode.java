package com.sevtinge.hyperceiler.hook.module.hook.scanner;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

public class ExperimentScannerBatchMode extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.xiaomi.scanner.config.ConfigModel", "isBatchModeEnabled", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }
}
