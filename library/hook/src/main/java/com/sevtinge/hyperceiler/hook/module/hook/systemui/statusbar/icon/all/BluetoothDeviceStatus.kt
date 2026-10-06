/*
 * This file is part of HyperHand.
 *
 * HyperHand is free software: you can redistribute it and/or modify
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
 * Copyright (C) 2023-2026 HyperHand Contributions
 */
package com.sevtinge.hyperceiler.hook.module.hook.systemui.statusbar.icon.all

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.utils.log.XposedLogUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createAfterHook

object BluetoothDeviceStatus : BaseHook() {

    private val isShowBtBattery by lazy {
        mPrefsMap.getBoolean("system_ui_status_bar_bluetooth_device_battery")
    }

    private val isDifferentDeviceIcon by lazy {
        mPrefsMap.getBoolean("system_ui_status_bar_bluetooth_device_icon_type")
    }

    private var isReceiverRegistered = false

    override fun init() {
        val mMiuiStatusBarSignalPolicyClass by lazy {
            loadClassOrNull("com.android.systemui.statusbar.phone.MiuiStatusBarSignalPolicy")
        }

        val mBluetoothControllerImplClass by lazy {
            loadClassOrNull("com.android.systemui.statusbar.policy.BluetoothControllerImpl")
        }

        mMiuiStatusBarSignalPolicyClass?.methodFinder()
            ?.filterByName("updateBluetooth")
            ?.firstOrNull()?.createAfterHook { param ->
                updateBtStatus(param)
            }

        mBluetoothControllerImplClass?.methodFinder()
            ?.filterByName("updateConnected")
            ?.firstOrNull()?.createAfterHook { param ->
                try {
                    val mContext = XposedHelpers.getObjectField(param.thisObject, "mContext") as? Context
                    mContext?.let { registerBtReceiver(it) }
                } catch (t: Throwable) {
                    XposedLogUtils.logE("BluetoothDeviceStatus", t)
                }
            }
    }

    private fun registerBtReceiver(context: Context) {
        if (isReceiverRegistered) return
        isReceiverRegistered = true
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.action?.let { action ->
                    if (action == "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED" ||
                        action == BluetoothDevice.ACTION_ACL_CONNECTED ||
                        action == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED
                    ) {
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        val batteryLevel = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
                        if (device != null) {
                            handleBluetoothDevice(device, batteryLevel)
                        }
                    }
                }
            }
        }, filter)
    }

    private fun handleBluetoothDevice(device: BluetoothDevice, batteryLevel: Int) {
        val iconSlot = getExtendedIconSlot(device, batteryLevel)
        if (isShowBtBattery && batteryLevel >= 0) {
            XposedLogUtils.logI("BluetoothDeviceStatus", "BT Device: ${device.name}, Battery: $batteryLevel%, Slot: $iconSlot")
        }
    }

    fun getIconSlotForDeviceClass(btClass: BluetoothClass?): String {
        if (!isDifferentDeviceIcon || btClass == null) return "bluetooth"
        val majorClass = btClass.majorDeviceClass
        val deviceClass = btClass.deviceClass

        return when (majorClass) {
            BluetoothClass.Device.Major.AUDIO_VIDEO -> {
                when (deviceClass) {
                    BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES,
                    BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET,
                    BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE -> "wireless_headset"

                    BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER,
                    BluetoothClass.Device.AUDIO_VIDEO_HI_FI_AUDIO,
                    BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO -> "sound_box"

                    BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> "car"

                    BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER,
                    BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX,
                    BluetoothClass.Device.AUDIO_VIDEO_VCR,
                    BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR -> "tv"

                    BluetoothClass.Device.AUDIO_VIDEO_AUDIO_VIDEO_INTERCOM -> "intercom"

                    else -> "wireless_headset"
                }
            }
            BluetoothClass.Device.Major.WEARABLE -> "pad"
            BluetoothClass.Device.Major.COMPUTER -> "pc"
            BluetoothClass.Device.Major.PHONE -> "phone"

            BluetoothClass.Device.Major.HEALTH -> {
                when (deviceClass) {
                    BluetoothClass.Device.HEALTH_BLOOD_PRESSURE -> "health_pressure"
                    BluetoothClass.Device.HEALTH_TEMPERATURE -> "health_temp"
                    BluetoothClass.Device.HEALTH_WEIGHT -> "health_weight"
                    BluetoothClass.Device.HEALTH_BLOOD_GLUCOSE -> "health_glucose"
                    BluetoothClass.Device.HEALTH_PULSE_OXIMETER -> "health_spo2"
                    BluetoothClass.Device.HEALTH_MEDICATION_MONITOR -> "health_medication"
                    BluetoothClass.Device.HEALTH_KNEE -> "health_knee"
                    BluetoothClass.Device.HEALTH_ANKLE -> "health_ankle"
                    BluetoothClass.Device.HEALTH_ELBOW -> "health_elbow"
                    BluetoothClass.Device.HEALTH_WRIST -> "health_wrist"
                    else -> "health_device"
                }
            }

            BluetoothClass.Device.Major.PERSONAL -> "personal"
            BluetoothClass.Device.Major.VIDEO -> "video"
            BluetoothClass.Device.Major.SCAN -> "scanner"
            BluetoothClass.Device.Major.MONITOR -> "monitor"
            BluetoothClass.Device.Major.GAME -> "game_controller"
            BluetoothClass.Device.Major.SPORT -> "sport"

            else -> "bluetooth"
        }
    }

    fun getExtendedIconSlot(device: BluetoothDevice, batteryLevel: Int): String {
        val iconSlot = getIconSlotForDeviceClass(device.bluetoothClass)

        // Enhanced slot for hearing aids with battery level display
        if (iconSlot == "wireless_headset" && batteryLevel > 0 && batteryLevel <= 20) {
            return "hearing_aid_low"
        } else if (iconSlot == "wireless_headset" && batteryLevel > 20 && batteryLevel <= 60) {
            return "hearing_aid_medium"
        } else if (iconSlot == "wireless_headset" && batteryLevel > 60 && batteryLevel <= 100) {
            return "hearing_aid_high"
        }

        // Audio/video streaming devices get different icon based on connection status
        if (iconSlot == "sound_box" || iconSlot == "tv") {
            return when {
                device.isConnected -> iconSlot + "_active"
                else -> iconSlot + "_standby"
            }
        }

        // Personal devices (watches, fitness bands) get battery-aware icons
        if (iconSlot == "personal") {
            return when (batteryLevel) {
                in 0..20 -> "personal_battery_low"
                in 21..60 -> "personal_battery_medium"
                in 61..100 -> "personal_battery_high"
                else -> "personal"
            }
        }

        return iconSlot
    }

    private fun updateBtStatus(param: XC_MethodHook.MethodHookParam) {
        try {
            if (isShowBtBattery || isDifferentDeviceIcon) {
                val iconController = XposedHelpers.getObjectField(param.thisObject, "mIconController")
                val isBtEnabled = XposedHelpers.getBooleanField(param.thisObject, "mBluetoothEnabled")
                if (isBtEnabled != null && iconController != null) {
                    if (isShowBtBattery) {
                        XposedHelpers.callMethod(iconController, "setIconVisibility", "bluetooth_handsfree_battery", true)
                    }
                }
            }
        } catch (t: Throwable) {
            XposedLogUtils.logE("BluetoothDeviceStatus", t)
        }
    }
}
