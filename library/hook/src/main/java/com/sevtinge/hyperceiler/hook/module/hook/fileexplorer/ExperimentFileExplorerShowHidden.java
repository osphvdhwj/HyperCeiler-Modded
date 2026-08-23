package com.sevtinge.hyperceiler.hook.module.hook.fileexplorer;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import java.io.File;

/**
 * [Experiment] Force File Explorer to always show hidden files, dotfiles, and system directories.
 */
public class ExperimentFileExplorerShowHidden extends BaseHook {

    @Override
    public void init() {
        // SettingUtil hooks
        findAndHookMethodSilently("com.android.fileexplorer.util.SettingUtil", "isShowHiddenFile", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.util.SettingUtil", "isShowHidden", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.util.SettingUtil", "getShowHidden", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.settings.SettingUtil", "isShowHiddenFile", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.settings.SettingUtil", "isShowHidden", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // Model settings hooks
        findAndHookMethodSilently("com.android.fileexplorer.model.FileExplorerSettings", "isShowHidden", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.model.FileExplorerSettings", "isShowHiddenFiles", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.model.FileExplorerSettings", "getShowHiddenFiles", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.model.SettingUtil", "isShowHidden", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.model.SettingUtil", "isShowHiddenFile", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // FileSortHelper & FileFilter bypass
        findAndHookMethodSilently("com.android.fileexplorer.util.FileSortHelper", "isHideFile", File.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.util.FileSortHelper", "isHideFile", String.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.util.FileUtils", "isHidden", File.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.util.FileUtils", "isHidden", String.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(false);
            }
        });

        // Model Util hooks
        findAndHookMethodSilently("com.android.fileexplorer.model.Util", "shouldShowHiddenFile", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.model.Util", "isShowHidden", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
        findAndHookMethodSilently("com.android.fileexplorer.model.Util", "isShowHiddenFile", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });

        // Controller hooks
        findAndHookMethodSilently("com.android.fileexplorer.controller.FileCategoryController", "isShowHidden", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                param.setResult(true);
            }
        });
    }
}
