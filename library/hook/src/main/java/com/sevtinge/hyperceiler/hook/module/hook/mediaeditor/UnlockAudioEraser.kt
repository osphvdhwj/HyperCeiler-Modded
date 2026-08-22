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
            val aiFeatures = listOf(
                "audio_eraser", "audioEraser", "ai_audio_eraser",
                "audio_denoise", "audioDenoise",
                "voice_eraser", "voiceEraser",
                "audio_separation", "audioSeparation", "audio_track_separation",
                "sound_eraser", "soundEraser", "ai_audio", "*"
            )

            // 1. Hook AISupportItem constructors across MediaEditor & Gallery
            for (className in listOf(
                "com.miui.mediaeditor.aigc.AISupportItem",
                "com.miui.gallery.editor.aigc.AISupportItem",
                "com.miui.gallery.editor.photo.app.aigc.AISupportItem"
            )) {
                findClassIfExists(className)?.let { clazz ->
                    hookAllConstructors(clazz, object : MethodHook() {
                        override fun before(param: MethodHookParam) {
                            val listIdx = param.args.indexOfFirst { it is List<*> }
                            if (listIdx != -1) {
                                @Suppress("UNCHECKED_CAST")
                                val origList = param.args[listIdx] as? List<String> ?: emptyList()
                                val newList = ArrayList(origList).apply { addAll(aiFeatures) }
                                param.args[listIdx] = newList
                            }
                        }
                    })
                }
            }

            // 2. Hook MediaEditorApiHelper AI audio capability methods
            findClassIfExists("com.miui.mediaeditor.api.MediaEditorApiHelper")?.let { helperClass ->
                val audioMethods = listOf(
                    "isAudioEraserAvailable", "isAudioDenoiseAvailable",
                    "isAudioTrackSeparationAvailable", "isAiAudioAvailable"
                )
                audioMethods.forEach { methodName ->
                    runCatching {
                        findAndHookMethod(helperClass, methodName, object : MethodHook() {
                            override fun before(param: MethodHookParam) {
                                param.result = true
                            }
                        })
                    }
                }
            }

            // 3. DexKit method finding for boolean capability getters across HyperOS 1.0 & 2.0
            val audioMethods = DexKit.findMemberList<Method>("AudioEraserMethods") { bridge ->
                bridge.findMethod(FindMethod.create().matcher(
                    MethodMatcher.create().usingStrings(
                        "audio_eraser", "audioEraser", "ai_audio_eraser",
                        "audio_denoise", "audioDenoise",
                        "voice_eraser", "voiceEraser",
                        "audio_separation", "audioSeparation", "audio_track_separation",
                        "sound_eraser", "soundEraser",
                        "AudioTrackSeparate", "AudioTrackSeparation",
                        "isAudioEraserSupported", "isAudioTrackSeparationSupport"
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
