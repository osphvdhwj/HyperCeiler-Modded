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
package com.sevtinge.hyperceiler.hook.utils.bridge;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import org.json.JSONObject;

/**
 * Client-side adapter for the AVES+ Tools provider (contract v1.0.0).
 *
 * Every call goes through ContentProvider.call(uri, method, arg, extras)
 * and returns a Bundle whose "json" key carries the result envelope:
 *
 *   { tool, version, ts, durationMs, ok, data, error, jobId }
 *
 * Design rules:
 *   - Never throws. Any failure → null / sensible default, so a hook
 *     that uses this can stay a no-op when the provider is absent.
 *   - Cache the "is provider installed?" answer per process — cheap
 *     enough to call once.
 *   - All calls assume the caller has been granted
 *     com.avesplus.tools.PROVIDER at install / runtime; if the grant
 *     is missing the provider throws SecurityException, which we
 *     surface as a null.
 */
public final class AvesBridge {

    public static final String PROVIDER_PKG = "com.avesplus.tools";
    public static final String AUTHORITY    = "com.avesplus.tools.provider";
    public static final String PERMISSION   = "com.avesplus.tools.PROVIDER";
    public static final Uri    URI          = Uri.parse("content://" + AUTHORITY);

    public static final String EXPECTED_VERSION = "1.0.0";

    /** short-lived cache to avoid hammering PackageManager */
    private static volatile long sLastProbe = 0L;
    private static volatile boolean sPresent = false;
    private static final long PROBE_TTL_MS = 30_000L;

    private AvesBridge() { }

    /** True if the provider responds to a trivial call. */
    public static boolean isAvailable(Context ctx) {
        if (ctx == null) return false;
        long now = System.currentTimeMillis();
        if (now - sLastProbe < PROBE_TTL_MS) return sPresent;
        sLastProbe = now;
        try {
            String v = callString(ctx, "provider.ping", null, null);
            sPresent = v != null;
        } catch (Throwable t) {
            sPresent = false;
        }
        return sPresent;
    }

    public static String version(Context ctx) {
        return callString(ctx, "provider.version", null, null);
    }

    /** Returns null on any failure. */
    public static JSONObject call(Context ctx, String method, String arg, Bundle extras) {
        if (ctx == null || method == null) return null;
        try {
            Bundle reply = ctx.getContentResolver().call(URI, method, arg, extras);
            if (reply == null) return null;
            String json = reply.getString("json");
            if (json == null) return null;
            return new JSONObject(json);
        } catch (Throwable t) {
            // SecurityException, DeadObjectException, provider crash,
            // FileNotFound, etc — treat as "not available this time".
            return null;
        }
    }

    /** Convenience: return just data.toString() from a successful call. */
    public static String callString(Context ctx, String method, String arg, Bundle extras) {
        JSONObject o = call(ctx, method, arg, extras);
        if (o == null || !o.optBoolean("ok", false)) return null;
        Object data = o.opt("data");
        return data == null ? null : data.toString();
    }

    // ------------------------------------------------------------------
    // Typed helpers — Phase 1 surface
    // ------------------------------------------------------------------

    public static String ocr(Context ctx, String path) {
        return callString(ctx, "ocr", path, null);
    }

    public static double nsfw(Context ctx, String path) {
        JSONObject o = call(ctx, "nsfw", path, null);
        if (o == null || !o.optBoolean("ok", false)) return -1.0;
        try {
            return o.optJSONObject("data").optDouble("score", -1.0);
        } catch (Throwable t) { return -1.0; }
    }

    public static String translate(Context ctx, String text, String from, String to) {
        Bundle b = new Bundle();
        b.putString("from", from);
        b.putString("to", to);
        return callString(ctx, "translate", text, b);
    }

    public static String submit(Context ctx, String tool, Bundle args) {
        JSONObject o = call(ctx, tool, null, args);
        if (o == null || !o.optBoolean("ok", false)) return null;
        return o.optString("jobId", null);
    }

    public static JSONObject jobStatus(Context ctx, String jobId) {
        return call(ctx, "batch.status", jobId, null);
    }

    public static JSONObject jobResult(Context ctx, String jobId) {
        return call(ctx, "batch.result", jobId, null);
    }

    public static boolean jobCancel(Context ctx, String jobId) {
        JSONObject o = call(ctx, "batch.cancel", jobId, null);
        return o != null && o.optBoolean("ok", false);
    }
}
