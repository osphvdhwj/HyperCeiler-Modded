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
package com.harry.hyperhand.hook.module.hook.systemui.plugin;

import static com.harry.hyperhand.hook.module.base.tool.HookTool.hookMethod;
import static com.harry.hyperhand.hook.utils.PropUtils.getProp;

import com.harry.hyperhand.hook.module.base.dexkit.DexKit;
import com.harry.hyperhand.hook.module.base.dexkit.IDexKit;
import com.harry.hyperhand.hook.module.base.tool.HookTool;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;
import org.luckypray.dexkit.result.base.BaseData;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;

public class ShowDeviceName {

    static String deviceName = getProp("persist.sys.device_name");

    public static void initShowDeviceName(ClassLoader classLoader) {
        Method method = DexKit.findMember("OnCarrierTextChanged", new IDexKit() {
            @Override
            public BaseData dexkit(DexKitBridge bridge) throws ReflectiveOperationException {
                MethodData methodData = bridge.findMethod(FindMethod.create()
                        .matcher(MethodMatcher.create()
                                .name("onCarrierTextChanged")
                        )).singleOrNull();
                return methodData;
            }
        });
        hookMethod(method, new HookTool.MethodHook() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) throws Throwable {
                param.args[0] = deviceName;
            }
        });
    }
}

