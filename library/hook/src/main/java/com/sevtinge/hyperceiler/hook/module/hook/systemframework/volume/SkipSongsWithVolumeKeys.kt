package com.sevtinge.hyperceiler.hook.module.hook.systemframework.volume

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook

object SkipSongsWithVolumeKeys : BaseHook() {

    private var isScreenOn = true
    private var handler = Handler(Looper.getMainLooper())
    private var volumeUpRunnable: Runnable? = null
    private var volumeDownRunnable: Runnable? = null
    private var volumeUpPressed = false
    private var volumeDownPressed = false
    private var volumeUpConsumed = false
    private var volumeDownConsumed = false

    override fun init() {
        val phoneWindowManagerClass = loadClassOrNull("com.android.server.policy.PhoneWindowManager")
            ?: loadClassOrNull("com.android.server.policy.MiuiPhoneWindowManager")

        if (phoneWindowManagerClass != null) {
            // interceptKeyBeforeQueueing
            phoneWindowManagerClass.methodFinder()
                .filterByName("interceptKeyBeforeQueueing")
                .firstOrNull()?.createHook {
                    before {
                        val event = it.args[0] as KeyEvent
                        val keyCode = event.keyCode
                        val action = event.action

                        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) return@before

                        val context = it.thisObject.javaClass.getMethod("getContext").invoke(it.thisObject) as Context
                        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                        isScreenOn = powerManager.isInteractive

                        if (isScreenOn) return@before

                        if (action == KeyEvent.ACTION_DOWN) {
                            it.result = 0 // consume to prevent clash

                            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                if (!volumeUpPressed) {
                                    volumeUpPressed = true
                                    volumeUpConsumed = false
                                    volumeUpRunnable = Runnable {
                                        volumeUpConsumed = true
                                        sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT)
                                    }
                                    handler.postDelayed(volumeUpRunnable!!, 500)
                                }
                            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                                if (!volumeDownPressed) {
                                    volumeDownPressed = true
                                    volumeDownConsumed = false
                                    volumeDownRunnable = Runnable {
                                        volumeDownConsumed = true
                                        sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                                    }
                                    handler.postDelayed(volumeDownRunnable!!, 500)
                                }
                            }
                        } else if (action == KeyEvent.ACTION_UP) {
                            it.result = 0 // consume UP

                            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                volumeUpPressed = false
                                volumeUpRunnable?.let { r -> handler.removeCallbacks(r) }
                                if (!volumeUpConsumed) {
                                    adjustVolume(context, android.media.AudioManager.ADJUST_RAISE)
                                }
                                volumeUpConsumed = false
                            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                                volumeDownPressed = false
                                volumeDownRunnable?.let { r -> handler.removeCallbacks(r) }
                                if (!volumeDownConsumed) {
                                    adjustVolume(context, android.media.AudioManager.ADJUST_LOWER)
                                }
                                volumeDownConsumed = false
                            }
                        }
                    }
                }
        }
    }

    private fun adjustVolume(context: Context, direction: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, direction, 0)
    }

    private fun sendMediaKey(context: Context, keyCode: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val eventDown = KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keyCode, 0)
        val eventUp = KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0)
        audioManager.dispatchMediaKeyEvent(eventDown)
        audioManager.dispatchMediaKeyEvent(eventUp)
    }
}
