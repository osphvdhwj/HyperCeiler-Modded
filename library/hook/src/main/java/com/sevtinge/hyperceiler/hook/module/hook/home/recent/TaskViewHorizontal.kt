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

import android.graphics.RectF
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.callMethod
import com.sevtinge.hyperceiler.hook.utils.callStaticMethod
import com.sevtinge.hyperceiler.hook.utils.findClass
import com.sevtinge.hyperceiler.hook.utils.hookAfterMethod

object TaskViewHorizontal : BaseHook() {
    override fun init() {

        try {
            "com.miui.home.recents.views.TaskStackViewsAlgorithmHorizontal".findClass().declaredMethods.forEach { method ->
                com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logI("HyperHand-Explore", "Found method in TaskStackViewsAlgorithmHorizontal: " + method.name)
            }
        } catch (e: Exception) {}

        try {
            "com.miui.home.launcher.RecentsAndFsGestureUtils".findClass().declaredMethods.forEach { method ->
                com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logI("HyperHand-Explore", "Found method in RecentsAndFsGestureUtils: " + method.name)
            }
        } catch (e: Exception) {}
        
        try {
            "com.miui.home.recents.RecentsModel".findClass().declaredMethods.forEach { method ->
                com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logI("HyperHand-Explore", "Found method in RecentsModel: " + method.name)
            }
        } catch (e: Exception) {}
}
