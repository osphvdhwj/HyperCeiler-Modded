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

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

/**
 * Redirect intents that target a Xiaomi system app to a Google
 * (or any user-installed) equivalent.
 *
 * Rationale: Xiaomi's stock apps (Notes, Music, Files, Browser, …)
 * register for standard system intents (VIEW / EDIT / SEND / PICK).
 * When another app launches one of those intents, Android resolves it
 * to the Xiaomi app. This hook rewrites the intent at the caller side
 * before it is dispatched, so the replacement app is used instead.
 *
 * Coverage:
 *   - Activity.startActivity(Intent)
 *   - Activity.startActivity(Intent, Bundle)
 *   - Activity.startActivityForResult(Intent, int)
 *   - Activity.startActivityForResult(Intent, int, Bundle)
 *   - ContextWrapper.startActivity(Intent)
 *   - ContextWrapper.startActivity(Intent, Bundle)
 *     (this catches Service, Application, ReceiverRestrictedContext…)
 *
 * Safety:
 *   - Only rewrites when the target package is in the redirect map.
 *   - Only rewrites when the *replacement* package is actually
 *     installed on the device; otherwise the intent is left alone so
 *     the stock app can handle it.
 *   - Never throws: all reflective calls are guarded.
 */
public class UnhardcodeNotes extends BaseHook {

    /**
     * source package → replacement package.
     * Kept minimal on purpose; other entries can be added later and
     * gated by their own prefs.
     */
    private static final String[][] REDIRECT = {
            { "com.miui.notes", "com.google.android.keep" },
    };

    private static Boolean sKeepInstalled = null;

    /** return true if any intent arg was rewritten. */
    private static boolean rewrite(Intent intent, Context ctx) {
        if (intent == null) return false;

        String target = null;
        ComponentName comp = intent.getComponent();
        if (comp != null) {
            target = comp.getPackageName();
        } else {
            target = intent.getPackage();
        }
        if (target == null) return false;

        for (String[] pair : REDIRECT) {
            if (!pair[0].equals(target)) continue;

            // Only rewrite if the replacement is installed.
            if (ctx == null) return false;
            Boolean installed = isInstalled(ctx, pair[1]);
            if (!Boolean.TRUE.equals(installed)) return false;

            intent.setPackage(pair[1]);
            // Clear a fully-qualified component so the new app can
            // resolve the action/category on its own.
            if (intent.getComponent() != null) {
                intent.setComponent(null);
            }
            return true;
        }
        return false;
    }

    private static boolean isInstalled(Context ctx, String pkg) {
        try {
            ctx.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** get Context from a hooked method's thisObject if it is one. */
    private static Context ctxOf(Object thisObject) {
        if (thisObject instanceof Context) return (Context) thisObject;
        if (thisObject instanceof Activity) return (Activity) thisObject;
        return null;
    }

    private MethodHook mStartActivity = new MethodHook() {
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

    private void hookStartActivityVariants(Class<?> cls) {
        if (cls == null) return;
        findAndHookMethodSilently(cls, "startActivity", Intent.class, mStartActivity);
        findAndHookMethodSilently(cls, "startActivity", Intent.class, android.os.Bundle.class, mStartActivity);
        findAndHookMethodSilently(cls, "startActivityForResult", Intent.class, int.class, mStartActivity);
        findAndHookMethodSilently(cls, "startActivityForResult", Intent.class, int.class, android.os.Bundle.class, mStartActivity);
    }

    @Override
    public void init() {
        hookStartActivityVariants(Activity.class);
        hookStartActivityVariants(ContextWrapper.class);
    }
}
