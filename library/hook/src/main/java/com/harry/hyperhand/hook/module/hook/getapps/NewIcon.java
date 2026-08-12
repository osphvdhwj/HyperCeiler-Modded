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
package com.harry.hyperhand.hook.module.hook.getapps;


import com.harry.hyperhand.hook.module.base.BaseHook;

import java.lang.reflect.Method;


public class NewIcon extends BaseHook {
    static Method isDesktopSupportOperationIcon;

    @Override
    public void init() {
       /* try {
            List<DexMethodDescriptor> result = Objects.requireNonNull(MarketDexKit.mMarketResultMethodsMap.get("DesktopSupportOperationIcon"));
            for (DexMethodDescriptor descriptor : result) {
                isDesktopSupportOperationIcon = descriptor.getMethodInstance(lpparam.classLoader);
                logI("isDesktopSupportOperationIcon method is " + isDesktopSupportOperationIcon);
                if (isDesktopSupportOperationIcon.getReturnType() == boolean.class) {
                    XposedBridge.hookMethod(isDesktopSupportOperationIcon, XC_MethodReplacement.returnConstant(false));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        hookAllMethods("com.xiaomi.market.util.FileUtils", "ensureExternalIconDir", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                param.setResult(false);
            }
        });*/
    }
}
