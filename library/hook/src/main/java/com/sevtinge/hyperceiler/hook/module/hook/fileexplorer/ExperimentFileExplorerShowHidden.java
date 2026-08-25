package com.sevtinge.hyperceiler.hook.module.hook.fileexplorer;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * [Feature 6/24] Force File Explorer to always show hidden files, dotfiles, and system directories.
 */
public class ExperimentFileExplorerShowHidden extends BaseHook {
    @Override
    public void init() {
        findAndHookMethodSilently("com.android.fileexplorer.util.SettingUtil", "isShowHiddenFiles", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.settings.FileExplorerSettings", "showHiddenFiles", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.settings.FileExplorerSettings", "getShowHiddenFiles", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.model.FileSortHelper", "isHideSystemFile", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.util.FileUtils", "isHiddenFile", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
    }
}
