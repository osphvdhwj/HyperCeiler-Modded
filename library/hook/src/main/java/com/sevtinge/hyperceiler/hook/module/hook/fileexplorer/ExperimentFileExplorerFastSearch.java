package com.sevtinge.hyperceiler.hook.module.hook.fileexplorer;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Experiment] Unlock fast search, deep search indexing, and remove search throttling in File Explorer.
 */
public class ExperimentFileExplorerFastSearch extends BaseHook {

    @Override
    public void init() {
        // SearchManager capabilities
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchManager", "isFastSearchSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchManager", "isDeepSearchEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchManager", "isIndexSearchSupported", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchManager", "isSearchOptimizationEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // SearchSettings hooks
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchSettings", "isFastSearchEnable", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchSettings", "isLocalSearchFast", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchSettings", "isIndexReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchSettings", "isDeepSearchOn", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // RomUtils hooks
        findAndHookMethodSilently("com.android.fileexplorer.util.RomUtils", "supportFastSearch", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.util.RomUtils", "supportDeepSearch", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.util.RomUtils", "supportLocalSearchIndex", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // SearchEngine throttling bypass
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchEngine", "isThrottlingEnabled", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.search.SearchEngine", "getSearchDelay", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(0L);
            }
        });
    }
}
