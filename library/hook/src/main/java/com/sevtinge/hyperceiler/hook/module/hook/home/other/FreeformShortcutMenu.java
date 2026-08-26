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
package com.sevtinge.hyperceiler.hook.module.hook.home.other;

import static com.sevtinge.hyperceiler.hook.module.base.tool.OtherTool.getModuleRes;
import static com.sevtinge.hyperceiler.hook.utils.devicesdk.MiDeviceAppUtilsKt.isPad;
import static de.robv.android.xposed.XposedHelpers.callMethod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.sevtinge.hyperceiler.hook.R;
import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class FreeformShortcutMenu extends BaseHook {

    Class<?> mActivity;
    Class<?> mViewDarkModeHelper;
    Class<?> mSystemShortcutMenu;
    Class<?> mSystemShortcutMenuItem;
    Class<?> mAppShortcutMenu;
    Class<?> mShortcutMenuItem;
    Class<?> mAppDetailsShortcutMenuItem;
    Class<?> mActivityUtilsCompat;
    Class<?> mRecentsAndFSGestureUtils;

    Context mContext;

    XC_MethodHook.Unhook mShortCutMenuItemHook;

    @Override
    public void init() {

        if (isPad()) {
            hookAllMethods("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem$SmallWindowShortcutMenuItem", "isValid",
                MethodHook.returnConstant(true));
            return;
        }

        mActivity = Activity.class;
        mViewDarkModeHelper = findClassIfExists("com.miui.home.launcher.util.ViewDarkModeHelper");
        mSystemShortcutMenu = findClassIfExists("com.miui.home.launcher.shortcuts.SystemShortcutMenu");
        mSystemShortcutMenuItem = findClassIfExists("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem");
        mAppShortcutMenu = findClassIfExists("com.miui.home.launcher.shortcuts.AppShortcutMenu");
        mShortcutMenuItem = findClassIfExists("com.miui.home.launcher.shortcuts.ShortcutMenuItem");
        mAppDetailsShortcutMenuItem = findClassIfExists("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem$AppDetailsShortcutMenuItem");
        mActivityUtilsCompat = findClassIfExists("com.miui.launcher.utils.ActivityUtilsCompat");
        mRecentsAndFSGestureUtils = findClassIfExists("com.miui.home.launcher.RecentsAndFSGestureUtils");

        try {

            hookAllMethods(mViewDarkModeHelper, "onConfigurationChanged", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    XposedHelpers.callStaticMethod(mSystemShortcutMenuItem, "createAllSystemShortcutMenuItems");
                }
            });

            hookAllMethods(mShortcutMenuItem, "getShortTitle", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    if (param.getResult().equals("应用信息")) {
                        param.setResult("信息");
                    }
                    if (param.getResult().equals("新建窗口")) {
                        param.setResult("多开");
                    }
                }
            });

            hookAllMethods(mActivity, "onCreate", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    mContext = (Context) param.thisObject;
                }
            });

            findAndHookMethod(mAppDetailsShortcutMenuItem, "getOnClickListener", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    Resources modRes = getModuleRes(mContext);
                    Object obj = param.thisObject;
                    CharSequence mShortTitle = (CharSequence) callMethod(obj, "getShortTitle");

                    if (mShortTitle.equals(modRes.getString(R.string.share_center))) {
                        XposedHelpers.callStaticMethod(mRecentsAndFSGestureUtils, "startWorld", mContext);
                    } else if (mShortTitle.equals(modRes.getString(R.string.floating_window))) {
                        param.setResult(getFreeformOnClickListener(obj, false));
                    } else if (mShortTitle.equals(modRes.getString(R.string.new_task))) {
                        param.setResult(getFreeformOnClickListener(obj, true));
                    } else if (mShortTitle.equals(modRes.getString(R.string.force_stop))) {
                        param.setResult(getForceStopOnClickListener(obj));
                    }
                }
            });

            hookAllMethods(mSystemShortcutMenu, "getMaxShortcutItemCount", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(6);
                }
            });

            hookAllMethods(mAppShortcutMenu, "getMaxShortcutItemCount", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(6);
                }
            });

            hookAllMethods(mSystemShortcutMenuItem, "createAllSystemShortcutMenuItems", new MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void after(MethodHookParam param) throws Throwable {

                    Resources modRes = getModuleRes(mContext);

                    List mAllSystemShortcutMenuItems = (List) XposedHelpers.getStaticObjectField(mSystemShortcutMenuItem, "sAllSystemShortcutMenuItems");

                    Object mSmallWindowInstance = XposedHelpers.newInstance(mAppDetailsShortcutMenuItem);
                    Object mNewTasksInstance = XposedHelpers.newInstance(mAppDetailsShortcutMenuItem);
                    Object mForceStopInstance = XposedHelpers.newInstance(mAppDetailsShortcutMenuItem);

                    if (mPrefsMap.getBoolean("home_other_freeform_shortcut_menu")) {
                        callMethod(mSmallWindowInstance, "setShortTitle", modRes.getString(R.string.floating_window));
                        callMethod(mSmallWindowInstance, "setIconDrawable", ContextCompat.getDrawable(mContext, mContext.getResources().getIdentifier("ic_task_small_window", "drawable", mContext.getPackageName())));
                    }
                    if (mPrefsMap.getBoolean("home_other_tasks_shortcut_menu")) {
                        callMethod(mNewTasksInstance, "setShortTitle", modRes.getString(R.string.new_task));
                        callMethod(mNewTasksInstance, "setIconDrawable", ContextCompat.getDrawable(mContext, mContext.getResources().getIdentifier("ic_task_add_pair", "drawable", mContext.getPackageName())));
                    }
                    if (mPrefsMap.getBoolean("home_other_force_stop_shortcut_menu")) {
                        callMethod(mForceStopInstance, "setShortTitle", modRes.getString(R.string.force_stop));
                        callMethod(mForceStopInstance, "setIconDrawable", ContextCompat.getDrawable(mContext, android.R.drawable.ic_menu_close_clear_cancel));
                    }

                    ArrayList sAllSystemShortcutMenuItems = new ArrayList();
                    if (mPrefsMap.getBoolean("home_other_force_stop_shortcut_menu"))
                        sAllSystemShortcutMenuItems.add(mForceStopInstance);
                    if (mPrefsMap.getBoolean("home_other_freeform_shortcut_menu"))
                        sAllSystemShortcutMenuItems.add(mSmallWindowInstance);
                    if (mPrefsMap.getBoolean("home_other_tasks_shortcut_menu"))
                        sAllSystemShortcutMenuItems.add(mNewTasksInstance);
                    sAllSystemShortcutMenuItems.addAll(mAllSystemShortcutMenuItems);
                    XposedHelpers.setStaticObjectField(mSystemShortcutMenuItem, "sAllSystemShortcutMenuItems", sAllSystemShortcutMenuItems);
                }
            });

        } catch (Throwable th) {
            logW(TAG, "FreeformShortcutMenu", th);
        }
    }


    private View.OnClickListener getFreeformOnClickListener(Object obj, boolean isNewTaskOnClick) {
        return view -> {
            Intent intent = new Intent();
            Context mContext1 = view.getContext();
            ComponentName mComponentName = (ComponentName) callMethod(obj, "getComponentName", new Object[0]);
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setComponent(mComponentName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isNewTaskOnClick) {
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
            Object makeFreeformActivityOptions = XposedHelpers.callStaticMethod(mActivityUtilsCompat, "makeFreeformActivityOptions", mContext1, mComponentName.getPackageName());

            if (makeFreeformActivityOptions != null) {
                mContext1.startActivity(intent, (Bundle) callMethod(makeFreeformActivityOptions, "toBundle", new Object[0]));
            }
        };
    }

    private View.OnClickListener getForceStopOnClickListener(Object obj) {
        return view -> {
            try {
                Context mContext1 = view.getContext();
                ComponentName mComponentName = (ComponentName) callMethod(obj, "getComponentName", new Object[0]);
                String packageName = mComponentName.getPackageName();

                // Execute Hyper Hand (Hail) Root force stop
                new Thread(() -> {
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", "am force-stop " + packageName});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                try {
                    android.app.ActivityManager am = (android.app.ActivityManager) mContext1.getSystemService(Context.ACTIVITY_SERVICE);
                    java.lang.reflect.Method forceStopPackageMethod = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
                    forceStopPackageMethod.setAccessible(true);
                    forceStopPackageMethod.invoke(am, packageName);
                } catch (Exception ignored) {}

                android.widget.Toast.makeText(mContext1, "Hyper Hand force stopped: " + packageName, android.widget.Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
