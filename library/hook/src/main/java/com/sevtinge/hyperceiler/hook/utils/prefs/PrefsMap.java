/*
 * This file is part of HyperCeiler.

 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HyperCeiler Contributions
 */
package com.sevtinge.hyperceiler.hook.utils.prefs;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class PrefsMap<K, V> extends HashMap<K, V> {

    private String normalizeKey(String key) {
        if (key == null) return "";
        return key.startsWith("prefs_key_") ? key : "prefs_key_" + key;
    }

    public Object getObject(String key, Object defValue) {
        return get(normalizeKey(key)) == null ? defValue : get(normalizeKey(key));
    }

    public int getInt(String key, int defValue) {
        String k = normalizeKey(key);
        return get(k) == null ? defValue : (Integer) get(k);
    }

    public String getString(String key, String defValue) {
        String k = normalizeKey(key);
        return get(k) == null ? defValue : (String) get(k);
    }

    public int getStringAsInt(String key, int defValue) {
        String k = normalizeKey(key);
        return get(k) == null ? defValue : Integer.parseInt((String) get(k));
    }

    @SuppressWarnings("unchecked")
    public Set<String> getStringSet(String key) {
        String k = normalizeKey(key);
        return get(k) == null ? new LinkedHashSet<>() : (Set<String>) get(k);
    }

    public boolean getBoolean(String key) {
        String k = normalizeKey(key);
        return get(k) != null && (Boolean) get(k);
    }

}
