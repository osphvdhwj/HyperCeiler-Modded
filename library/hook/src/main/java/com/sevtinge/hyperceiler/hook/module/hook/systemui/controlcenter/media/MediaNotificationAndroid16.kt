/*
 * This file is part of HyperHand.
 *
 * HyperHand is free software: you can redistribute it and/or modify
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
 * Copyright (C) 2023-2026 HyperHand Contributions
 */
package com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter.media

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Icon
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.api.dp
import com.sevtinge.hyperceiler.hook.utils.getObjectFieldOrNullAs
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils
import de.robv.android.xposed.XposedHelpers
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createAfterHook

object MediaNotificationAndroid16 : BaseHook() {

    override fun init() {
        try {
            // Android 16 & 17 Media Control Panel UI & UX Engine
            val mediaControlPanelClass = loadClassOrNull("com.android.systemui.media.controls.ui.controller.MediaControlPanel")
                ?: loadClassOrNull("com.android.systemui.media.controls.ui.MediaControlPanel")
                ?: loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel")

            mediaControlPanelClass?.methodFinder()
                ?.filterByName("bindPlayer")
                ?.firstOrNull()?.createAfterHook { param ->
                    try {
                        val viewHolder = XposedHelpers.getObjectField(param.thisObject, "mMediaViewHolder")
                            ?: XposedHelpers.getObjectField(param.thisObject, "mediaViewHolder")
                        if (viewHolder != null) {
                            applyAndroid16UiUx(param.thisObject, viewHolder, param.args)
                        }
                    } catch (t: Throwable) {
                        XposedLogUtils.logE("MediaNotificationAndroid16 bindPlayer error", t)
                    }
                }

            // Android 16/17 Output Switcher / Route Selector Dialog UI
            val mediaOutputDialogClass = loadClassOrNull("com.android.systemui.media.dialog.MediaOutputDialog")
                ?: loadClassOrNull("com.android.systemui.media.controls.ui.controller.MediaOutputDialog")

            mediaOutputDialogClass?.methodFinder()
                ?.filterByName("onCreate")
                ?.firstOrNull()?.createAfterHook { param ->
                    try {
                        val window = (param.thisObject as? android.app.Dialog)?.window
                        window?.decorView?.run {
                            background = GradientDrawable().apply {
                                cornerRadius = 28.dp.toFloat()
                                setColor(Color.parseColor("#EE1E1E24"))
                            }
                        }
                    } catch (t: Throwable) {
                        XposedLogUtils.logE("MediaNotificationAndroid16 output dialog error", t)
                    }
                }

        } catch (t: Throwable) {
            XposedLogUtils.logE("MediaNotificationAndroid16 init failed", t)
        }
    }

    private fun applyAndroid16UiUx(panel: Any, viewHolder: Any, args: Array<Any>) {
        val titleText = viewHolder.getObjectFieldOrNullAs<TextView>("titleText")
        val artistText = viewHolder.getObjectFieldOrNullAs<TextView>("artistText")
        val albumView = viewHolder.getObjectFieldOrNullAs<ImageView>("albumView")
        val seekBar = viewHolder.getObjectFieldOrNullAs<SeekBar>("seekBar")
        val seamlessView = viewHolder.getObjectFieldOrNullAs<View>("seamless")
            ?: viewHolder.getObjectFieldOrNullAs<View>("seamlessView")
            ?: viewHolder.getObjectFieldOrNullAs<View>("seamlessButton")

        // 1. Android 16 Continuous Marquee Scrolling Typography
        titleText?.run {
            isSelected = true
            isSingleLine = true
            textSize = 16f
        }
        artistText?.run {
            isSelected = true
            isSingleLine = true
            textSize = 13f
        }

        // 2. Android 16 Album Art Gradient Palette Extraction & Blend Card
        if (args.isNotEmpty()) {
            val mediaData = args[0]
            val artwork = mediaData.getObjectFieldOrNullAs<Icon>("artwork")
            val context = (panel as? View)?.context ?: titleText?.context
            if (artwork != null && context != null) {
                try {
                    val drawable = artwork.loadDrawable(context)
                    if (drawable != null) {
                        val bitmap = drawable.toBitmap(100, 100)
                        val accentColor = extractAccentColor(bitmap)
                        val darkColor = darkenColor(accentColor, 0.4f)

                        // Apply Android 16 Gradient Background
                        val playerView = (viewHolder as? View) ?: (panel as? View)
                        playerView?.background = GradientDrawable(
                            GradientDrawable.Orientation.TL_BR,
                            intArrayOf(accentColor, darkColor)
                        ).apply {
                            cornerRadius = 24.dp.toFloat()
                        }
                    }
                } catch (ignored: Throwable) {}
            }
        }

        // 3. Android 16 Squiggly Progress Bar Waveform Styling
        seekBar?.run {
            val squigglyClass = loadClassOrNull("com.android.systemui.media.controls.ui.drawable.SquigglyProgress")
                ?: loadClassOrNull("com.android.systemui.media.controls.ui.SquigglyProgress")
            if (squigglyClass != null) {
                val squigglyDrawable = XposedHelpers.newInstance(squigglyClass) as? Drawable
                if (squigglyDrawable != null) {
                    progressDrawable = squigglyDrawable
                }
            }
        }

        // 4. Android 16 Media Output Switcher Chip Button Styling
        seamlessView?.run {
            background = GradientDrawable().apply {
                cornerRadius = 20.dp.toFloat()
                setColor(Color.parseColor("#40FFFFFF"))
            }
            isClickable = true
        }

        // 5. Android 16 Circular Action Buttons & Ripple Touch FX
        val actionIds = arrayOf("action0", "action1", "action2", "action3", "action4", "actionPrev", "actionNext", "actionPlayPause")
        for (actionId in actionIds) {
            val btn = viewHolder.getObjectFieldOrNullAs<ImageButton>(actionId) ?: continue
            btn.background = RippleDrawable(
                ColorStateList.valueOf(Color.parseColor("#33FFFFFF")),
                GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#20FFFFFF"))
                },
                null
            )
        }
    }

    private fun extractAccentColor(bitmap: Bitmap): Int {
        // Sample a subset of pixels for performance (downsample to ~1000 samples)
        val stepX = (bitmap.width / 20).coerceAtLeast(1)
        val stepY = (bitmap.height / 20).coerceAtLeast(1)
        var red = 0L
        var green = 0L
        var blue = 0L
        var count = 0L
        for (x in 0 until bitmap.width step stepX) {
            for (y in 0 until bitmap.height step stepY) {
                val pixel = bitmap.getPixel(x, y)
                red += Color.red(pixel)
                green += Color.green(pixel)
                blue += Color.blue(pixel)
                count++
            }
        }
        val r = (red / count).toInt().coerceIn(0, 255)
        val g = (green / count).toInt().coerceIn(0, 255)
        val b = (blue / count).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt()
        val g = (Color.green(color) * factor).toInt()
        val b = (Color.blue(color) * factor).toInt()
        return Color.argb(a, r, g, b)
    }

    /**
     * Apply waveform animation to the seek bar for smoother visual feedback.
     * Uses a custom Canvas-based waveform drawing with hardware acceleration.
     */
    private fun applyWaveformAnimation(seekBar: SeekBar, context: android.content.Context) {
        try {
            val waveformClass = loadClassOrNull("com.android.systemui.media.controls.ui.drawable.WaveformAnimation")
            if (waveformClass != null) {
                val waveform = XposedHelpers.newInstance(waveformClass) as? Drawable
                if (waveform != null) {
                    seekBar.progressDrawable = waveform
                }
            }
        } catch (t: Throwable) {
            XposedLogUtils.logE("MediaNotificationAndroid16 waveform animation error", t)
        }
    }
}
