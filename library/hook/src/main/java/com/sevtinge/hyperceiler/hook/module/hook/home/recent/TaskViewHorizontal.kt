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

        "com.miui.home.recents.views.TaskStackViewsAlgorithmHorizontal".hookAfterMethod(
            "getTaskViewTransform", Int::class.javaPrimitiveType, Float::class.javaPrimitiveType, "com.miui.home.recents.views.TaskViewTransform".findClass()
        ) {
            val iosMode = mPrefsMap.getBoolean("prefs_key_home_recent_ios_mode", false)
            if (iosMode) {
                val scrollProgress = it.args[1] as Float
                val transformOut = it.args[2]!!
                
                // Get rect and scale
                val rectF = transformOut.javaClass.getDeclaredField("rect").apply { isAccessible = true }.get(transformOut) as RectF
                val scaleField = transformOut.javaClass.getDeclaredField("scale").apply { isAccessible = true }
                val currentScale = scaleField.getFloat(transformOut)
                
                val scalePref = mPrefsMap.getInt("prefs_key_home_recent_ios_scale", 90).toFloat() / 100f
                val overlapDp = mPrefsMap.getInt("prefs_key_home_recent_ios_overlap", 30).toFloat()
                
                // iOS Stacking: Cards to the left (background) overlap and scale down
                if (scrollProgress < 0f) {
                    val progressInt = Math.abs(scrollProgress)
                    
                    // Scale down background cards exponentially based on how far back they are
                    val newScale = currentScale * Math.pow(scalePref.toDouble(), progressInt.toDouble()).toFloat()
                    scaleField.setFloat(transformOut, newScale)
                    
                    // Offset to the right to create an overlap effect
                    // dp approximation, multiplying by 3 for density factor
                    val offset = overlapDp * progressInt * 3f 
                    rectF.offset(offset, 0f)
                }
            }
        }
    }
}
