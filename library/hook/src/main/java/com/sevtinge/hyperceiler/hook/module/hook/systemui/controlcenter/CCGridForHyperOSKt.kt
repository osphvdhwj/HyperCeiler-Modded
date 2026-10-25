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
package com.sevtinge.hyperceiler.hook.module.hook.systemui.controlcenter

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils.logE
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefType
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsChangeObserver
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.WeakHashMap

// from YunZiA & HyperCeiler Mod
object CCGridForHyperOSKt {
    private var activeLayoutRef: WeakReference<ViewGroup>? = null
    private var observerRegistered = false

    private val activeIconViews: MutableSet<View> = Collections.newSetFromMap(
        WeakHashMap<View, Boolean>()
    )
    private val activeTileItemViews: MutableSet<ViewGroup> = Collections.newSetFromMap(
        WeakHashMap<ViewGroup, Boolean>()
    )

    private val isCustomGrid: Boolean
        get() = PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_custom_grid")

    private val isRoundedRect: Boolean
        get() = PrefsUtils.mPrefsMap.getBoolean("system_ui_control_center_rounded_rect")

    private val tileScale: Float
        get() {
            val scalePercent = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_tile_scale", 100)
            return (scalePercent.coerceIn(50, 150)) / 100.0f
        }

    fun getCornerRadiusPx(density: Float): Float {
        val radiusDp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_rounded_rect_radius", 26)
        val clampedDp = radiusDp.coerceIn(0, 100).toFloat()
        return clampedDp * density.coerceAtLeast(1.0f)
    }

    fun getResolvedRadius(view: View?): Float {
        val density = view?.resources?.displayMetrics?.density ?: 2.75f
        return if (isRoundedRect) {
            getCornerRadiusPx(density)
        } else {
            72.0f
        }
    }

    fun applyTileScale(view: View) {
        val scale = tileScale
        view.scaleX = scale
        view.scaleY = scale
        view.pivotX = view.width / 2.0f
        view.pivotY = view.height / 2.0f
    }

    fun applyCornerRadius(view: View) {
        if (!isRoundedRect) return
        val density = view.resources?.displayMetrics?.density ?: 2.75f
        val r = getCornerRadiusPx(density)
        val bg = view.background
        if (bg is GradientDrawable) {
            bg.cornerRadius = r
        }
        runCatching {
            XposedHelpers.callMethod(view, "setCornerRadius", r)
        }
    }

    @JvmStatic
    fun initCCGridForHyperOS(classLoader: ClassLoader?) {
        if (classLoader == null) return

        initCCGridLayout(classLoader)
        initCCCornerRadius(classLoader)
        initCCTileItemHooks(classLoader)
    }

    @JvmStatic
    fun initCCGridLayout(classLoader: ClassLoader) {
        // HyperOS 2: the CC tile grid is com.android.systemui.qs.MiuiPagedTileLayout
        // (the old miui.systemui.controlcenter.qs.tileview.QSTileListLayout is gone).
        val targetClass = "com.android.systemui.qs.MiuiPagedTileLayout"

        // After construction: apply the custom grid fields.
        runCatching {
            XposedBridge.hookAllConstructors(
                XposedHelpers.findClass(targetClass, classLoader),
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? ViewGroup ?: return
                        activeLayoutRef = WeakReference(view)
                        registerObserverIfNeeded(view.context)
                        if (isCustomGrid) {
                            applyGridLayout(view)
                        }
                    }
                }
            )
        }.onFailure {
            logE("initCCGridForHyperOS", "MiuiPagedTileLayout constructor hook failed: $it")
        }

        // Column count queries (called during tile distribution).
        runCatching {
            XposedHelpers.findAndHookMethod(
                targetClass,
                classLoader,
                "getColumnCount",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!isCustomGrid) return
                        val view = param.thisObject as? ViewGroup ?: return
                        val isPortrait = view.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                        val colsPortrait = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns", 4)
                        val colsLandscape = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns_horizontal", 6)
                        param.result = if (isPortrait) colsPortrait else colsLandscape
                    }
                }
            )
        }.onFailure {
            logE("initCCGridForHyperOS", "MiuiPagedTileLayout getColumnCount hook failed: $it")
        }

        runCatching {
            XposedHelpers.findAndHookMethod(
                targetClass,
                classLoader,
                "setMaxColumns",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!isCustomGrid) return
                        val view = param.thisObject as? ViewGroup ?: return
                        val isPortrait = view.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                        val colsPortrait = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns", 4)
                        val colsLandscape = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns_horizontal", 6)
                        param.args[0] = if (isPortrait) colsPortrait else colsLandscape
                    }
                }
            )
        }.onFailure {
            logE("initCCGridForHyperOS", "MiuiPagedTileLayout setMaxColumns hook failed: $it")
        }
    }

    private fun registerObserverIfNeeded(context: Context) {
        if (observerRegistered) return
        observerRegistered = true
        val handler = Handler(Looper.getMainLooper())

        val keys = listOf(
            "prefs_key_system_ui_control_center_custom_grid",
            "prefs_key_system_ui_control_center_grid_columns",
            "prefs_key_system_ui_control_center_grid_columns_horizontal",
            "prefs_key_system_ui_control_center_tile_margin_h",
            "prefs_key_system_ui_control_center_tile_margin_v",
            "prefs_key_system_ui_control_center_grid_padding_h",
            "prefs_key_system_ui_control_center_grid_padding_v",
            "prefs_key_system_ui_control_center_rounded_rect",
            "prefs_key_system_ui_control_center_rounded_rect_radius",
            "prefs_key_system_ui_control_center_tile_scale"
        )

        for (key in keys) {
            val isBool = key.contains("custom_grid") || (key.contains("rounded_rect") && !key.contains("radius"))
            val type = if (isBool) PrefType.Boolean else PrefType.Integer
            val defVal: Any = if (isBool) false else if (key.contains("scale")) 100 else if (key.contains("radius")) 26 else 0

            runCatching {
                object : PrefsChangeObserver(context, handler, true, type, key, defVal) {
                    override fun onChange(type: PrefType?, uri: Uri?, name: String?, def: Any?) {
                        handler.post {
                            // 1. Grid layout dynamic update
                            activeLayoutRef?.get()?.let { layout ->
                                if (isCustomGrid) {
                                    applyGridLayout(layout)
                                }
                                layout.requestLayout()
                                layout.invalidate()
                            }

                            // 2. Tile icon views dynamic update
                            for (iconView in activeIconViews) {
                                applyTileScale(iconView)
                                if (isRoundedRect) {
                                    applyCornerRadius(iconView)
                                } else {
                                    (iconView.background as? GradientDrawable)?.cornerRadius = 72.0f
                                    runCatching {
                                        XposedHelpers.callMethod(iconView, "setCornerRadius", 72.0f)
                                    }
                                }
                                iconView.requestLayout()
                                iconView.invalidate()
                            }

                            // 3. Tile item views dynamic update
                            for (itemView in activeTileItemViews) {
                                refreshTileItemView(itemView)
                                itemView.requestLayout()
                                itemView.invalidate()
                            }
                        }
                    }
                }
            }
        }
    }

    fun applyGridLayout(view: ViewGroup) {
        runCatching {
            val resources = view.resources ?: return
            val density = resources.displayMetrics.density
            val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

            val colsPortrait = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns", 4)
            val colsLandscape = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_columns_horizontal", 6)
            val cols = if (isPortrait) colsPortrait else colsLandscape

            val marginHDp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_tile_margin_h", 8)
            val marginVDp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_tile_margin_v", 8)
            val paddingHDp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_padding_h", 16)
            val paddingVDp = PrefsUtils.mPrefsMap.getInt("system_ui_control_center_grid_padding_v", 16)

            val marginHPx = (marginHDp * density).toInt()
            val marginVPx = (marginVDp * density).toInt()
            val paddingHPx = (paddingHDp * density).toInt()
            val paddingVPx = (paddingVDp * density).toInt()

            // 1. Set column count
            runCatching { XposedHelpers.setIntField(view, "mMaxColumns", cols) }

            // 2. Set horizontal and vertical inter-tile spacing
            runCatching { XposedHelpers.setIntField(view, "mCellMarginHorizontal", marginHPx) }
            runCatching { XposedHelpers.setIntField(view, "mCellMarginVertical", marginVPx) }
            runCatching { XposedHelpers.setIntField(view, "mTileMarginHorizontal", marginHPx) }
            runCatching { XposedHelpers.setIntField(view, "mTileMarginVertical", marginVPx) }
            runCatching { XposedHelpers.setIntField(view, "mTileMargin", marginHPx) }
            runCatching { XposedHelpers.setIntField(view, "mCellMargin", marginHPx) }

            // 3. Set container edge padding (both horizontal and vertical)
            view.setPaddingRelative(paddingHPx, paddingVPx, paddingHPx, paddingVPx)
            runCatching { XposedHelpers.setIntField(view, "mPaddingStart", paddingHPx) }
            runCatching { XposedHelpers.setIntField(view, "mPaddingEnd", paddingHPx) }
            runCatching { XposedHelpers.setIntField(view, "mPaddingTop", paddingVPx) }
            runCatching { XposedHelpers.setIntField(view, "mPaddingBottom", paddingVPx) }
        }.onFailure {
            logE("initCCGridForHyperOS", "applyGridLayout failed: $it")
        }
    }

    @JvmStatic
    fun initCCCornerRadius(classLoader: ClassLoader) {
        // HyperOS 2: miui.systemui.controlcenter.qs.tileview.QSTileItemIconView
        // no longer exists — corner-radius hooks disabled until the new tile
        // icon view is mapped.
        return
        val targetClass = "miui.systemui.controlcenter.qs.tileview.QSTileItemIconView"
    }

    @JvmStatic
    fun initCCTileItemHooks(classLoader: ClassLoader) {
        // HyperOS 2: tile item views moved — disabled with the corner hooks.
        return
        val iconViewClass = "miui.systemui.controlcenter.qs.tileview.QSTileItemIconView"
        val tileItemViewClass = "miui.systemui.controlcenter.qs.tileview.QSTileItemView"

        // Scale & Shape hooks for QSTileItemIconView: onLayout
        runCatching {
            XposedHelpers.findAndHookMethod(
                iconViewClass,
                classLoader,
                "onLayout",
                Boolean::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? View ?: return
                        activeIconViews.add(view)
                        registerObserverIfNeeded(view.context)
                        applyTileScale(view)
                        if (isRoundedRect) {
                            applyCornerRadius(view)
                        }
                    }
                }
            )
        }

        // Scale & Shape hooks for QSTileItemIconView: onMeasure
        runCatching {
            XposedHelpers.findAndHookMethod(
                iconViewClass,
                classLoader,
                "onMeasure",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? View ?: return
                        activeIconViews.add(view)
                        applyTileScale(view)
                        if (isRoundedRect) {
                            applyCornerRadius(view)
                        }
                    }
                }
            )
        }

        // Scale & Shape hooks for QSTileItemIconView: updateResources
        runCatching {
            XposedHelpers.findAndHookMethod(
                iconViewClass,
                classLoader,
                "updateResources",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? View ?: return
                        activeIconViews.add(view)
                        registerObserverIfNeeded(view.context)
                        applyTileScale(view)
                        if (isRoundedRect) {
                            applyCornerRadius(view)
                        }
                    }
                }
            )
        }

        // Scale hooks for QSTileItemIconView: updateIcon methods
        runCatching {
            val clazz = XposedHelpers.findClass(iconViewClass, classLoader)
            for (method in clazz.declaredMethods) {
                if (method.name == "updateIcon") {
                    XposedBridge.hookMethod(method, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val view = param.thisObject as? View ?: return
                            activeIconViews.add(view)
                            registerObserverIfNeeded(view.context)
                            applyTileScale(view)
                            if (isRoundedRect) {
                                applyCornerRadius(view)
                            }
                        }
                    })
                }
            }
        }

        // Hooks on QSTileItemView container
        runCatching {
            XposedHelpers.findAndHookMethod(
                tileItemViewClass,
                classLoader,
                "onLayout",
                Boolean::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? ViewGroup ?: return
                        activeTileItemViews.add(view)
                        registerObserverIfNeeded(view.context)
                        refreshTileItemView(view)
                    }
                }
            )
        }

        runCatching {
            XposedHelpers.findAndHookMethod(
                tileItemViewClass,
                classLoader,
                "updateResources",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? ViewGroup ?: return
                        activeTileItemViews.add(view)
                        registerObserverIfNeeded(view.context)
                        refreshTileItemView(view)
                    }
                }
            )
        }
    }

    fun refreshTileItemView(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child != null && child.javaClass.name.contains("IconView")) {
                activeIconViews.add(child)
                applyTileScale(child)
                if (isRoundedRect) {
                    applyCornerRadius(child)
                }
            }
        }
    }
}

