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
package com.harry.hyperhand.hook.module.hook.systemui.plugin

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.harry.hyperhand.hook.utils.callMethod
import com.harry.hyperhand.hook.utils.getBooleanField
import com.harry.hyperhand.hook.utils.getFloatField
import com.harry.hyperhand.hook.utils.getObjectField
import com.harry.hyperhand.hook.utils.getObjectFieldAs
import com.harry.hyperhand.hook.utils.log.XposedLogUtils.logD
import com.harry.hyperhand.hook.utils.replaceMethod
import com.harry.hyperhand.hook.utils.setLongField
import com.harry.hyperhand.hook.utils.setObjectField
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClass
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createAfterHook
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createBeforeHook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.get

object StartCollpasedColumnPress {
    fun initLoaderHook(classLoader: ClassLoader) {
        val miuiVolumeDialogView by lazy {
            loadClass("com.android.systemui.miui.volume.MiuiVolumeDialogView", classLoader)
        }
        val miuiVolumeDialogMotion by lazy {
            loadClass("com.android.systemui.miui.volume.MiuiVolumeDialogMotion", classLoader)
        }
        val miuiVolumeSeekBar by lazy {
            loadClass("com.android.systemui.miui.volume.MiuiVolumeSeekBar", classLoader)
        }

        var longClick = false
        var longPressJob: Job? = null

        fun View.startScaleAnimation() {
            longClick = true
            animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(300)
                .start()
        }

        fun View.stopScaleAnimation() {
            longClick = false
            animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300)
                .start()
        }

        miuiVolumeDialogView.methodFinder().apply {
            filterByName("onFinishInflate")
                .first().createAfterHook {
                    it.thisObject.getObjectFieldAs<View>("mExpandButton").apply {
                        alpha = 0f
                        isClickable = false
                        visibility = View.GONE
                        setOnClickListener(null)
                    }

                }
            filterByName("notifyAccessibilityChanged")
                .filterByParamTypes {
                    it[0] == Boolean::class.java
                }.first().createAfterHook {
                    it.thisObject.getObjectFieldAs<View>("mExpandButton").apply {
                        isClickable = false
                        visibility = View.GONE
                        setOnClickListener(null)
                    }
                }
        }

        miuiVolumeDialogMotion.methodFinder().apply {
            filterByName("lambda\$processExpandTouch\$1")
                .first().createBeforeHook {
                    it.thisObject.setObjectField("mIsExpandButton",true)
                }
        }
        miuiVolumeSeekBar.methodFinder()
            .filterByName("onTouchEvent")
            .filterByParamTypes {
                it[0] == MotionEvent::class.java
            }.first().createAfterHook {
                val mSeekBarOnclickListener = it.thisObject.getObjectField("mSeekBarOnclickListener")
                val mSeekBarAnimListener = it.thisObject.getObjectField("mSeekBarAnimListener")!!
                val volumePanelViewController = mSeekBarAnimListener.getObjectField("this\$0")!!
                val mVolumeView = volumePanelViewController.getObjectFieldAs<View>("mVolumeView")

                it.thisObject.setLongField("mCurrentMS",0L)
                if (mSeekBarOnclickListener != null) {
                    val motionEvent = it.args?.get(0) as MotionEvent
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            if ( !volumePanelViewController.getBooleanField("mExpanded") ){
                                longPressJob = CoroutineScope(Dispatchers.Main).launch {
                                    mVolumeView.startScaleAnimation()
                                    delay(300)
                                    val mMoveY = it.thisObject.getFloatField("mMoveY")
                                    if (longClick && mMoveY < 10f){
                                        mVolumeView.apply {
                                            scaleY = 1f
                                            scaleX = 1f
                                            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                        }
                                        mSeekBarOnclickListener.callMethod( "onClick")

                                    }

                                }
                            }
                        }
                        MotionEvent.ACTION_UP->{
                            mVolumeView.stopScaleAnimation()
                            longPressJob?.cancel()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            mVolumeView.stopScaleAnimation()
                            longPressJob?.cancel()
                        }
                    }

                }
            }

    }
}
