package com.sevtinge.hyperceiler.hook.module.hook.phone;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class ExperimentPhoneRecordAll extends BaseHook {
    @Override
    public void init() {
        try {
            findAndHookMethod("com.android.phone.CallRecorder", "isAutoRecordEnabled", new MethodHook() {
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
