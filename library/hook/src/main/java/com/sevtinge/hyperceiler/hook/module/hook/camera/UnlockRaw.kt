package com.sevtinge.hyperceiler.hook.module.hook.camera

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method

object UnlockRaw : BaseHook() {
    override fun init() {
        val classNames = listOf(
            "com.mi.device.DataItemFeature",
            "com.android.camera.data.data.config.DataItemConfig",
            "com.android.camera.CameraSettings"
        )
        
        val targetMethods = listOf(
            "isSupportRaw",
            "isSupportCaptureRaw10",
            "isSupportSatRawSize",
            "isSupportUltraPixelRaw",
            "isMultipleRawHdrSupported",
            "isUltraPixelRawPhotographySupported"
        )

        classNames.forEach { className ->
            try {
                val clazz = XposedHelpers.findClass(className, lpparam.classLoader)
                val methods = clazz.declaredMethods
                methods.forEach { method ->
                    if (targetMethods.contains(method.name) && method.returnType == Boolean::class.javaPrimitiveType) {
                        method.createHook {
                            returnConstant(true)
                        }
                    }
                }
            } catch (e: Throwable) {
                // Ignore if class not found
            }
        }
    }
}
