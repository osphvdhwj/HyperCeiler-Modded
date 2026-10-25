package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.sidebar.game

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook

/**
 * The China-ROM game toolbox panel ships Chinese-only labels; when the
 * system language is English the panel falls back to them. Walk the panel's
 * view tree after inflation and replace known Chinese labels with English.
 */
object GamePanelTranslator : BaseHook() {

    private val zhToEn = mapOf(
        "性能模式" to "Performance mode",
        "均衡模式" to "Balanced mode",
        "性能优先模式" to "Performance-first mode",
        "当前性能优先模式" to "Performance-first mode active",
        "亮度锁定" to "Brightness lock",
        "阻止手势" to "Block gestures",
        "防误触" to "Anti-mistouch",
        "截屏" to "Screenshot",
        "录屏" to "Screen recording",
        "游戏音效" to "Game sound effects",
        "4D振感" to "4D vibration",
        "弹幕通知" to "Barrage notifications",
        "微信防打扰" to "WeChat do-not-disturb",
        "QQ防打扰" to "QQ do-not-disturb",
        "实时网速" to "Live network speed",
        "自动亮度" to "Auto brightness",
        "变声" to "Voice changer",
        "游戏加速" to "Game Turbo",
        "工具箱" to "Toolbox",
        "更多" to "More",
        "推荐" to "Recommended",
        "已开启" to "On",
        "已关闭" to "Off",
        "打开" to "On",
        "关闭" to "Off",
        "护眼模式" to "Eye care",
        "防闪烁" to "Flicker-free",
        "色彩增强" to "Color enhance",
        "图像增强" to "Image enhance",
        "画质增强" to "Picture enhance",
        "边缘抗锯齿" to "Anti-aliasing",
        "性能增强" to "Performance enhance",
        "网络加速" to "Network boost",
        " MiSound" to " MiSound",
        "环绕声" to "Surround sound",
        "耳机音效" to "Headphone sound",
        "视频音效" to "Video sound",
        "视频增强" to "Video enhance",
        "色彩还原" to "True colors",
        "Memo" to "Memo",
    )

    override fun init() {
        val cls = findClassIfExists("com.miui.gamebooster.windowmanager.newbox.GameToolboxMainView")
            ?: return

        hookAllConstructors(cls, object : MethodHook() {
            override fun after(param: MethodHookParam) {
                val root = param.thisObject as? ViewGroup ?: return
                // Panel content (RecyclerView items) loads asynchronously —
                // re-walk a few times so late-bound labels get translated too.
                root.post { translate(root) }
                for (delay in longArrayOf(400, 1000, 2000, 3500)) {
                    root.postDelayed({ translate(root) }, delay)
                }
            }
        })
    }

    private fun translate(view: View) {
        if (view is TextView) {
            val original = view.text?.toString()?.trim() ?: return
            zhToEn[original]?.let { en ->
                if (original != en) view.text = en
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) translate(view.getChildAt(i))
        }
    }
}
