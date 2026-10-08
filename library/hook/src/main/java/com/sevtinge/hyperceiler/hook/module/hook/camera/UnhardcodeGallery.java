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
package com.sevtinge.hyperceiler.hook.module.hook.camera;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * "Remove hardcoded gallery" — strip the Xiaomi Gallery targeting from
 * the camera's post-capture review intents so Android resolves them
 * naturally and the user's default gallery app handles them.
 *
 * Verified constants in MiuiCamera.apk classes.dex (HyperOS 2):
 *   com.android.camera.action.REVIEW
 *   com.miui.camera.action.REVIEW
 *   com.android.camera.action.SPILIT_SCREEN_REVIEW  (typo is Xiaomi's)
 *   com.android.camera.action.CROP
 *   target package: com.miui.gallery
 *
 * Match rule: any REVIEW-ish action (or any intent explicitly targeting
 * com.miui.gallery) gets rewritten to ACTION_VIEW with the package and
 * component cleared. CROP is deliberately NOT touched — it needs its
 * own dedicated handling and is out of scope for this toggle.
 *
 * All reflective calls use findAndHookMethodSilently so a missing
 * overload on a future build is a silent no-op, never a crash.
 */
public class UnhardcodeGallery extends BaseHook {

    private static final String MIUI_GALLERY = "com.miui.gallery";

    /** Match any action containing "REVIEW" (case-sensitive, matches all
     *  three variants Xiaomi ships) OR the exact crop action which we
     *  intentionally leave alone. */
    private static boolean isReviewAction(String action) {
        if (action == null) return false;
        if (action.contains("SPILIT_SCREEN_REVIEW")) return true; // Xiaomi typo — treat same
        if ("com.android.camera.action.REVIEW".equals(action)) return true;
        if ("com.miui.camera.action.REVIEW".equals(action)) return true;
        return false;
    }

    private final MethodHook mHook = new MethodHook() {
        @Override
        protected void before(MethodHookParam param) {
            for (Object a : param.args) {
                if (!(a instanceof Intent)) continue;
                rewrite((Intent) a);
                return;
            }
        }
    };

    private static void rewrite(Intent intent) {
        if (intent == null) return;

        boolean shouldRewrite = false;

        if (isReviewAction(intent.getAction())) {
            shouldRewrite = true;
        }

        ComponentName comp = intent.getComponent();
        if (comp != null && MIUI_GALLERY.equals(comp.getPackageName())) {
            shouldRewrite = true;
        }
        if (MIUI_GALLERY.equals(intent.getPackage())) {
            shouldRewrite = true;
        }

        if (!shouldRewrite) return;

        intent.setAction(Intent.ACTION_VIEW);
        intent.setPackage(null);
        intent.setComponent(null);
    }

    private void hookAll(Class<?> cls) {
        if (cls == null) return;
        findAndHookMethodSilently(cls, "startActivity", Intent.class, mHook);
        findAndHookMethodSilently(cls, "startActivity", Intent.class, Bundle.class, mHook);
        findAndHookMethodSilently(cls, "startActivityForResult", Intent.class, int.class, mHook);
        findAndHookMethodSilently(cls, "startActivityForResult", Intent.class, int.class, Bundle.class, mHook);
    }

    @Override
    public void init() {
        hookAll(Activity.class);
        hookAll(ContextWrapper.class);
    }
}
