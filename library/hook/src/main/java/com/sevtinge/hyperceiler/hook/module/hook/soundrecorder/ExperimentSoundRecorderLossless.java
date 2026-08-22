package com.sevtinge.hyperceiler.hook.module.hook.soundrecorder;

import com.sevtinge.hyperceiler.hook.module.base.BaseHook;
import de.robv.android.xposed.XC_MethodHook;

/**
 * [Experiment] Enable lossless FLAC audio recording in Sound Recorder.
 * Forces FLAC encoding format and removes bitrate/format restrictions.
 */
public class ExperimentSoundRecorderLossless extends BaseHook {
    @Override
    public void init() {
        // Hook 1: Override output format to FLAC (AudioFormat.ENCODING_PCM_FLOAT)
        findAndHookMethod("com.android.soundrecorder.RecorderParameters",
            "getAudioOutputFormat", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    // MediaRecorder.OutputFormat.OGG = 11, use custom format
                    param.result = 11; // OUTPUT_FORMAT_OGG/FLAC-compatible
                }
            });
        // Hook 2: Force lossless format availability
        findAndHookMethod("com.android.soundrecorder.RecorderParameters",
            "isLosslessSupported", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 3: Miui SoundRecorder variant - enable FLAC option
        findAndHookMethod("com.miui.soundrecorder.RecordingConfig",
            "isFLACEnabled", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = true;
                }
            });
        // Hook 4: Override sample rate to maximum (192kHz)
        findAndHookMethod("com.android.soundrecorder.RecorderParameters",
            "getMaxSampleRate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.result = 192000;
                }
            });
    }
}
