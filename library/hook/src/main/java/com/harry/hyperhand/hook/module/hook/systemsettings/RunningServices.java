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
package com.harry.hyperhand.hook.module.hook.systemsettings;

import android.content.ComponentName;
import android.content.Intent;

import com.harry.hyperhand.hook.module.base.BaseHook;

public class RunningServices extends BaseHook {
    @Override
    public void init() throws NoSuchMethodException {
        findAndHookMethod("com.android.settings.SettingsActivity",
            "getStartingFragmentClass", Intent.class,
            new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    Intent intent = (Intent) param.args[0];
                    ComponentName componentName = intent.getComponent();
                    if (componentName != null) {
                        String className = componentName.getClassName();
                        if ("com.android.settings.RunningServices".equals(className)) {
                            param.setResult("com.android.settings.applications.RunningServices");
                        }
                    }
                }
            }
        );
    }
}
