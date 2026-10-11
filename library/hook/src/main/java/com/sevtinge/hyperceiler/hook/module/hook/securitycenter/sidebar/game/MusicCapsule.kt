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
package com.sevtinge.hyperceiler.hook.module.hook.securitycenter.sidebar.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sevtinge.hyperceiler.hook.module.base.BaseHook

/**
 * Music control capsule injected into the Game Turbo in-game toolbox,
 * between the PerformancePanelView and the tools RecyclerView.
 *
 * Layout strategy: find the RecyclerView with id rv_game_toolbox_function,
 * insert our LinearLayout immediately above it in the parent.
 *
 * Media control: use MediaSessionManager directly (SecurityCenter holds
 * MEDIA_CONTENT_CONTROL as a system priv-app, so getActiveSessions(null)
 * is allowed). Poll every 500 ms while the capsule is attached, plus
 * update on button press.
 */
class MusicCapsule : BaseHook() {

    private data class CapsuleHolder(
        val root: ViewGroup,
        val art: ImageView,
        val title: TextView,
        val artist: TextView,
        val prev: ImageButton,
        val playPause: ImageButton,
        val next: ImageButton,
    )

    private val attached = HashMap<ViewGroup, CapsuleHolder>()
    private val pollHandler = Handler(Looper.getMainLooper())
    private var polling = false

    override fun init() {
        val cls = findClassIfExists("com.miui.gamebooster.windowmanager.newbox.GameToolboxMainView")
        if (cls == null) {
            logE(TAG, lpparam.packageName, "GameToolboxMainView not found")
            return
        }

        // Hook the constructor so we run after the view is inflated.
        hookAllConstructors(cls, object : MethodHook() {
            override fun after(param: MethodHookParam) {
                val root = param.thisObject as? ViewGroup ?: return
                root.post { inject(root) }
            }
        })

        // Also hook onAttachedToWindow as a backup.
        findAndHookMethod(cls, "onAttachedToWindow", object : MethodHook() {
            override fun after(param: MethodHookParam) {
                val root = param.thisObject as? ViewGroup ?: return
                root.post { inject(root) }
            }
        })
    }

    private fun inject(root: ViewGroup) {
        if (attached.containsKey(root)) return
        val ctx = root.context

        val rvId = ctx.resources.getIdentifier("rv_game_toolbox_function", "id", ctx.packageName)
        if (rvId == 0) {
            logE(TAG, lpparam.packageName, "rv_game_toolbox_function id not found")
            return
        }
        val rv = root.findViewById<View>(rvId) ?: return
        val parent = rv.parent as? ViewGroup ?: return

        val capsule = buildCapsule(ctx, parent)
        val idx = parent.indexOfChild(rv)
        parent.addView(capsule.root, idx)

        attached[root] = capsule
        wireButtons(capsule, ctx)
        startPolling()

        capsule.root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                attached.remove(root)
                if (attached.isEmpty()) stopPolling()
            }
        })
    }

    private fun buildCapsule(ctx: Context, parent: ViewGroup): CapsuleHolder {
        val dp = ctx.resources.displayMetrics.density
        fun p(v: Int) = (v * dp).toInt()

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(p(12), p(6), p(12), p(6))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                p(56),
            )
            setBackgroundColor(0x1AFFFFFF)
        }

        val art = ImageView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(p(44), p(44))
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val texts = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(p(10), 0, p(10), 0)
        }

        val title = TextView(ctx).apply {
            textSize = 13f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(0xFFFFFFFF.toInt())
        }
        val artist = TextView(ctx).apply {
            textSize = 10f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(0xB3FFFFFF.toInt())
        }
        texts.addView(title)
        texts.addView(artist)

        val prev = ImageButton(ctx).apply {
            setBackgroundColor(0)
            layoutParams = LinearLayout.LayoutParams(p(40), p(40))
            setImageDrawable(iconFor(ctx, android.R.drawable.ic_media_previous))
        }
        val playPause = ImageButton(ctx).apply {
            setBackgroundColor(0)
            layoutParams = LinearLayout.LayoutParams(p(40), p(40))
            setImageDrawable(iconFor(ctx, android.R.drawable.ic_media_play))
        }
        val next = ImageButton(ctx).apply {
            setBackgroundColor(0)
            layoutParams = LinearLayout.LayoutParams(p(40), p(40))
            setImageDrawable(iconFor(ctx, android.R.drawable.ic_media_next))
        }

        root.addView(art)
        root.addView(texts)
        root.addView(prev)
        root.addView(playPause)
        root.addView(next)

        return CapsuleHolder(root, art, title, artist, prev, playPause, next)
    }

    private fun iconFor(ctx: Context, resId: Int): Drawable? =
        try { ctx.getDrawable(resId) } catch (t: Throwable) { null }

    private fun wireButtons(c: CapsuleHolder, ctx: Context) {
        c.prev.setOnClickListener { control(ctx)?.transportControls?.skipToPrevious() }
        c.next.setOnClickListener { control(ctx)?.transportControls?.skipToNext() }
        c.playPause.setOnClickListener {
            val tc = control(ctx)?.transportControls ?: return@setOnClickListener
            val state = control(ctx)?.playbackState?.state ?: 0
            if (state == PlaybackState.STATE_PLAYING) tc.pause() else tc.play()
        }
    }

    private fun control(ctx: Context): MediaController? {
        return try {
            val msm = ctx.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return null
            val sessions = msm.getActiveSessions(null)
            sessions.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: sessions.firstOrNull { it.playbackState != null }
        } catch (t: Throwable) {
            logE(TAG, lpparam.packageName, "getActiveSessions: $t")
            null
        }
    }

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (attached.isEmpty()) { polling = false; return }
            for ((_, holder) in attached) {
                try { refresh(holder) } catch (t: Throwable) { /* ignore */ }
            }
            pollHandler.postDelayed(this, 500)
        }
    }

    private fun startPolling() {
        if (polling) return
        polling = true
        pollHandler.post(pollRunnable)
    }

    private fun stopPolling() {
        polling = false
        pollHandler.removeCallbacks(pollRunnable)
    }

    @SuppressLint("SetTextI18n")
    private fun refresh(c: CapsuleHolder) {
        val ctx = c.root.context
        val controller = control(ctx) ?: run {
            c.title.text = "No media"
            c.artist.text = ""
            c.art.setImageDrawable(null)
            return
        }
        val md: MediaMetadata? = controller.metadata
        val state = controller.playbackState?.state ?: 0

        c.title.text = md?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown"
        c.artist.text = md?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""

        val art: Bitmap? = md?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: md?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        c.art.setImageBitmap(art)

        val icon = if (state == PlaybackState.STATE_PLAYING)
            android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        c.playPause.setImageDrawable(iconFor(ctx, icon))
    }
}
