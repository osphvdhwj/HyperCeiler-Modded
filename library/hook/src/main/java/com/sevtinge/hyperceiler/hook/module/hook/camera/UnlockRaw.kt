package com.sevtinge.hyperceiler.hook.module.hook.camera

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.dexkit.DexKit
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook
import java.lang.reflect.Method

object UnlockRaw : BaseHook() {
    private val rawMethods: List<Method> by lazy {
        val names = listOf(
            "isSupportRaw",
            "isSupportCaptureRaw10",
            "isSupportSatRawSize",
            "isSupportUltraPixelRaw",
            "isMultipleRawHdrSupported",
            "isUltraPixelRawPhotographySupported"
        )
        val list = mutableListOf<Method>()
        names.forEach { methodName ->
            val method = DexKit.findMember<Method?>("UnlockRaw_$methodName") {
                it.findMethod {
                    matcher {
                        name = methodName
                        returnType = "boolean"
                    }
                }.firstOrNull()
            }
            if (method != null) {
                list.add(method)
            }
        }
        list
    }

    override fun init() {
        rawMethods.forEach { method ->
            method.createHook {
                returnConstant(true)
            }
        }
    }
}
