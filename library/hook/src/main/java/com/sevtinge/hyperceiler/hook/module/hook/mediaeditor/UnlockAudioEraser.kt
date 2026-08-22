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

package com.sevtinge.hyperceiler.hook.module.hook.mediaeditor

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.dexkit.DexKit
import org.luckypray.dexkit.query.FindMethod
import org.luckypray.dexkit.query.matchers.MethodMatcher
import java.lang.reflect.Method

object UnlockAudioEraser : BaseHook() {
    override fun init() {
        runCatching {
            // Hook AISupportItem constructors in MediaEditor and Gallery
            for (className in listOf(
                "com.miui.mediaeditor.aigc.AISupportItem",
                "com.miui.gallery.editor.aigc.AISupportItem",
                "com.miui.gallery.editor.photo.app.aigc.AISupportItem"
            )) {
                findClassIfExists(className)?.let { clazz ->
                    hookAllConstructors(clazz, object : MethodHook() {
                        override fun before(param: MethodHookParam) {
                            if (param.args.size > 1 && param.args[1] is MutableList<*>) {
                                @Suppress("UNCHECKED_CAST")
                                val list = param.args[1] as MutableList<String>
                                list.addAll(listOf("audio_eraser", "audio_denoise", "voice_eraser", "audio_separation", "sound_eraser", "ai_audio", "*"))
                            }
                        }
                    })
                }
            }

            // Hook Audio Eraser / Denoise / Separation capability getters
            val audioMethods = DexKit.findMemberList<Method>("AudioEraserMethods") { bridge ->
                bridge.findMethod(FindMethod.create().matcher(
                    MethodMatcher.create().usingStrings(
                        "audio_eraser", "audio_denoise", "voice_eraser", "audio_separation", "sound_eraser", "AudioTrackSeparate"
                    ).returnType(Boolean::class.javaPrimitiveType ?: java.lang.Boolean.TYPE)
                ))
            }

            audioMethods.forEach { method ->
                hookMethod(method, object : MethodHook() {
                    override fun after(param: MethodHookParam) {
                        param.result = true
                    }
                })
            }
        }.onFailure { logE(TAG, lpparam.packageName, it) }
    }
}
