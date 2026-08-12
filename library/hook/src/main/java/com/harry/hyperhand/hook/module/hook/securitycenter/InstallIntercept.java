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
package com.harry.hyperhand.hook.module.hook.securitycenter;

import com.harry.hyperhand.hook.module.base.BaseHook;
import com.harry.hyperhand.hook.module.base.dexkit.DexKit;
import com.harry.hyperhand.hook.module.base.dexkit.IDexKit;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.base.BaseData;

import java.lang.reflect.Method;

public class InstallIntercept extends BaseHook {
    @Override
    public void init() throws NoSuchMethodException {
        long stime = System.currentTimeMillis();
        Method method = DexKit.findMember("install", new IDexKit() {
            @Override
            public BaseData dexkit(DexKitBridge bridge) {
                MethodData methodData = bridge.findMethod(FindMethod.create()
                        .matcher(MethodMatcher.create()
                                .usingStrings("permcenter_install_intercept_enabled")
                                .returnType(boolean.class)
                        )).singleOrNull();
                return methodData;
            }
        });

        long etime = System.currentTimeMillis();
        logE(TAG, "time: " + (etime - stime));
        logE(TAG, "find: " + method);

        hookMethod(method,
                new MethodHook() {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(false);
                    }
                }
        );
    }
}
