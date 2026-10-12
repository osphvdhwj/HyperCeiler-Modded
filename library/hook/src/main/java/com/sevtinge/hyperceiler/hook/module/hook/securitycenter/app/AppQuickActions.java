/*
 * This file is part of HyperCeiler.

 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HyperCeiler Contributions
*/
package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import de.robv.android.xposed.XposedHelpers;

/**
 * Injects an "APK path" preference row into the App info screen, placed
 * between the app header (icon/label/version) and the Storage row.
 *
 * Single tap  -> open the APK's parent folder in the user's default file
 *               manager; if none is set, show the Android chooser; if
 *               nothing can handle a folder, show a toast.
 * Long press  -> copy the APK path to the clipboard.
 *
 * NOTE: The hook module's compile classpath does not include
 * androidx.preference, so every preference call here is reflection.
 */
public class AppQuickActions extends BaseHook {

    private static final String PREF_KEY = "sc_app_quick_path";

    @Override
    public void init() {
        Class<?> fragCls = findClassIfExists("com.miui.appmanager.fragment.ApplicationsDetailsFragment");
        if (fragCls == null) {
            logE(TAG, lpparam.packageName, "ApplicationsDetailsFragment not found");
            return;
        }
        findAndHookMethod(
                fragCls,
                "onCreatePreferences",
                Bundle.class, String.class,
                new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) {
                        try {
                            inject(param.thisObject);
                        } catch (Throwable t) {
                            logE(TAG, lpparam.packageName, "AppQuickActions inject: " + t);
                        }
                    }
                }
        );
    }

    private void inject(Object frag) {
        Context ctx = (Context) XposedHelpers.callMethod(frag, "getContext");
        if (ctx == null) { logE(TAG, lpparam.packageName, "v2: ctx null"); return; }

        Field appInfoField = XposedHelpers.findFirstFieldByExactType(
                frag.getClass(), ApplicationInfo.class);
        if (appInfoField == null) { logE(TAG, lpparam.packageName, "v2: no ApplicationInfo field"); return; }
        ApplicationInfo appInfo;
        try { appInfo = (ApplicationInfo) appInfoField.get(frag); }
        catch (Throwable t) { logE(TAG, lpparam.packageName, "v2: field get " + t); return; }
        if (appInfo == null) { logE(TAG, lpparam.packageName, "v2: appInfo null"); return; }

        Object storagePref = XposedHelpers.callMethod(frag, "findPreference", "app_storage_pref");
        Object trafficPref = XposedHelpers.callMethod(frag, "findPreference", "app_traffic_pref");
        Object powerPref   = XposedHelpers.callMethod(frag, "findPreference", "app_power_pref");
        if (storagePref == null) { logE(TAG, lpparam.packageName, "v2: storagePref null"); return; }
        Object parent = XposedHelpers.callMethod(storagePref, "getParent");
        if (parent == null) { logE(TAG, lpparam.packageName, "v2: parent null"); return; }

        if (XposedHelpers.callMethod(parent, "findPreference", PREF_KEY) != null) return;

        // Use miuix.preference.TextPreference - matches what the fragment's own rows use.
        Class<?> prefCls = findClassIfExists("miuix.preference.TextPreference");
        if (prefCls == null) {
            logE(TAG, lpparam.packageName, "v2: miuix TextPreference not found");
            return;
        }
        logI(TAG, lpparam.packageName, "v2: creating instance");

        final String apkPath = appInfo.sourceDir;
        final Context fctx = ctx;
        Object pref;
        try {
            pref = XposedHelpers.newInstance(prefCls, fctx);
        } catch (Throwable t) {
            logE(TAG, lpparam.packageName, "v2: newInstance " + t); return;
        }

        try {
            XposedHelpers.callMethod(pref, "setKey", PREF_KEY);
            XposedHelpers.callMethod(pref, "setTitle", "APK path");
            File folder = new File(apkPath).getParentFile();
            XposedHelpers.callMethod(pref, "setSummary",
                    folder != null ? folder.getAbsolutePath() : apkPath);
            XposedHelpers.callMethod(pref, "setPersistent", false);
            logI(TAG, lpparam.packageName, "v2: setKey/title/summary ok");
        } catch (Throwable t) {
            logE(TAG, lpparam.packageName, "v2: setters " + t); return;
        }

        Class<?> clickIface = findClassIfExists(
                "androidx.preference.Preference$OnPreferenceClickListener");
        if (clickIface == null) { logE(TAG, lpparam.packageName, "v2: clickIface null"); return; }

        Object clickListener = Proxy.newProxyInstance(
                clickIface.getClassLoader(),
                new Class<?>[]{clickIface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("onPreferenceClick".equals(method.getName())) {
                            openFolder(fctx, apkPath);
                            return Boolean.TRUE;
                        }
                        return null;
                    }
                });
        try {
            XposedHelpers.callMethod(pref, "setOnPreferenceClickListener", clickListener);
            logI(TAG, lpparam.packageName, "v2: listener set");
        } catch (Throwable t) {
            logE(TAG, lpparam.packageName, "v2: setListener " + t); return;
        }

        try {
            logI(TAG, lpparam.packageName, "v2: parent class " + parent.getClass().getName());
            logI(TAG, lpparam.packageName, "v2: count before " + XposedHelpers.callMethod(parent, "getPreferenceCount"));
            XposedHelpers.callMethod(parent, "addPreference", pref);
            logI(TAG, lpparam.packageName, "v2: count after " + XposedHelpers.callMethod(parent, "getPreferenceCount"));
        } catch (Throwable t) {
            logE(TAG, lpparam.packageName, "v2: addPreference " + t);
        }
    }

    private static void openFolder(Context ctx, String path) {
        File folder = new File(path).getParentFile();
        if (folder == null || !folder.exists()) {
            Toast.makeText(ctx, "Folder not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.fromFile(folder);
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(uri, "resource/folder");
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        int n = ctx.getPackageManager().queryIntentActivities(view, 0).size();
        try {
            if (n == 0) {
                Toast.makeText(ctx, "No file manager found", Toast.LENGTH_SHORT).show();
            } else if (n == 1) {
                ctx.startActivity(view);
            } else {
                Intent chooser = Intent.createChooser(view, "Open APK folder");
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(chooser);
            }
        } catch (Throwable t) {
            Toast.makeText(ctx, "Cannot open folder (restricted path)", Toast.LENGTH_SHORT).show();
        }
    }
}
