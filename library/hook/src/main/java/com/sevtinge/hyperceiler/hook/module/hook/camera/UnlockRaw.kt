package com.sevtinge.hyperceiler.hook.module.hook.camera

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.dexkit.DexKit
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook
import java.lang.reflect.Method

object UnlockRaw : BaseHook() {
    private val rawMethods by lazy<List<Method>> {
        DexKit.findMember("UnlockRaw") {
            it.findMethod {
                matcher {
                    nameRegex = "^(isSupportRaw|isSupportCaptureRaw10|isSupportSatRawSize|isSupportUltraPixelRaw|isMultipleRawHdrSupported|isUltraPixelRawPhotographySupported)$"
                    returnType = "boolean"
                }
            }
        }
    }

    override fun init() {
        rawMethods.forEach { method ->
            method.createHook {
                returnConstant(true)
            }
        }
    }
}
