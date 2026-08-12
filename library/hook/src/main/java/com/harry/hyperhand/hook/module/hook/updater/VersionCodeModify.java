/*
  * This file is part of HyperHand.

  * HyperHand is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.

  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <https://www.gnu.org/licenses/>.

  * Copyright (C) 2023-2025 HyperHand Contributions
*/
package com.harry.hyperhand.hook.module.hook.updater;

import android.os.Build;
import android.text.TextUtils;

import com.harry.hyperhand.hook.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

public class VersionCodeModify extends BaseHook {

    Class<?> mApplication;

    @Override
    public void init() {

        mApplication = findClassIfExists("com.android.updater.Application");

        findAndHookMethod(mApplication, "onCreate", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                String mVersionCode = mPrefsMap.getString("various_updater_miui_version", "V14.0.22.11.26.DEV");
                if (!TextUtils.isEmpty(mVersionCode)) {
                    XposedHelpers.setStaticObjectField(Build.VERSION.class, "INCREMENTAL", mVersionCode);
                }
            }
        });
    }
}
