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

/**
 * Unlock 4K60 recording.
 *
 * Two build families are known:
 *
 *   6.x — the gate is a private boolean with an Iterator.hasNext() call
 *         in the caller's scope, and the number 60 in the signature.
 *         DexKit still finds it.
 *
 *   5.1.001370 — the gate lives in
 *         com.android.camera.data.data.config.ComponentConfigVideoQuality
 *         as `private boolean isSupport60FPS(int w, int h, CameraCapabilities caps)`.
 *         DexKit cannot find it because the query relied on the 6.x
 *         caller-graph shape.
 *
 * Strategy: try DexKit first (works on 6.x), fall back to the direct
 * 5.1 method on any DexKit failure.
 */
object Unlock4k60 : BaseHook() {

    override fun init() {
        if (!tryDexKit()) {
            tryDirect()
        }
    }

    private fun tryDexKit(): Boolean = try {
        val m: Method = DexKit.findMember("4k60") {
            it.findMethod {
                matcher {
                    paramCount = 3
                    returnType = "boolean"
                    usingNumbers(60)
                    addInvoke("Ljava/util/Iterator;->hasNext()Z")
                }
            }.single()
        }
        m.createHook { returnConstant(true) }
        true
    } catch (t: Throwable) {
        XposedBridge.log("[HyperHand][Unlock4k60] DexKit miss, trying direct hook: ${t.message}")
        false
    }

    private fun tryDirect() {
        val capsCls = findClassIfExists("com.android.camera2.CameraCapabilities", lpparam.classLoader)
            ?: return
        findAndHookMethodSilently(
            "com.android.camera.data.data.config.ComponentConfigVideoQuality",
            lpparam.classLoader,
            "isSupport60FPS",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            capsCls,
            object : MethodHook() {
                override fun before(param: MethodHookParam) {
                    param.result = true
                }
            }
        )
    }
}
