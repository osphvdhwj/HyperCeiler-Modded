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
package com.sevtinge.hyperceiler.hook.module.hook.home.recent

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

object HideStatusBarWhenEnterRecent : BaseHook() {
    override fun init() {
        // The MiuiHome build on this ROM (Redmi Note 12 Pro+ 5G, HyperOS 2.0.2.0 port)
        // no longer contains DeviceLevelUtils, and the method signature on
        // DeviceConfig has changed. Skip silently on those ROMs.

        runCatching {
            loadClass("com.miui.home.launcher.common.DeviceLevelUtils").methodFinder()
                .filterByName("isHideStatusBarWhenEnterRecents")
                .single().createHook {
                    returnConstant(true)
                }
        }.onFailure {
            logD(TAG, lpparam.packageName, "DeviceLevelUtils absent; skipping: $it")
        }

        runCatching {
            loadClass("com.miui.home.launcher.DeviceConfig").methodFinder()
                .filterByName("keepStatusBarShowingForBetterPerformance")
                .single().createHook {
                    returnConstant(false)
                }
        }.onFailure {
            logD(TAG, lpparam.packageName, "keepStatusBarShowingForBetterPerformance absent; skipping: $it")
        }
    }
}
