package com.sevtinge.hyperceiler.hook.module.hook.systemframework.volume;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioSystem;
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
                int streamType = (int) param.args[0];
                if (streamType != AudioManager.STREAM_MUSIC) {
                    return;
                }

                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

                if (pm != null && !pm.isInteractive()) { // Screen is off
                    int index = (int) param.args[1];
                    
                    // AudioSystem.getDevicesForStream returns a bitmask of devices
                    int device = (int) XposedHelpers.callStaticMethod(
                            XposedHelpers.findClass("android.media.AudioSystem", mContext.getClassLoader()),
                            "getDevicesForStream", streamType);

                    boolean isEarphone = (device & (AudioSystem.DEVICE_OUT_WIRED_HEADSET |
                            AudioSystem.DEVICE_OUT_WIRED_HEADPHONE |
                            AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP |
                            AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES |
                            AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER |
                            AudioSystem.DEVICE_OUT_USB_HEADSET)) != 0;

                    int limit;
                    if (isEarphone) {
                        limit = mPrefsMap.getInt("system_framework_volume_limit_screen_off_earphones", 15);
                    } else {
                        limit = mPrefsMap.getInt("system_framework_volume_limit_screen_off_speaker", 15);
                    }
                    
                    // Scale limit based on max volume index which might be fine-grained (e.g. multiplied by 10)
                    int maxIndex = (int) XposedHelpers.callMethod(param.thisObject, "getStreamMaxVolume", streamType);
                    
                    int scaledLimit = limit;
                    if (maxIndex > 15) {
                        scaledLimit = limit * (maxIndex / 15);
                    }
                    
                    if (index > scaledLimit) {
                        param.args[1] = scaledLimit;
                    }
                }
            }
        });
    }
}
