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
package com.sevtinge.hyperceiler.hook.module.hook.various;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import java.util.HashMap;
import java.util.Map;

/**
 * Redirect intents that target a stock Xiaomi/system app to any other
 * app (typically a Google equivalent or a user build like AVES Gallery).
 *
 * The redirect map is user-editable in Settings → Various → Package
 * Redirect. Each line is a "source=target" pair:
 *
 *     com.miui.notes=com.google.android.keep
 *     com.miui.player=com.google.android.apps.youtube.music
 *     com.android.fileexplorer=com.google.android.documentsui
 *     com.miui.gallery=com.aves.gallery
 *     com.android.browser=com.android.chrome
 *
 * Blank lines and lines starting with '#' are ignored.
 *
 * The hook is installed in every process the module scopes to; it
 * rewrites intents at the caller side before dispatch. If the target
 * package is not installed the intent is left untouched so the stock
 * app can still handle it.
 */
public class UnhardcodeNotes extends BaseHook {

    private static final String PREF_MAP = "various_pkg_redirect_map";

    /** default used when the user map is empty but the toggle is on */
    private static final String DEFAULT_MAP =
            "com.miui.notes=com.google.android.keep";

    private static Map<String, String> parseMap(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw == null) return m;
        for (String line : raw.split("\\r?\\n")) {
            String s = line.trim();
            if (s.isEmpty() || s.startsWith("#")) continue;
            int eq = s.indexOf('=');
            if (eq <= 0 || eq == s.length() - 1) continue;
            String src = s.substring(0, eq).trim();
            String dst = s.substring(eq + 1).trim();
            if (!src.isEmpty() && !dst.isEmpty()) m.put(src, dst);
        }
        return m;
    }

    private boolean rewrite(Intent intent, Context ctx) {
        if (intent == null) return false;

        ComponentName comp = intent.getComponent();
        String target = (comp != null) ? comp.getPackageName() : intent.getPackage();
        if (target == null) return false;

        String replacement = currentMap().get(target);
        if (replacement == null || replacement.equals(target)) return false;

        if (ctx == null) return false;
        if (!isInstalled(ctx, replacement)) return false;

        intent.setPackage(replacement);
        if (intent.getComponent() != null) intent.setComponent(null);
        return true;
    }

    private Map<String, String> currentMap() {
        String raw;
        try {
            raw = mPrefsMap.getString(PREF_MAP, "");
        } catch (Throwable t) {
            raw = "";
        }
        if (raw == null || raw.trim().isEmpty()) raw = DEFAULT_MAP;
        return parseMap(raw);
    }

    private static boolean isInstalled(Context ctx, String pkg) {
        try {
            ctx.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Context ctxOf(Object thisObject) {
        if (thisObject instanceof Context) return (Context) thisObject;
        if (thisObject instanceof Activity) return (Activity) thisObject;
        return null;
    }

    private final MethodHook mHook = new MethodHook() {
        @Override
        protected void before(MethodHookParam param) {
            for (Object a : param.args) {
                if (a instanceof Intent) {
                    rewrite((Intent) a, ctxOf(param.thisObject));
                    return;
                }
            }
        }
    };

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
