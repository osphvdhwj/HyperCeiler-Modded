package com.harry.hyperhand.hook.module.hook.camera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.hchen.database.HookBase
import com.harry.hyperhand.hook.module.base.BaseHook
import com.harry.hyperhand.hook.utils.log.AndroidLogUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

@HookBase(targetPackage = "com.android.camera")
object OpenDefaultGallery : BaseHook() {
    override fun init() {
        XposedHelpers.findAndHookMethod(
            Activity::class.java,
            "startActivityForResult",
            Intent::class.java,
            Int::class.javaPrimitiveType,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val intent = param.args[0] as? Intent ?: return
                    val cmp = intent.component
                    val pkg = intent.`package`
                    
                    if (cmp?.packageName == "com.miui.gallery" || pkg == "com.miui.gallery") {
                        AndroidLogUtils.logI("HyperHand", "Intercepted MIUI Camera gallery intent: \$intent")
                        
                        intent.component = null
                        intent.`package` = null
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        
                        if (intent.action == "com.miui.gallery.action.VIEW_PHOTO") {
                            intent.action = Intent.ACTION_VIEW
                        }
                    }
                }
            }
        )
    }
}
