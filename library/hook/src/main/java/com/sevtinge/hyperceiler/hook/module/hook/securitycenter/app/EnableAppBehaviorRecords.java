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

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * The ROM's fragment ApplicationsDetailsFragment removes the "App behavior records"
 * preference (app_behavior_pref) whenever mc.c.k() returns false. On this ROM
 * mc.c.k() is hard-coded to return false, so the entry never appears.
 * Force it to true so the preference is shown; its click handler routes to
 * com.miui.permcenter.privacycenter.usage.AppPermissionUsageActivity which exists.
 */
public class EnableAppBehaviorRecords extends BaseHook {

    @Override
    public void init() {
        Class<?> mcC = findClassIfExists("mc.c");
        if (mcC == null) {
            logE(TAG, lpparam.packageName, "mc.c not found");
            return;
        }
        findAndHookMethod(mcC, "k", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                param.setResult(true);
            }
        });
    }
}
