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
import com.sevtinge.hyperceiler.hook.utils.callStaticMethod
import com.sevtinge.hyperceiler.hook.utils.findClass
import com.sevtinge.hyperceiler.hook.utils.replaceMethod
import io.github.kyuubiran.ezxhelper.xposed.EzXposed.appContext

object TaskViewVertical : BaseHook() {
    override fun init() {

        val value = mPrefsMap.getInt("home_recent_vertical_task_view_card_size", 100).toFloat() / 100f
        if (value == -1f || value == 1f) return
        
        val algorithmCls = findClassIfExists("com.miui.home.recents.views.TaskStackViewsAlgorithmVertical") ?: return
        val utilitiesCls = findClassIfExists("com.miui.home.recents.util.Utilities") ?: return
        
        try {
            algorithmCls.replaceMethod("scaleTaskView", RectF::class.java) {
                val rect = it.args[0] as? RectF ?: return@replaceMethod null
                val scaleObj = utilitiesCls.callStaticMethod("getTaskViewScale", appContext) as? Number
                val baseScale = scaleObj?.toFloat() ?: 1f
                utilitiesCls.callStaticMethod("scaleRectAboutCenter", rect, value * baseScale)
                null
            }
        } catch (t: Throwable) {
            // Ignore if method signature changed on port ROM
        }

    }
}
