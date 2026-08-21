package com.sevtinge.hyperceiler.hook.module.hook.systemui.statusbar

import android.content.Context
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import de.robv.android.xposed.XposedHelpers
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook
import kotlin.math.abs

object StatusbarBrightnessControl : BaseHook() {

    override fun init() {
        loadClass("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView").methodFinder()
            .filterByName("dispatchTouchEvent")
            .single().createHook {
                before {
                    val view = it.thisObject as ViewGroup
                    val event = it.args[0] as MotionEvent

                    val action = event.actionMasked

                    if (action == MotionEvent.ACTION_DOWN) {
                        XposedHelpers.setAdditionalInstanceField(view, "brightnessStartX", event.rawX)
                        XposedHelpers.setAdditionalInstanceField(view, "brightnessStartY", event.rawY)
                        XposedHelpers.setAdditionalInstanceField(view, "isBrightnessSliding", false)
                        
                        try {
                            val context = view.context
                            val currentBrightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                            XposedHelpers.setAdditionalInstanceField(view, "startBrightness", currentBrightness)
                        } catch (e: Exception) {
                            XposedHelpers.setAdditionalInstanceField(view, "startBrightness", 128)
                        }
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        val startX = XposedHelpers.getAdditionalInstanceField(view, "brightnessStartX") as? Float ?: 0f
                        val startY = XposedHelpers.getAdditionalInstanceField(view, "brightnessStartY") as? Float ?: 0f
                        var isSliding = XposedHelpers.getAdditionalInstanceField(view, "isBrightnessSliding") as? Boolean ?: false

                        val diffX = event.rawX - startX
                        val diffY = event.rawY - startY

                        if (!isSliding && abs(diffX) > 50f && abs(diffX) > abs(diffY) * 2) {
                            isSliding = true
                            XposedHelpers.setAdditionalInstanceField(view, "isBrightnessSliding", true)
                        }

                        if (isSliding) {
                            val startBrightness = XposedHelpers.getAdditionalInstanceField(view, "startBrightness") as? Int ?: 128
                            val context = view.context
                            
                            val screenWidth = context.resources.displayMetrics.widthPixels
                            val brightnessDiff = (diffX / screenWidth) * 255f
                            var newBrightness = (startBrightness + brightnessDiff).toInt()
                            if (newBrightness < 1) newBrightness = 1
                            if (newBrightness > 255) newBrightness = 255
                            
                            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, newBrightness)
                            
                            it.result = true // consume event
                        }
                    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        val isSliding = XposedHelpers.getAdditionalInstanceField(view, "isBrightnessSliding") as? Boolean ?: false
                        if (isSliding) {
                            it.result = true // consume event
                        }
                    }
                }
            }
    }
}
