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
package com.harry.hyperhand.hook.module.app;

import com.hchen.database.HookBase;
import com.harry.hyperhand.hook.module.base.BaseModule;
import com.harry.hyperhand.hook.module.hook.mtb.BypassAuthentication;
import com.harry.hyperhand.hook.module.hook.mtb.IsUserBuild;

@HookBase(targetPackage = "com.xiaomi.mtb")
public class Mtb extends BaseModule {
    @Override
    public void handleLoadPackage() {
        initHook(BypassAuthentication.INSTANCE, mPrefsMap.getBoolean("mtb_auth"));
        initHook(IsUserBuild.INSTANCE, mPrefsMap.getBoolean("mtb_auth"));
    }
}
