package com.sevtinge.hyperceiler.hook.module.hook.fileexplorer;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Feature 7/24] Unlock full-disk indexing and bypass search query throttling in File Explorer.
 */
public class ExperimentFileExplorerFastSearch extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchManager", "isFastSearchEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchManager", "isDeepIndexingSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchSettings", "isSearchThrottlingEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.util.RomUtils", "isLowRamSearch", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchEngine", "isThrottlingEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
    }
}
