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
package com.sevtinge.hyperceiler.hook.module.hook.home.title;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

public class DownloadAnimation extends BaseHook {
    @Override
    public void init() {
        // All three targets use Silently variants: on HyperOS 2 the older
        // DeviceLevelUtils class is gone, CpuLevelUtils covers modern builds,
        // and any missing variant is a no-op rather than a crash.
        hookAllMethodsSilently("com.miui.home.launcher.common.DeviceLevelUtils",
                "needMamlProgressIcon", MethodHook.returnConstant(true));
        hookAllMethodsSilently("com.miui.home.launcher.common.DeviceLevelUtils",
                "needRemoveDownloadAnimationDevice", MethodHook.returnConstant(false));
        hookAllMethodsSilently("com.miui.home.launcher.common.CpuLevelUtils",
                "needMamlDownload", MethodHook.returnConstant(true));
    }
}
