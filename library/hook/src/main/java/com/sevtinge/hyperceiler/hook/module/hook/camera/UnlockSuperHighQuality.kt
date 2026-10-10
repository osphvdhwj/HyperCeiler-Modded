/*
 * This file is part of HyperCeiler.
 *
 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023-2025 HyperCeiler Contributions
 */
package com.sevtinge.hyperceiler.hook.module.hook.camera

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.dexkit.DexKit
import de.robv.android.xposed.XposedBridge
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Unlock "super high quality" picture mode.
 *
 * Two paths:
 *   DexKit — the original query matched a 0-param boolean that lived
 *            inside a caller referencing pref_camera_jpegquality_key.
 *            Works on 6.x, fails on 5.1.001370 because the call-graph
 *            shape changed.
 *
 *   Direct — com.android.camera.CameraSettings.isSupportHighQualityPreferred()
 *            is the 0-param gate the settings UI reads on 5.1. Forcing it
 *            true is equivalent to the DexKit hook on 6.x.
 *
 * Strategy: try DexKit, fall back to the direct gate.
 */
object UnlockSuperHighQuality : BaseHook() {

    override fun init() {
        if (!tryDexKit()) {
            tryDirect()
        }
    }

    private fun tryDexKit(): Boolean = try {
        val m: Method = DexKit.findMember("SuperHighQuality") {
            it.findMethod {
                matcher {
                    addCaller {
                        declaredClass {
                            usingEqStrings("pref_camera_jpegquality_key")
                        }
                        modifiers = Modifier.STATIC or Modifier.PUBLIC
                        addInvoke("Landroid/content/res/Resources;->getString(I)Ljava/lang/String;")
                    }
                    paramCount = 0
                    returnType = "boolean"
                }
            }.single()
        }
        m.createHook { returnConstant(true) }
        true
    } catch (t: Throwable) {
        XposedBridge.log("[HyperHand][UnlockSuperHighQuality] DexKit miss, trying direct hook: ${t.message}")
        false
    }

    private fun tryDirect() {
        // 5.1.001370 removed the 0-param CameraSettings.isSupportHighQualityPreferred().
        // The real capability gate is the 1-param version on CameraCapabilitiesUtil
        // (same pattern as UnlockTrackEyes hooking isSupportTrackEye). Hook both
        // namespaces — the missing one is a harmless no-op on the wrong build.
        val okCaps = hookAllMethodsBoolean(
            "com.android.camera2.CameraCapabilitiesUtil",
            "isSupportHighQualityPreferred",
            object : MethodHook() {
                override fun before(param: MethodHookParam) {
                    param.result = true
                }
            }
        )
        val okSettings = findAndHookMethodSilently(
            "com.android.camera.CameraSettings",
            lpparam.classLoader,
            "isSupportHighQualityPreferred",
            object : MethodHook() {
                override fun before(param: MethodHookParam) {
                    param.result = true
                }
            }
        )
        XposedBridge.log(
            "[HyperHand][UnlockSuperHighQuality] direct hooks: " +
                    "CameraCapabilitiesUtil=" + okCaps + " CameraSettings=" + okSettings
        )
    }
}
