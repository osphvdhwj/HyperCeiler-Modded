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
package com.harry.hyperhand.hook.module.skip;

import static com.harry.hyperhand.hook.BuildConfig.DEBUG;

import android.os.Build;

import com.harry.hyperhand.hook.module.hook.systemframework.corepatch.CorePatchForB;
import com.harry.hyperhand.hook.module.hook.systemframework.corepatch.CorePatchForR;
import com.harry.hyperhand.hook.module.hook.systemframework.corepatch.CorePatchForS;
import com.harry.hyperhand.hook.module.hook.systemframework.corepatch.CorePatchForT;
import com.harry.hyperhand.hook.module.hook.systemframework.corepatch.CorePatchForU;
import com.harry.hyperhand.hook.module.hook.systemframework.corepatch.CorePatchForV;
import com.harry.hyperhand.hook.utils.log.XposedLogUtils;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SystemFrameworkForCorePatch implements IXposedHookLoadPackage {
    public static final String TAG = "CorePatch";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (("android".equals(lpparam.packageName)) && (lpparam.processName.equals("android"))) {
            if (DEBUG)
                XposedLogUtils.logD(TAG, "android", "handleLoadPackage: Current sdk version " + Build.VERSION.SDK_INT);
            switch (Build.VERSION.SDK_INT) {
                case Build.VERSION_CODES.BAKLAVA -> // 36
                        new CorePatchForB().handleLoadPackage(lpparam);
                case Build.VERSION_CODES.VANILLA_ICE_CREAM -> // 35
                        new CorePatchForV().handleLoadPackage(lpparam);
                case Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> // 34
                        new CorePatchForU().handleLoadPackage(lpparam);
                case Build.VERSION_CODES.TIRAMISU -> // 33
                        new CorePatchForT().handleLoadPackage(lpparam);
                case Build.VERSION_CODES.S_V2 -> // 32
                        new CorePatchForS().handleLoadPackage(lpparam);
                case Build.VERSION_CODES.S -> // 31
                        new CorePatchForS().handleLoadPackage(lpparam);
                case Build.VERSION_CODES.R -> // 30
                        new CorePatchForR().handleLoadPackage(lpparam);
                default -> {
                    new CorePatchForB().handleLoadPackage(lpparam);
                    XposedLogUtils.logW(TAG, "android", "Unsupported Version of Android sdk version " + Build.VERSION.SDK_INT);
                }
            }
        }
    }

}
