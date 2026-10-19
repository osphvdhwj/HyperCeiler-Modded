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
package com.sevtinge.hyperceiler.hook.module.hook.home;

import android.content.Context;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import com.sevtinge.hyperceiler.hook.utils.devicesdk.DisplayUtils;

public class WidgetCornerRadius extends BaseHook {

    Context mContext;

    @Override
    public void init() {

        Class<?> maMlClass = findClassIfExists("com.miui.home.launcher.maml.MaMlHostView");
        if (maMlClass != null) {
            hookAllConstructors(maMlClass, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    if (param.args[0] instanceof Context) mContext = (Context) param.args[0];
                }
            });
            hookAllMethods(maMlClass, "computeRoundedCornerRadius", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.setResult((float) DisplayUtils.dp2px(mPrefsMap.getInt("home_widget_corner_radius", 0)));
                }
            });
        }

        Class<?> appWidgetClass = findClassIfExists("com.miui.home.launcher.LauncherAppWidgetHostView");
        if (appWidgetClass != null) {
            hookAllConstructors(appWidgetClass, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    if (param.args[0] instanceof Context) mContext = (Context) param.args[0];
                }
            });
            hookAllMethods(appWidgetClass, "computeRoundedCornerRadius", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.setResult((float) DisplayUtils.dp2px(mPrefsMap.getInt("home_widget_corner_radius", 0)));
                }
            });
        }
    }
}
