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
package com.sevtinge.hyperceiler.hook.module.hook.securityadd;

import android.app.Activity;
import android.os.Bundle;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * Blocks the SecurityAdd ad landing page.
 *
 * TransWebActivity is the WebView container that SecurityAddService opens
 * for every "recommended app" tap and other ad surfaces. Short-circuiting
 * onCreate makes the click a no-op: the ad URL is never loaded.
 */
public class BlockSecurityAddAdPages extends BaseHook {

    @Override
    public void init() {
        Class<?> cls = findClassIfExists("com.miui.ad.TransWebActivity");
        if (cls == null) {
            logE(TAG, lpparam.packageName, "TransWebActivity not found");
            return;
        }
        findAndHookMethod(cls, "onCreate", Bundle.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                try {
                    ((Activity) param.thisObject).finish();
                } catch (Throwable ignored) { }
                param.setResult(null);
            }
        });
    }
}
