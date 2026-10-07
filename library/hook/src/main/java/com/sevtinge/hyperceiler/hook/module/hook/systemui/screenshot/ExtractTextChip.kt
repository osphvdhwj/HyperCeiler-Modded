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
package com.sevtinge.hyperceiler.hook.module.hook.systemui.screenshot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.bridge.AvesBridge
import de.robv.android.xposed.XposedHelpers

/**
 * Adds an "Extract text" chip to the screenshot preview toolbar.
 *
 * The screenshot preview is a HorizontalScrollView (R.id.actions_container)
 * whose children are OverlayActionChip instances. After setChipIntents()
 * populates the standard chips (share / edit / scroll / quick-share), we
 * append our own chip that:
 *
 *   1. Reads the saved screenshot URI from SavedImageData.uri.
 *   2. Calls AVES+ Tools via AvesBridge.ocr().
 *   3. Copies the extracted text to the system clipboard.
 *   4. Toasts the outcome.
 *
 * If AVES+ is not installed, the chip still appears but its click shows
 * "no provider" — this makes the missing dependency visible instead of
 * silently doing nothing.
 */
object ExtractTextChip : BaseHook() {

    private const val SYSTEMUI_PKG = "com.android.systemui"
    private const val SCREENSHOT_VIEW = "com.android.systemui.screenshot.ScreenshotView"
    private const val SAVED_IMAGE_DATA = "com.android.systemui.screenshot.ScreenshotController\$SavedImageData"

    override fun init() {
        val cls = findClassIfExists(SCREENSHOT_VIEW, lpparam.classLoader) ?: return

        findAndHookMethod(
            cls, "setChipIntents", SAVED_IMAGE_DATA,
            object : MethodHook() {
                override fun after(param: MethodHookParam) {
                    try {
                        injectChip(param)
                    } catch (_: Throwable) {
                        // never break the screenshot flow
                    }
                }
            }
        )
    }

    private fun injectChip(param: BaseHook.MethodHookParam) {
        val saved = param.args.firstOrNull() ?: return
        val uri = XposedHelpers.getObjectField(saved, "uri") as? Uri ?: return
        val view = param.thisObject as? View ?: return
        val ctx: Context = view.context ?: return

        val actionsView = XposedHelpers.getObjectField(view, "mActionsView") as? LinearLayout ?: return

        val layoutId = ctx.resources.getIdentifier("overlay_action_chip", "layout", SYSTEMUI_PKG)
        if (layoutId == 0) return

        val chip = LayoutInflater.from(ctx).inflate(layoutId, actionsView, false) ?: return
        XposedHelpers.callMethod(chip, "setText", "Extract text")
        XposedHelpers.callMethod(chip, "setAlpha", 1.0f)
        chip.setOnClickListener { runOcr(ctx, uri) }

        // Insert before the trailing spacer (matches how SmartActions chips are added)
        val insertAt = (actionsView.childCount - 1).coerceAtLeast(0)
        actionsView.addView(chip, insertAt)
    }

    private fun runOcr(ctx: Context, uri: Uri) {
        val text = try {
            AvesBridge.ocr(ctx, uri.toString())
        } catch (_: Throwable) {
            null
        }
        if (text.isNullOrBlank()) {
            Toast.makeText(
                ctx,
                if (AvesBridge.isAvailable(ctx)) "No text found" else "AVES+ Tools not installed",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        try {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("ocr", text))
            Toast.makeText(ctx, "Text copied (${text.length} chars)", Toast.LENGTH_SHORT).show()
        } catch (_: Throwable) {
        }
    }
}
