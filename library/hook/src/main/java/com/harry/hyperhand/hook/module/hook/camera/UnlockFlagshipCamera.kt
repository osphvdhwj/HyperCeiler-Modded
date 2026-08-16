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
package com.harry.hyperhand.hook.module.hook.camera

import com.harry.hyperhand.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

object UnlockFlagshipCamera : BaseHook() {
    override fun init() {
        runCatching {
            val cameraSettings = loadClass("com.android.camera.CameraSettings")
            
            // 4K Super Night Video
            cameraSettings.methodFinder()
                .filterByName("isSuperNightVideo4kSupported")
                .firstOrNull()?.createHook { returnConstant(true) }
                
            // Cinematic Photo Mode
            cameraSettings.methodFinder()
                .filterByName("isCinematicPhotoSupported")
                .firstOrNull()?.createHook { returnConstant(true) }
                
            // HDR10+ / Dolby Vision Pro Video
            cameraSettings.methodFinder()
                .filterByName("isVideoHdr10ProModeSupported")
                .firstOrNull()?.createHook { returnConstant(true) }
                
            cameraSettings.methodFinder()
                .filterByName("isVideoHdr10PlusModeSupported")
                .firstOrNull()?.createHook { returnConstant(true) }
                
            // Ultra Pixel RAW (10-bit RAW for 50MP/108MP)
            cameraSettings.methodFinder()
                .filterByName("isUltraPixelRawSupportedByTele")
                .firstOrNull()?.createHook { returnConstant(true) }
                
            cameraSettings.methodFinder()
                .filterByName("isUltraPixelRawSupportedByWide")
                .firstOrNull()?.createHook { returnConstant(true) }
                
        }.onFailure {
            logE(TAG, this.lpparam.packageName, "UnlockFlagshipCamera initialization failed: $it")
        }
    }
}
