package com.sevtinge.hyperceiler.hook.module.hook.systemframework.volume;

import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class VolumeScreenOffLimit extends BaseHook {

    @Override
    public void init() {
        hookAllMethods("com.android.server.audio.AudioService", "setStreamVolume", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                try {
                    if (param.args == null || param.args.length < 2) return;
                    if (!(param.args[0] instanceof Integer) || !(param.args[1] instanceof Integer)) return;

                    int streamType = (int) param.args[0];
                    if (streamType != AudioManager.STREAM_MUSIC) {
                        return;
                    }

                    Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    if (mContext == null) return;
                    PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

                    if (pm != null && !pm.isInteractive()) { // Screen is off
                        int index = (int) param.args[1];
                        
                        Class<?> audioSystemClass = XposedHelpers.findClassIfExists("android.media.AudioSystem", mContext.getClassLoader());
                        if (audioSystemClass == null) return;

                        Object deviceObj = XposedHelpers.callStaticMethod(audioSystemClass, "getDevicesForStream", streamType);
                        if (!(deviceObj instanceof Integer)) return;
                        int device = (int) deviceObj;

                        int deviceOutWiredHeadset = getStaticIntFieldOrDefault(audioSystemClass, "DEVICE_OUT_WIRED_HEADSET", 0x4);
                        int deviceOutWiredHeadphone = getStaticIntFieldOrDefault(audioSystemClass, "DEVICE_OUT_WIRED_HEADPHONE", 0x8);
                        int deviceOutBtA2dp = getStaticIntFieldOrDefault(audioSystemClass, "DEVICE_OUT_BLUETOOTH_A2DP", 0x80);
                        int deviceOutBtA2dpHeadphones = getStaticIntFieldOrDefault(audioSystemClass, "DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES", 0x100);
                        int deviceOutBtA2dpSpeaker = getStaticIntFieldOrDefault(audioSystemClass, "DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER", 0x200);
                        int deviceOutUsbHeadset = getStaticIntFieldOrDefault(audioSystemClass, "DEVICE_OUT_USB_HEADSET", 0x4000);

                        int earphoneMask = deviceOutWiredHeadset | deviceOutWiredHeadphone | deviceOutBtA2dp | deviceOutBtA2dpHeadphones | deviceOutBtA2dpSpeaker | deviceOutUsbHeadset;
                        boolean isEarphone = (device & earphoneMask) != 0;

                        int limit;
                        if (isEarphone) {
                            limit = mPrefsMap.getInt("system_framework_volume_limit_screen_off_earphones", 15);
                        } else {
                            limit = mPrefsMap.getInt("system_framework_volume_limit_screen_off_speaker", 15);
                        }
                        
                        // Scale limit based on max volume index which might be fine-grained (e.g. multiplied by 10)
                        Object maxIndexObj = XposedHelpers.callMethod(param.thisObject, "getStreamMaxVolume", streamType);
                        int maxIndex = (maxIndexObj instanceof Integer) ? (int) maxIndexObj : 15;
                        
                        int scaledLimit = limit;
                        if (maxIndex > 15) {
                            scaledLimit = limit * (maxIndex / 15);
                        }
                        
                        if (index > scaledLimit) {
                            param.args[1] = scaledLimit;
                        }
                    }
                } catch (Throwable t) {
                    logE(TAG, "android", t);
                }
            }
        });
    }

    private int getStaticIntFieldOrDefault(Class<?> clazz, String fieldName, int defValue) {
        try {
            return XposedHelpers.getStaticIntField(clazz, fieldName);
        } catch (Throwable t) {
            return defValue;
        }
    }
}
