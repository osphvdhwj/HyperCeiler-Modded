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
package com.harry.hyperhand.hook.module.hook.securitycenter.battery;

import static com.harry.hyperhand.hook.module.base.tool.HookTool.MethodHook.returnConstant;

import com.harry.hyperhand.hook.module.base.BaseHook;
import com.harry.hyperhand.hook.module.base.dexkit.DexKit;
import com.harry.hyperhand.hook.module.base.dexkit.IDexKitList;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.BaseDataList;
import org.luckypray.dexkit.result.MethodDataList;

import java.lang.reflect.Method;
import java.util.List;

public class UnlockSmartCharge extends BaseHook {
    @Override
    public void init() throws NoSuchMethodException {
        List<Method> methods = DexKit.findMemberList("VendorSmartChg", new IDexKitList() {
            @Override
            public BaseDataList dexkit(DexKitBridge bridge) throws ReflectiveOperationException {
                MethodDataList methodData = bridge.findMethod(FindMethod.create()
                        .matcher(MethodMatcher.create()
                                .usingStrings("persist.vendor.smartchg")
                        )
                );
                return methodData;
            }
        });
        for (Method method : methods) {
            hookMethod(method, returnConstant(true));
        }
    }
}
