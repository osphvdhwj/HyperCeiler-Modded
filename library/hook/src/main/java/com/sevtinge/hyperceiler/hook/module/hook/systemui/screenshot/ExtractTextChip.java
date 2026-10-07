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
package com.sevtinge.hyperceiler.hook.module.hook.systemui.screenshot;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import com.sevtinge.hyperceiler.hook.utils.bridge.AvesBridge;

import de.robv.android.xposed.XposedHelpers;

/**
 * Adds an "Extract text" chip to the screenshot preview toolbar.
 *
 * Injects after ScreenshotView.setChipIntents(SavedImageData) runs, so
 * the chip lands next to the standard share/edit/scroll chips. Click
 * runs OCR on the saved screenshot via AvesBridge.ocr() and copies the
 * result to the system clipboard.
 */
public class ExtractTextChip extends BaseHook {

    private static final String SYSTEMUI_PKG = "com.android.systemui";
    private static final String SCREENSHOT_VIEW =
            "com.android.systemui.screenshot.ScreenshotView";
    private static final String SAVED_IMAGE_DATA =
            "com.android.systemui.screenshot.ScreenshotController$SavedImageData";

    @Override
    public void init() {
        Class<?> cls = findClassIfExists(SCREENSHOT_VIEW, lpparam.classLoader);
        if (cls == null) return;

        Class<?> savedCls = findClassIfExists(SAVED_IMAGE_DATA, lpparam.classLoader);
        if (savedCls == null) return;

        findAndHookMethodSilently(cls, "setChipIntents", savedCls, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                try {
                    Object saved = (param.args != null && param.args.length > 0)
                            ? param.args[0] : null;
                    if (saved == null) return;

                    Uri uri = (Uri) XposedHelpers.getObjectField(saved, "uri");
                    if (uri == null) return;

                    View view = (param.thisObject instanceof View)
                            ? (View) param.thisObject : null;
                    if (view == null) return;

                    Context ctx = view.getContext();
                    if (ctx == null) return;

                    LinearLayout actionsView =
                            (LinearLayout) XposedHelpers.getObjectField(view, "mActionsView");
                    if (actionsView == null) return;

                    int layoutId = ctx.getResources()
                            .getIdentifier("overlay_action_chip", "layout", SYSTEMUI_PKG);
                    if (layoutId == 0) return;

                    View chip = LayoutInflater.from(ctx).inflate(layoutId, actionsView, false);
                    if (chip == null) return;

                    try {
                        XposedHelpers.callMethod(chip, "setText", "Extract text");
                    } catch (Throwable ignored) { }
                    chip.setAlpha(1.0f);
                    chip.setOnClickListener(v -> runOcr(ctx, uri));

                    int insertAt = Math.max(0, actionsView.getChildCount() - 1);
                    actionsView.addView(chip, insertAt);
                } catch (Throwable ignored) {
                    // never break the screenshot flow
                }
            }
        });
    }

    private void runOcr(Context ctx, Uri uri) {
        String text;
        try {
            text = AvesBridge.ocr(ctx, uri.toString());
        } catch (Throwable t) {
            text = null;
        }
        if (text == null || text.trim().isEmpty()) {
            boolean installed;
            try {
                installed = AvesBridge.isAvailable(ctx);
            } catch (Throwable t) {
                installed = false;
            }
            Toast.makeText(ctx,
                    installed ? "No text found" : "AVES+ Tools not installed",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ClipboardManager cm =
                    (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("ocr", text));
            Toast.makeText(ctx, "Text copied (" + text.length() + " chars)",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable ignored) { }
    }
}
