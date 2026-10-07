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
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

/**
 * "Remove hardcoded gallery" — strip the Xiaomi Gallery targeting from
 * the camera's post-capture REVIEW intents, so Android resolves them
 * naturally and the user's default gallery app (Aves, Google Photos,
 * anything) handles them instead.
 *
 * HyperOS 2's MiuiCamera sends the "review the shot you just took"
 * intent with a hardcoded component/package pointing at com.miui.gallery
 * and a MIUI-specific action. If MiuiGallery isn't installed or the user
 * prefers a different gallery, the intent falls on the floor.
 *
 * Hook targets (caller side):
 *   Activity.startActivity(Intent)
 *   Activity.startActivity(Intent, Bundle)
 *   Activity.startActivityForResult(Intent, int)
 *   Activity.startActivityForResult(Intent, int, Bundle)
 *   ContextWrapper.startActivity(Intent)
 *   ContextWrapper.startActivity(Intent, Bundle)
 *
 * Behaviour per Intent:
 *   - If action == "com.android.camera.action.REVIEW" OR the target
 *     package is com.miui.gallery:
 *       * setAction(Intent.ACTION_VIEW)
 *       * setPackage(null)
 *       * setComponent(null)
 *   - Any other intent passes through untouched.
 *
 * Every reflective call is guarded so a missing overload on a future
 * build is a silent no-op, never a crash.
 */
public class UnhardcodeGallery extends BaseHook {

    private static final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
    private static final String MIUI_GALLERY = "com.miui.gallery";

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

        boolean isReviewAction = REVIEW_ACTION.equals(intent.getAction());
        boolean isMiuiGallery = false;

        ComponentName comp = intent.getComponent();
        if (comp != null && MIUI_GALLERY.equals(comp.getPackageName())) {
            isMiuiGallery = true;
        }
        if (!isMiuiGallery && MIUI_GALLERY.equals(intent.getPackage())) {
            isMiuiGallery = true;
        }

        if (!isReviewAction && !isMiuiGallery) return;

        intent.setAction(Intent.ACTION_VIEW);
        intent.setPackage(null);
        intent.setComponent(null);
    }

    private void hookAll(Class<?> cls) {
        if (cls == null) return;
        findAndHookMethodSilently(cls, "startActivity", Intent.class, mHook);
        findAndHookMethodSilently(cls, "startActivity", Intent.class, android.os.Bundle.class, mHook);
        findAndHookMethodSilently(cls, "startActivityForResult", Intent.class, int.class, mHook);
        findAndHookMethodSilently(cls, "startActivityForResult", Intent.class, int.class, android.os.Bundle.class, mHook);
    }

    @Override
    public void init() {
        hookAll(Activity.class);
        hookAll(ContextWrapper.class);
    }
}
