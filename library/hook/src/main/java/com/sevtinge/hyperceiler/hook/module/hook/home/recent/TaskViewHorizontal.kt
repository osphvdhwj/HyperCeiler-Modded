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
            val iosMode = mPrefsMap.getBoolean("home_recent_ios_mode")
            if (iosMode) {
                val scrollProgress = it.args[1] as Float
                val transformOut = it.args[2]!!
                
                // Get rect and scale
                val rectF = transformOut.javaClass.getDeclaredField("rect").apply { isAccessible = true }.get(transformOut) as RectF
                val scaleField = transformOut.javaClass.getDeclaredField("scale").apply { isAccessible = true }
                val currentScale = scaleField.getFloat(transformOut)
                
                val scalePref = mPrefsMap.getInt("home_recent_ios_scale", 90).toFloat() / 100f
                val overlapDp = mPrefsMap.getInt("home_recent_ios_overlap", 30).toFloat()
                
                // iOS Stacking: Cards to the left (background) overlap and scale down
                if (scrollProgress < 0f) {
                    val progressFloat = Math.abs(scrollProgress)
                    
                    // Scale down background cards exponentially based on how far back they are
                    val newScale = currentScale * Math.pow(scalePref.toDouble(), progressFloat.toDouble()).toFloat()
                    scaleField.setFloat(transformOut, newScale)
                    
                    // Non-linear offset to create a true stacking effect where cards bunch up
                    // We use a diminishing returns formula so they don't just spread out linearly
                    val stackFactor = (1f - Math.pow(0.7, progressFloat.toDouble())).toFloat() * 2.5f
                    val offset = overlapDp * stackFactor * 3f * progressFloat
                    rectF.offset(offset, 0f)

                    // Add alpha fading for background cards to mimic iOS depth
                    try {
                        val alphaField = transformOut.javaClass.getDeclaredField("alpha").apply { isAccessible = true }
                        val currentAlpha = alphaField.getFloat(transformOut)
                        val newAlpha = Math.max(0.0f, currentAlpha - (progressFloat * 0.15f))
                        alphaField.setFloat(transformOut, newAlpha)
                    } catch (e: Exception) {
                        // Ignore if alpha field is not accessible
                    }

                    // Optional: Adjust translationZ to ensure correct overlapping order
                    try {
                        val zField = transformOut.javaClass.getDeclaredField("translationZ").apply { isAccessible = true }
                        zField.setFloat(transformOut, -progressFloat * 10f)
                    } catch (e: Exception) {
                        // Ignore if translationZ is not accessible
                    }
                }
            }
        }
    }
}
